package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * DecreaseFatStrategy - Strategy for decreasing fat content
 * Part of the Domain Layer - Strategy Pattern
 */
public class DecreaseFatStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public DecreaseFatStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find lower-fat alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in fat
            if (candidateNutrition.getFat() < originalNutrition.getFat()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByFatReduction(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "decrease_fat";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "decrease_fat".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalFat = swap.getOriginalNutrition().getFat();
        double replacementFat = swap.getReplacementNutrition().getFat();
        double fatReduction = originalFat - replacementFat;
        
        if (fatReduction <= 0) return 0.0;
        
        // Score based on percentage reduction
        double reductionPercent = fatReduction / originalFat;
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
            
            // Find lower-fat alternatives
            List<SwapDTO> ingredientSwaps = findLowerFatAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByFatReduction(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "decrease_fat";
    }
    
    @Override
    public String getDescription() {
        return "Decreases fat content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByFatReduction(List<SwapDTO> swaps) {
        // Sort by fat reduction (most reduction first)
        swaps.sort((a, b) -> {
            double fatChangeA = a.getFatChange();
            double fatChangeB = b.getFatChange();
            return Double.compare(fatChangeA, fatChangeB); // Ascending order for reduction
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findLowerFatAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in fat
            if (candidateNutrition.getFat() < originalNutrition.getFat()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for low-fat swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("beef") || lower.contains("pork") || lower.contains("lamb")) {
            return Arrays.asList("chicken breast", "turkey breast", "fish", "tofu", "egg whites");
        } else if (lower.contains("chicken thigh") || lower.contains("dark meat")) {
            return Arrays.asList("chicken breast", "turkey breast", "fish", "egg whites");
        } else if (lower.contains("cheese") || lower.contains("cream")) {
            return Arrays.asList("low-fat cheese", "cottage cheese", "skim milk", "greek yogurt");
        } else if (lower.contains("milk") || lower.contains("yogurt")) {
            return Arrays.asList("skim milk", "almond milk", "low-fat yogurt", "greek yogurt");
        } else if (lower.contains("nuts") || lower.contains("seeds") || lower.contains("avocado")) {
            return Arrays.asList("fruits", "vegetables", "rice", "pasta");
        } else if (lower.contains("oil") || lower.contains("butter")) {
            return Arrays.asList("cooking spray", "broth", "water", "lemon juice");
        } else if (lower.contains("fried") || lower.contains("crispy")) {
            return Arrays.asList("grilled", "baked", "steamed", "boiled");
        } else {
            // Generic low-fat alternatives
            return Arrays.asList("lean protein", "vegetables", "fruits", "whole grains");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Lower fat alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("decrease_fat");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 