package ca.nutrisci.infrastructure.external.adapters;

import ca.nutrisci.application.dto.NutrientInfo;
import java.util.List;

/**
 * INutritionGateway - Interface for external nutrition data access
 * Part of the Infrastructure Layer - Adapter Pattern
 */
public interface INutritionGateway {
    
    // Main lookup method
    NutrientInfo lookupIngredient(String ingredientName);
    
    // Batch lookup for multiple ingredients
    List<NutrientInfo> lookupMultipleIngredients(List<String> ingredientNames);
    
    // Search methods
    List<String> searchIngredients(String searchTerm);
    List<String> getAllIngredients();
    
    // Ingredient validation
    boolean ingredientExists(String ingredientName);
    String findClosestMatch(String ingredientName);
    
    // Gateway management
    void initialize();
    boolean isAvailable();
} 