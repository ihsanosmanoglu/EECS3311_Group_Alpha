package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * IncreaseProteinStrategy - Strategy for increasing protein content
 * Part of the Domain Layer - Strategy Pattern
 */
public class IncreaseProteinStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public IncreaseProteinStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find higher-protein alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in protein
            if (candidateNutrition.getProtein() > originalNutrition.getProtein()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByProteinIncrease(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "increase_protein";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "increase_protein".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalProtein = swap.getOriginalNutrition().getProtein();
        double replacementProtein = swap.getReplacementNutrition().getProtein();
        double proteinIncrease = replacementProtein - originalProtein;
        
        if (proteinIncrease <= 0) return 0.0;
        
        // Score based on protein increase (scale to 0-1)
        double targetIncrease = goal.getTargetValue();
        return Math.min(1.0, proteinIncrease / targetIncrease);
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
            
            // Find higher-protein alternatives
            List<SwapDTO> ingredientSwaps = findHigherProteinAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByProteinIncrease(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "increase_protein";
    }
    
    @Override
    public String getDescription() {
        return "Increases protein content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByProteinIncrease(List<SwapDTO> swaps) {
        // Sort by protein increase (most increase first)
        swaps.sort((a, b) -> {
            double proteinChangeA = a.getProteinChange();
            double proteinChangeB = b.getProteinChange();
            return Double.compare(proteinChangeB, proteinChangeA); // Descending order
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findHigherProteinAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually higher in protein
            if (candidateNutrition.getProtein() > originalNutrition.getProtein()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for high-protein swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("rice") || lower.contains("pasta") || lower.contains("bread")) {
            return Arrays.asList("chicken breast", "fish", "tofu", "eggs", "protein pasta");
        } else if (lower.contains("vegetable") || lower.contains("salad")) {
            return Arrays.asList("chicken breast", "fish", "tofu", "beans", "lentils");
        } else if (lower.contains("fruit")) {
            return Arrays.asList("greek yogurt", "protein smoothie", "nuts", "seeds");
        } else if (lower.contains("snack") || lower.contains("chip")) {
            return Arrays.asList("nuts", "seeds", "protein bar", "hard-boiled eggs");
        } else if (lower.contains("cheese")) {
            return Arrays.asList("cottage cheese", "greek yogurt", "protein cheese");
        } else if (lower.contains("milk")) {
            return Arrays.asList("protein shake", "greek yogurt", "protein milk");
        } else {
            // Generic high-protein alternatives
            return Arrays.asList("chicken breast", "fish", "eggs", "tofu", "beans", "lentils", "greek yogurt");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Higher protein alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("increase_protein");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 