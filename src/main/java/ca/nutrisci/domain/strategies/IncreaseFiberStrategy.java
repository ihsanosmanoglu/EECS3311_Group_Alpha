package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * IncreaseFiberStrategy - Strategy for increasing fiber content
 * Part of the Domain Layer - Strategy Pattern
 */
public class IncreaseFiberStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public IncreaseFiberStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find higher-fiber alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in fiber
            if (candidateNutrition.getFiber() > originalNutrition.getFiber()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByFiberIncrease(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "increase_fiber";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "increase_fiber".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalFiber = swap.getOriginalNutrition().getFiber();
        double replacementFiber = swap.getReplacementNutrition().getFiber();
        double fiberIncrease = replacementFiber - originalFiber;
        
        if (fiberIncrease <= 0) return 0.0;
        
        // Score based on fiber increase (scale to 0-1)
        double targetIncrease = goal.getTargetValue();
        return Math.min(1.0, fiberIncrease / targetIncrease);
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
            
            // Find higher-fiber alternatives
            List<SwapDTO> ingredientSwaps = findHigherFiberAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByFiberIncrease(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "increase_fiber";
    }
    
    @Override
    public String getDescription() {
        return "Increases fiber content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByFiberIncrease(List<SwapDTO> swaps) {
        // Sort by fiber increase (most increase first)
        swaps.sort((a, b) -> {
            double fiberChangeA = a.getFiberChange();
            double fiberChangeB = b.getFiberChange();
            return Double.compare(fiberChangeB, fiberChangeA); // Descending order
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findHigherFiberAlternatives(String originalIngredient, double quantity, 
                                                     NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in fiber
            if (candidateNutrition.getFiber() > originalNutrition.getFiber()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for high-fiber swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("bread") || lower.contains("pasta")) {
            return Arrays.asList("whole wheat bread", "oat bran", "quinoa", "brown rice", "whole grain pasta");
        } else if (lower.contains("rice") || lower.contains("grain")) {
            return Arrays.asList("brown rice", "wild rice", "oats", "barley", "quinoa");
        } else if (lower.contains("cereal")) {
            return Arrays.asList("oat bran", "bran flakes", "whole grain cereal", "oats");
        } else if (lower.contains("fruit")) {
            return Arrays.asList("apples with skin", "pears", "berries", "oranges");
        } else if (lower.contains("vegetable")) {
            return Arrays.asList("broccoli", "brussels sprouts", "artichokes", "beans", "lentils");
        } else {
            // Generic high-fiber alternatives
            return Arrays.asList("beans", "lentils", "vegetables", "whole grains", "fruits");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        // Calculate impact score
        SwapDTO swap = new SwapDTO(original, suggested, "Higher fiber alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("increase_fiber");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 