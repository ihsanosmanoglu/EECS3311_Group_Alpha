package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.*;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import java.util.*;

/**
 * DecreaseCarbohydratesStrategy - Strategy for decreasing carbohydrate content
 * Part of the Domain Layer - Strategy Pattern
 */
public class DecreaseCarbohydratesStrategy implements SwapStrategy {
    
    private INutritionGateway nutritionGateway;
    
    public DecreaseCarbohydratesStrategy(INutritionGateway nutritionGateway) {
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
        
        // Find lower-carbohydrate alternatives
        List<String> candidates = findSimilarIngredients(currentFood);
        
        for (String candidate : candidates) {
            if (candidate.equals(currentFood)) continue;
            
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in carbohydrates
            if (candidateNutrition.getCarbs() < originalNutrition.getCarbs()) {
                SwapDTO swap = createSwap(currentFood, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                swaps.add(swap);
            }
        }
        
        return rankByCarbohydrateReduction(swaps);
    }
    
    @Override
    public String getGoalType() {
        return "decrease_carbohydrates";
    }
    
    @Override
    public boolean canHandle(SwapGoalDTO goal) {
        return goal != null && "decrease_carbohydrates".equals(goal.getGoalType());
    }
    
    @Override
    public double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal) {
        if (swap == null || swap.getOriginalNutrition() == null || swap.getReplacementNutrition() == null) {
            return 0.0;
        }
        
        double originalCarbs = swap.getOriginalNutrition().getCarbs();
        double replacementCarbs = swap.getReplacementNutrition().getCarbs();
        double carbReduction = originalCarbs - replacementCarbs;
        
        if (carbReduction <= 0) return 0.0;
        
        // Score based on percentage reduction
        double reductionPercent = carbReduction / originalCarbs;
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
            
            // Find lower-carbohydrate alternatives
            List<SwapDTO> ingredientSwaps = findLowerCarbohydrateAlternatives(ingredient, quantity, originalNutrition, goal);
            swaps.addAll(ingredientSwaps);
        }
        
        return rankByGoal(swaps);
    }
    
    @Override
    public List<SwapDTO> rankByGoal(List<SwapDTO> swaps) {
        return rankByCarbohydrateReduction(swaps);
    }
    
    @Override
    public String getStrategyType() {
        return "decrease_carbohydrates";
    }
    
    @Override
    public String getDescription() {
        return "Decreases carbohydrate content while maintaining nutritional balance";
    }
    
    // Private helper methods
    
    private List<SwapDTO> rankByCarbohydrateReduction(List<SwapDTO> swaps) {
        // Sort by carbohydrate reduction (most reduction first)
        swaps.sort((a, b) -> {
            double carbChangeA = a.getCarbohydrateChange();
            double carbChangeB = b.getCarbohydrateChange();
            return Double.compare(carbChangeA, carbChangeB); // Ascending order for reduction
        });
        
        return swaps;
    }
    
    private List<SwapDTO> findLowerCarbohydrateAlternatives(String originalIngredient, double quantity, 
                                                      NutrientInfo originalNutrition, SwapGoalDTO goal) {
        List<SwapDTO> alternatives = new ArrayList<>();
        
        // Get similar ingredients
        List<String> candidates = findSimilarIngredients(originalIngredient);
        
        for (String candidate : candidates) {
            NutrientInfo candidateNutrition = nutritionGateway.lookupIngredient(candidate);
            
            // Check if it's actually lower in carbohydrates
            if (candidateNutrition.getCarbs() < originalNutrition.getCarbs()) {
                SwapDTO swap = createSwap(originalIngredient, candidate, 
                                        originalNutrition, candidateNutrition, goal);
                alternatives.add(swap);
            }
        }
        
        return alternatives;
    }
    
    private List<String> findSimilarIngredients(String ingredient) {
        // Simple ingredient categorization for low-carb swaps
        String lower = ingredient.toLowerCase();
        
        if (lower.contains("bread") || lower.contains("toast")) {
            return Arrays.asList("lettuce wraps", "cauliflower bread", "protein bread", "eggs");
        } else if (lower.contains("pasta") || lower.contains("noodles")) {
            return Arrays.asList("zucchini noodles", "shirataki noodles", "spaghetti squash", "cauliflower rice");
        } else if (lower.contains("rice")) {
            return Arrays.asList("cauliflower rice", "broccoli rice", "shirataki rice", "quinoa");
        } else if (lower.contains("potato")) {
            return Arrays.asList("cauliflower mash", "turnip", "radish", "broccoli");
        } else if (lower.contains("fruit") || lower.contains("apple") || lower.contains("banana")) {
            return Arrays.asList("berries", "avocado", "nuts", "vegetables");
        } else if (lower.contains("cereal") || lower.contains("oats")) {
            return Arrays.asList("nuts", "seeds", "eggs", "protein powder");
        } else {
            // Generic low-carb alternatives
            return Arrays.asList("vegetables", "protein", "nuts", "seeds");
        }
    }
    
    private SwapDTO createSwap(String original, String suggested, 
                              NutrientInfo originalNutrition, NutrientInfo suggestedNutrition, 
                              SwapGoalDTO goal) {
        
        SwapDTO swap = new SwapDTO(original, suggested, "Lower carbohydrate alternative");
        swap.setOriginalNutrition(originalNutrition);
        swap.setReplacementNutrition(suggestedNutrition);
        swap.setGoalType("decrease_carbohydrates");
        
        double impactScore = calculateImpactScore(swap, goal);
        swap.setImpactScore(impactScore);
        
        return swap;
    }
} 