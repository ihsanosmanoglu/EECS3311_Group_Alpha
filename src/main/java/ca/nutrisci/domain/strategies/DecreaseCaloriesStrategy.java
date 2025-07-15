package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * DecreaseCaloriesStrategy - Strategy for decreasing calorie content
 * Part of the Domain Layer - Strategy Pattern
 */
public class DecreaseCaloriesStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public DecreaseCaloriesStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find lower-calorie alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in calories
            if (candidateNutrition.getCalories() < originalNutrition.getCalories()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByCalorieReduction(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "decrease_calories";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "decrease_calories".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalCalories = swap.getOriginalNutrition().getCalories();
        double replacementCalories = swap.getReplacementNutrition().getCalories();
        double calorieReduction = originalCalories - replacementCalories;
        
        if (calorieReduction <= 0) return 0.0;
        
        // Score based on percentage reduction
        double reductionPercent = calorieReduction / originalCalories;
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
            
            // Find lower-calorie alternatives
            List<SwapDTO> ingredientSwaps = findLowerCalorieAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByCalorieReduction(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "decrease_calories";
    }
    
    @Override
    public String getDescription() {
        return "Decreases calorie content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByCalorieReduction(List<SwapDTO> swaps) {
        // Sort by calorie reduction (most reduction first)
        swaps.sort((a, b) -> {
            double calorieChangeA = a.getCalorieChange();
            double calorieChangeB = b.getCalorieChange();
            return Double.compare(calorieChangeA, calorieChangeB);
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findLowerCalorieAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in calories
            if (candidateNutrition.getCalories() < originalNutrition.getCalories()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("bread") || lower.contains("pasta")) {
            return Arrays.asList("whole wheat bread", "brown rice", "quinoa", "oats");
        } else if (lower.contains("beef") || lower.contains("pork")) {
            return Arrays.asList("chicken breast", "turkey", "fish", "tofu");
        } else if (lower.contains("milk") || lower.contains("cheese")) {
            return Arrays.asList("skim milk", "almond milk", "low-fat cheese", "yogurt");
        } else if (lower.contains("oil") || lower.contains("butter")) {
            return Arrays.asList("olive oil", "cooking spray", "avocado");
        } else {
            // Generic alternatives
            return Arrays.asList("vegetables", "fruits", "lean protein", "whole grains");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        // Calculate impact score
        SwapDTO swap = new SwapDTO(original, suggested, "Lower calorie alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("decrease_calories");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 