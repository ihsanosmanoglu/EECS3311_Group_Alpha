package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * IncreaseCaloriesStrategy - Strategy for increasing calorie content
 * Part of the Domain Layer - Strategy Pattern
 */
public class IncreaseCaloriesStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public IncreaseCaloriesStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find higher-calorie alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in calories
            if (candidateNutrition.getCalories() > originalNutrition.getCalories()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByCalorieIncrease(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "increase_calories";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "increase_calories".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalCalories = swap.getOriginalNutrition().getCalories();
        double replacementCalories = swap.getReplacementNutrition().getCalories();
        double calorieIncrease = replacementCalories - originalCalories;
        
        if (calorieIncrease <= 0) return 0.0;
        
        // Score based on percentage increase
        double increasePercent = calorieIncrease / originalCalories;
        return Math.min(1.0, increasePercent * 2.0); // Cap at 1.0
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
            
            // Find higher-calorie alternatives
            List<SwapDTO> ingredientSwaps = findHigherCalorieAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByCalorieIncrease(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "increase_calories";
    }
    
    @Override
    public String getDescription() {
        return "Increases calorie content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByCalorieIncrease(List<SwapDTO> swaps) {
        // Sort by calorie increase (most increase first)
        swaps.sort((a, b) -> {
            double calorieChangeA = a.getCalorieChange();
            double calorieChangeB = b.getCalorieChange();
            return Double.compare(calorieChangeB, calorieChangeA); // Descending order
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findHigherCalorieAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in calories
            if (candidateNutrition.getCalories() > originalNutrition.getCalories()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for higher-calorie swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("bread") || lower.contains("pasta")) {
            return Arrays.asList("croissant", "bagel", "buttered toast", "pasta with cream sauce");
        } else if (lower.contains("chicken") || lower.contains("turkey")) {
            return Arrays.asList("chicken thigh", "beef", "pork", "salmon");
        } else if (lower.contains("milk") || lower.contains("yogurt")) {
            return Arrays.asList("whole milk", "cream", "full-fat yogurt", "cheese");
        } else if (lower.contains("vegetable") || lower.contains("salad")) {
            return Arrays.asList("avocado", "nuts", "seeds", "olive oil dressing");
        } else if (lower.contains("fruit")) {
            return Arrays.asList("banana", "avocado", "dried fruits", "nuts");
        } else {
            // Generic higher-calorie alternatives
            return Arrays.asList("nuts", "seeds", "avocado", "cheese", "oil");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Higher calorie alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("increase_calories");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 