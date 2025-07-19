package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * DecreaseProteinStrategy - Strategy for decreasing protein content
 * Part of the Domain Layer - Strategy Pattern
 */
public class DecreaseProteinStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public DecreaseProteinStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find lower-protein alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in protein
            if (candidateNutrition.getProtein() < originalNutrition.getProtein()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByProteinReduction(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "decrease_protein";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "decrease_protein".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalProtein = swap.getOriginalNutrition().getProtein();
        double replacementProtein = swap.getReplacementNutrition().getProtein();
        double proteinReduction = originalProtein - replacementProtein;
        
        if (proteinReduction <= 0) return 0.0;
        
        // Score based on percentage reduction
        double reductionPercent = proteinReduction / originalProtein;
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
            
            // Find lower-protein alternatives
            List<SwapDTO> ingredientSwaps = findLowerProteinAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByProteinReduction(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "decrease_protein";
    }
    
    @Override
    public String getDescription() {
        return "Decreases protein content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByProteinReduction(List<SwapDTO> swaps) {
        // Sort by protein reduction (most reduction first)
        swaps.sort((a, b) -> {
            double proteinChangeA = a.getProteinChange();
            double proteinChangeB = b.getProteinChange();
            return Double.compare(proteinChangeA, proteinChangeB); // Ascending order for reduction
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findLowerProteinAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in protein
            if (candidateNutrition.getProtein() < originalNutrition.getProtein()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for lower-protein swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("beef") || lower.contains("chicken") || lower.contains("pork")) {
            return Arrays.asList("vegetables", "rice", "pasta", "potatoes");
        } else if (lower.contains("fish") || lower.contains("seafood")) {
            return Arrays.asList("vegetables", "rice", "pasta", "fruits");
        } else if (lower.contains("eggs")) {
            return Arrays.asList("vegetables", "fruits", "rice", "oats");
        } else if (lower.contains("beans") || lower.contains("lentils")) {
            return Arrays.asList("vegetables", "fruits", "rice", "pasta");
        } else if (lower.contains("cheese") || lower.contains("yogurt")) {
            return Arrays.asList("fruits", "vegetables", "rice", "pasta");
        } else if (lower.contains("nuts") || lower.contains("seeds")) {
            return Arrays.asList("fruits", "vegetables", "rice", "crackers");
        } else {
            // Generic lower-protein alternatives
            return Arrays.asList("fruits", "vegetables", "rice", "pasta");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Lower protein alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("decrease_protein");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 