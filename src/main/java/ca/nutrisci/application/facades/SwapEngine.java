package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.domain.strategies.SwapStrategy;
import ca.nutrisci.domain.strategies.SwapStrategyFactory;
import ca.nutrisci.infrastructure.data.repositories.SwapHistoryRepo;
import ca.nutrisci.infrastructure.data.repositories.MealLogRepo;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import java.time.LocalDate;
import java.util.*;

/**
 * SwapEngine - Facade for all food swap operations
 * Part of the Application Layer - Facade Pattern
 * This is the main entry point for UI swap operations
 */
public class SwapEngine implements ISwapFacade {
    
    private SwapStrategyFactory strategyFactory;
    private SwapHistoryRepo swapHistoryRepo;
    private INutritionGateway nutritionGateway;
    private MealLogRepo mealLogRepo;
    
    public SwapEngine(SwapStrategyFactory strategyFactory, SwapHistoryRepo swapHistoryRepo, 
                      INutritionGateway nutritionGateway, MealLogRepo mealLogRepo) {
        this.strategyFactory = strategyFactory;
        this.swapHistoryRepo = swapHistoryRepo;
        this.nutritionGateway = nutritionGateway;
        this.mealLogRepo = mealLogRepo;
    }

    @Override
    public List<SwapDTO> selectStrategyAndSuggest(ArrayList<SwapGoalDTO> goals, MealDTO selectedMeal) {
        if (goals == null || goals.isEmpty()) {
            System.out.println("⚠️ No goals provided for swap suggestions");
            return new ArrayList<>();
        }

        if (selectedMeal == null) {
            System.out.println("⚠️ No meal provided for swap suggestions");
            return new ArrayList<>();
        }

        ArrayList<SwapDTO> allSwaps = new ArrayList<>();
        
        System.out.println("🎯 Processing " + goals.size() + " nutrition goals for meal: " + selectedMeal.getMealType());
        System.out.println("🍽️ Meal ingredients: " + selectedMeal.getIngredientNames());
        
        for (SwapGoalDTO goal : goals) {
            try {
                System.out.println("📊 Processing goal: " + goal.getGoalTarget() + " " + goal.getAction());
                
                // Get the appropriate strategy for this goal
                SwapStrategy strategy = strategyFactory.getStrategy(goal.getGoalTarget(), goal.getAction());
                
                if (strategy != null) {
                    // Generate swaps using the strategy with the actual meal
                    List<SwapDTO> goalSwaps = strategy.generateSwaps(selectedMeal, goal);
                    
                    // Filter and limit swaps to avoid overwhelming the user
                    List<SwapDTO> filteredSwaps = filterAndLimitSwaps(goalSwaps, 5);
                    allSwaps.addAll(filteredSwaps);
                    
                    System.out.println("✅ Generated " + filteredSwaps.size() + " swaps for " + 
                                     goal.getGoalTarget() + " " + goal.getAction());
                } else {
                    System.out.println("❌ No strategy found for: " + goal.getGoalTarget() + " " + goal.getAction());
                }
            } catch (Exception e) {
                System.err.println("❌ Error processing goal " + goal.getGoalTarget() + ": " + e.getMessage());
            }
        }
        
        // Remove duplicates and rank by overall impact
        List<SwapDTO> uniqueSwaps = removeDuplicateSwaps(allSwaps);
        List<SwapDTO> rankedSwaps = rankSwapsByOverallImpact(uniqueSwaps);
        
        System.out.println("🎉 Total unique swaps generated: " + rankedSwaps.size());
        return rankedSwaps;
    }

    @Override
    public SwapResultDTO previewSwap(UUID mealId, SwapDTO proposal) {
        if (mealId == null || proposal == null) {
            throw new IllegalArgumentException("MealId and proposal cannot be null");
        }
        
        try {
            // Get the original meal
            MealDTO originalMeal = mealLogRepo.getSingleMealById(mealId);
            if (originalMeal == null) {
                throw new IllegalArgumentException("Meal not found: " + mealId);
            }
            
            // Calculate nutritional changes if the swap were applied
            ArrayList<Map<String, Double>> nutrientChanges = calculateNutrientChanges(proposal);
            
            // Create result with preview information
            SwapResultDTO result = new SwapResultDTO(proposal, nutrientChanges);
            
            System.out.println("👁️ Preview generated for swap: " + proposal.getOriginalFood() + 
                             " → " + proposal.getReplacementFood());
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error previewing swap: " + e.getMessage());
            throw new RuntimeException("Failed to preview swap", e);
        }
    }

    @Override
    public SwapResultDTO applySwap(UUID mealId, SwapDTO chosen) {
        if (mealId == null || chosen == null) {
            throw new IllegalArgumentException("MealId and chosen swap cannot be null");
        }
        
        try {
            // Get the original meal
            MealDTO originalMeal = mealLogRepo.getSingleMealById(mealId);
            if (originalMeal == null) {
                throw new IllegalArgumentException("Meal not found: " + mealId);
            }
            
            // Apply the swap to the meal
            MealDTO updatedMeal = applySwapToMeal(originalMeal, chosen);
            
            // Save the updated meal
            mealLogRepo.editMeal(mealId, updatedMeal);
            
            // Save swap to history
            chosen.setSwapHistoryId(UUID.randomUUID());
            // TODO: Save to swap history repository when implemented
            
            // Calculate the actual nutritional changes
            ArrayList<Map<String, Double>> nutrientChanges = calculateNutrientChanges(chosen);
            
            SwapResultDTO result = new SwapResultDTO(chosen, nutrientChanges);
            
            System.out.println("✅ Swap applied successfully: " + chosen.getOriginalFood() + 
                             " → " + chosen.getReplacementFood());
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error applying swap: " + e.getMessage());
            throw new RuntimeException("Failed to apply swap", e);
        }
    }

    @Override
    public List<SwapDTO> listAppliedSwaps(LocalDate from, LocalDate to) {
        try {
            // TODO: Implement when SwapHistory repository methods are available
            // For now, return empty list
            System.out.println("📋 Listing applied swaps from " + from + " to " + to);
            return new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("❌ Error listing applied swaps: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Private helper methods
    

    
    private List<SwapDTO> filterAndLimitSwaps(List<SwapDTO> swaps, int maxSwaps) {
        if (swaps == null || swaps.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Filter out invalid swaps and limit results
        return swaps.stream()
                .filter(SwapDTO::isValid)
                .filter(swap -> swap.getImpactScore() > 0.1) // Only meaningful swaps
                .limit(maxSwaps)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    private List<SwapDTO> removeDuplicateSwaps(List<SwapDTO> swaps) {
        Set<String> seen = new HashSet<>();
        List<SwapDTO> unique = new ArrayList<>();
        
        for (SwapDTO swap : swaps) {
            String key = swap.getOriginalFood() + " -> " + swap.getReplacementFood();
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(swap);
            }
        }
        
        return unique;
    }
    
    private List<SwapDTO> rankSwapsByOverallImpact(List<SwapDTO> swaps) {
        // Sort by impact score (highest first)
        swaps.sort((a, b) -> Double.compare(b.getImpactScore(), a.getImpactScore()));
        return swaps;
    }
    
    private ArrayList<Map<String, Double>> calculateNutrientChanges(SwapDTO swap) {
        ArrayList<Map<String, Double>> changes = new ArrayList<>();
        
        if (swap.getOriginalNutrition() != null && swap.getReplacementNutrition() != null) {
            Map<String, Double> changeMap = new HashMap<>();
            
            changeMap.put("calories", swap.getCalorieChange());
            changeMap.put("protein", swap.getProteinChange());
            changeMap.put("carbohydrates", swap.getCarbohydrateChange());
            changeMap.put("fat", swap.getFatChange());
            changeMap.put("fiber", swap.getFiberChange());
            
            changes.add(changeMap);
        }
        
        return changes;
    }
    
    private MealDTO applySwapToMeal(MealDTO originalMeal, SwapDTO swap) {
        // Create a copy of the meal
        List<String> newIngredients = new ArrayList<>(originalMeal.getIngredientNames());
        List<Double> newQuantities = new ArrayList<>(originalMeal.getQuantities());
        
        // Find and replace the ingredient
        String originalFood = swap.getOriginalFood();
        String replacementFood = swap.getReplacementFood();
        
        for (int i = 0; i < newIngredients.size(); i++) {
            if (newIngredients.get(i).toLowerCase().contains(originalFood.toLowerCase())) {
                newIngredients.set(i, replacementFood);
                break; // Replace only the first match
            }
        }
        
        // Recalculate nutrition for the updated meal
        NutrientInfo updatedNutrition = calculateUpdatedNutrition(originalMeal.getNutrients(), swap);
        
        return new MealDTO(
            originalMeal.getId(),
            originalMeal.getProfileId(),
            originalMeal.getDate(),
            originalMeal.getMealType(),
            newIngredients,
            newQuantities,
            updatedNutrition
        );
    }
    
    private NutrientInfo calculateUpdatedNutrition(NutrientInfo originalNutrition, SwapDTO swap) {
        if (originalNutrition == null) {
            return swap.getReplacementNutrition();
        }
        
        // Calculate new nutrition values after swap
        double newCalories = originalNutrition.getCalories() + swap.getCalorieChange();
        double newProtein = originalNutrition.getProtein() + swap.getProteinChange();
        double newCarbs = originalNutrition.getCarbs() + swap.getCarbohydrateChange();
        double newFat = originalNutrition.getFat() + swap.getFatChange();
        double newFiber = originalNutrition.getFiber() + swap.getFiberChange();
        
        return new NutrientInfo(newCalories, newProtein, newCarbs, newFat, newFiber);
    }
} 