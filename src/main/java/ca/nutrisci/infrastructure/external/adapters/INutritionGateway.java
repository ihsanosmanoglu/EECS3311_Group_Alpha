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
    
    /**
     * Get the food ID for a specific ingredient name
     * @param foodName The name of the food
     * @return The food ID, or -1 if not found
     */
    int getFoodId(String foodName);
    
    /**
     * Get all food groups
     * @return List of all food group names
     */
    List<String> getAllFoodGroups();
    
    /**
     * Get ingredients by food group
     * @param groupName The name of the food group
     * @return List of ingredients in the specified group
     */
    List<String> getIngredientsByGroup(String groupName);
    
    // Batch lookup for multiple ingredients
    List<NutrientInfo> lookupMultipleIngredients(List<String> ingredientNames);
    
    // Ingredient validation
    String findClosestMatch(String ingredientName);
    
    // Gateway management
    void initialize();
    boolean isAvailable();
} 