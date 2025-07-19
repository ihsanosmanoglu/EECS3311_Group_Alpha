package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * DecreaseFiberStrategy - Strategy for decreasing fiber content
 * Part of the Domain Layer - Strategy Pattern
 */
public class DecreaseFiberStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public DecreaseFiberStrategy(INutritionGateway nutritionGateway) {
        this.nutritionGateway = nutritionGateway;
    }
    
    @Override
    public List<SwapDTO> findSwaps(String currentFood, SwapGoalDTO goal) {
        if (currentFood == null || currentFood.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SwapDTO> swaps = new ArrayList<>();
        
        // Get nutrition info for current food
        NutrientInfo originalNutrition = nutritionGateway.lookupIngredient(currentFood);
        
        // Find lower-fiber alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in fiber
            if (candidateNutrition.getFiber() < originalNutrition.getFiber()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByFiberReduction(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "decrease_fiber";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "decrease_fiber".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalFiber = swap.getOriginalNutrition().getFiber();
        double replacementFiber = swap.getReplacementNutrition().getFiber();
        double fiberReduction = originalFiber - replacementFiber;
        
        if (fiberReduction <= 0) return 0.0;
        
        // Score based on percentage reduction
        double reductionPercent = fiberReduction / originalFiber;
        return Math.min(1.0, reductionPercent * 2.0); // Cap at 1.0
    }
    
    @Override
    public List<SwapDTO> generateSwaps(MealDTO meal, SwapGoalDTO goal) {
        List<SwapDTO> swaps = new ArrayList<>();
        
        List<String> ingredients = meal.getIngredientNames();
        List<Double> quantities = meal.getQuantities();
        
        for (int i = 0; i < ingredients.size(); i++) {
            String ingredient = ingredients.get(i);
            double quantity = quantities.get(i);
            
            // Get nutrition info for current ingredient
            NutrientInfo originalNutrition = nutritionGateway.lookupIngredient(ingredient);
            
            // Find lower-fiber alternatives
            List<SwapDTO> ingredientSwaps = findLowerFiberAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByFiberReduction(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "decrease_fiber";
    }
    
    @Override
    public String getDescription() {
        return "Decreases fiber content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByFiberReduction(List<SwapDTO> swaps) {
        // Sort by fiber reduction (most reduction first)
        swaps.sort((a, b) -> {
            double fiberChangeA = a.getFiberChange();
            double fiberChangeB = b.getFiberChange();
            return Double.compare(fiberChangeA, fiberChangeB); // Ascending order for reduction
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findLowerFiberAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in fiber
            if (candidateNutrition.getFiber() < originalNutrition.getFiber()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for low-fiber swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("whole wheat") || lower.contains("whole grain")) {
            return Arrays.asList("white bread", "white rice", "refined pasta", "crackers");
        } else if (lower.contains("brown rice") || lower.contains("wild rice")) {
            return Arrays.asList("white rice", "pasta", "refined grains");
        } else if (lower.contains("oats") || lower.contains("bran")) {
            return Arrays.asList("corn flakes", "rice cereal", "refined cereal");
        } else if (lower.contains("beans") || lower.contains("lentils")) {
            return Arrays.asList("rice", "pasta", "potatoes", "meat");
        } else if (lower.contains("apple") || lower.contains("pear")) {
            return Arrays.asList("peeled apple", "apple juice", "melon", "grapes");
        } else if (lower.contains("berries") || lower.contains("raspberries")) {
            return Arrays.asList("grapes", "melon", "peaches", "banana");
        } else if (lower.contains("broccoli") || lower.contains("brussels sprouts")) {
            return Arrays.asList("lettuce", "cucumber", "zucchini", "carrots");
        } else if (lower.contains("nuts") || lower.contains("seeds")) {
            return Arrays.asList("meat", "cheese", "refined snacks");
        } else {
            // Generic low-fiber alternatives
            return Arrays.asList("white rice", "refined pasta", "peeled fruits", "lean protein");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Lower fiber alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("decrease_fiber");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 