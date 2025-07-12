package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * IncreaseFatStrategy - Strategy for increasing fat content
 * Part of the Domain Layer - Strategy Pattern
 */
public class IncreaseFatStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public IncreaseFatStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find higher-fat alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in fat
            if (candidateNutrition.getFat() > originalNutrition.getFat()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByFatIncrease(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "increase_fat";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "increase_fat".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalFat = swap.getOriginalNutrition().getFat();
        double replacementFat = swap.getReplacementNutrition().getFat();
        double fatIncrease = replacementFat - originalFat;
        
        if (fatIncrease <= 0) return 0.0;
        
        // Score based on percentage increase
        double increasePercent = fatIncrease / originalFat;
        return Math.min(1.0, increasePercent * 2.0); // Cap at 1.0
    }
    
    @Override
    public List<SwapDTO> generateSwaps(MealDTO meal, SwapGoalDTO goal) {
        List<SwapDTO> swaps = new ArrayList<>();
        
        List<String> ingredients = meal.getIngredients();
        List<Double> quantities = meal.getQuantities();
        
        for (int i = 0; i < ingredients.size(); i++) {
            String ingredient = ingredients.get(i);
            double quantity = quantities.get(i);
            
            // Get nutrition info for current ingredient
            NutrientInfo originalNutrition = nutritionGateway.lookupIngredient(ingredient);
            
            // Find higher-fat alternatives
            List<SwapDTO> ingredientSwaps = findHigherFatAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByFatIncrease(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "increase_fat";
    }
    
    @Override
    public String getDescription() {
        return "Increases fat content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByFatIncrease(List<SwapDTO> swaps) {
        // Sort by fat increase (most increase first)
        swaps.sort((a, b) -> {
            double fatChangeA = a.getFatChange();
            double fatChangeB = b.getFatChange();
            return Double.compare(fatChangeB, fatChangeA); // Descending order
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findHigherFatAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in fat
            if (candidateNutrition.getFat() > originalNutrition.getFat()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for high-fat swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("chicken breast") || lower.contains("turkey breast")) {
            return Arrays.asList("chicken thigh", "salmon", "beef", "pork", "duck");
        } else if (lower.contains("lean") || lower.contains("white fish")) {
            return Arrays.asList("salmon", "tuna", "mackerel", "sardines", "beef");
        } else if (lower.contains("skim milk") || lower.contains("low-fat")) {
            return Arrays.asList("whole milk", "cream", "full-fat yogurt", "cheese");
        } else if (lower.contains("vegetable") || lower.contains("salad")) {
            return Arrays.asList("avocado", "nuts", "seeds", "olive oil dressing");
        } else if (lower.contains("rice") || lower.contains("pasta") || lower.contains("bread")) {
            return Arrays.asList("buttered rice", "cream pasta", "buttered bread", "nuts");
        } else if (lower.contains("fruit")) {
            return Arrays.asList("avocado", "coconut", "nuts", "nut butter");
        } else if (lower.contains("cooking spray") || lower.contains("water")) {
            return Arrays.asList("olive oil", "butter", "coconut oil", "avocado oil");
        } else {
            // Generic high-fat alternatives
            return Arrays.asList("nuts", "seeds", "avocado", "olive oil", "cheese", "fatty fish");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Higher fat alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("increase_fat");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 