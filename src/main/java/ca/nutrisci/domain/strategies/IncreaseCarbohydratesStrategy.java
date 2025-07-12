package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * IncreaseCarbohydratesStrategy - Strategy for increasing carbohydrate content
 * Part of the Domain Layer - Strategy Pattern
 */
public class IncreaseCarbohydratesStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public IncreaseCarbohydratesStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find higher-carbohydrate alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in carbohydrates
            if (candidateNutrition.getCarbs() > originalNutrition.getCarbs()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByCarbohydrateIncrease(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "increase_carbohydrates";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "increase_carbohydrates".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalCarbs = swap.getOriginalNutrition().getCarbs();
        double replacementCarbs = swap.getReplacementNutrition().getCarbs();
        double carbIncrease = replacementCarbs - originalCarbs;
        
        if (carbIncrease <= 0) return 0.0;
        
        // Score based on percentage increase
        double increasePercent = carbIncrease / originalCarbs;
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
            
            // Find higher-carbohydrate alternatives
            List<SwapDTO> ingredientSwaps = findHigherCarbohydrateAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByCarbohydrateIncrease(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "increase_carbohydrates";
    }
    
    @Override
    public String getDescription() {
        return "Increases carbohydrate content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByCarbohydrateIncrease(List<SwapDTO> swaps) {
        // Sort by carbohydrate increase (most increase first)
        swaps.sort((a, b) -> {
            double carbChangeA = a.getCarbohydrateChange();
            double carbChangeB = b.getCarbohydrateChange();
            return Double.compare(carbChangeB, carbChangeA); // Descending order
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findHigherCarbohydrateAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in carbohydrates
            if (candidateNutrition.getCarbs() > originalNutrition.getCarbs()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for high-carb swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("protein") || lower.contains("chicken") || lower.contains("meat")) {
            return Arrays.asList("rice", "pasta", "bread", "potatoes", "quinoa");
        } else if (lower.contains("vegetable") || lower.contains("salad")) {
            return Arrays.asList("sweet potatoes", "corn", "peas", "carrots", "beets");
        } else if (lower.contains("fat") || lower.contains("oil") || lower.contains("nuts")) {
            return Arrays.asList("rice", "pasta", "bread", "oats", "bananas");
        } else if (lower.contains("cheese") || lower.contains("dairy")) {
            return Arrays.asList("rice", "pasta", "bread", "crackers", "fruit");
        } else if (lower.contains("egg")) {
            return Arrays.asList("toast", "oatmeal", "fruit", "rice", "pasta");
        } else {
            // Generic high-carb alternatives
            return Arrays.asList("rice", "pasta", "bread", "potatoes", "oats", "bananas");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Higher carbohydrate alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("increase_carbohydrates");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 