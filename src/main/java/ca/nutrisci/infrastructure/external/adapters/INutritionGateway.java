package ca.nutrisci.infrastructure.external.adapters;

import ca.nutrisci.application.dto.NutrientInfo;
import java.util.List;

/**
 * INutritionGateway - Interface for accessing external nutrition data
 * This is the adapter interface that hides the complexity of external data sources
 * like the Canada Nutrient File (CNF) from the application layer
 */
public interface INutritionGateway {
    
    /**
     * Look up nutritional information for a specific ingredient
     * @param ingredient The name of the ingredient to look up
     * @return NutrientInfo containing nutritional data, or null if not found
     */
    NutrientInfo lookupIngredient(String ingredient);
    
    /**
     * Check if the ingredient exists in the nutrition database
     * @param ingredient The name of the ingredient to check
     * @return true if the ingredient exists, false otherwise
     */
    boolean ingredientExists(String ingredient);
    
    /**
     * Search for ingredients by partial name match
     * @param partialName Partial ingredient name to search for
     * @return List of matching ingredient names
     */
    java.util.List<String> searchIngredients(String partialName);
    
    /**
     * Get all available ingredients
     * @return List of all ingredient names in the database
     */
    java.util.List<String> getAllIngredients();
    
    // Batch lookup for multiple ingredients
    List<NutrientInfo> lookupMultipleIngredients(List<String> ingredientNames);
    
    // Ingredient validation
    String findClosestMatch(String ingredientName);
    
    // Gateway management
    void initialize();
    boolean isAvailable();
} 