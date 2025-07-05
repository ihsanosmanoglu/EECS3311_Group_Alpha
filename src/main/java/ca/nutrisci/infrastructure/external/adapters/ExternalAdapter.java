package ca.nutrisci.infrastructure.external.adapters;

import ca.nutrisci.application.dto.NutrientInfo;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ExternalAdapter - CSV-based adapter for Canadian Nutrient File data
 * Part of the Infrastructure Layer - Adapter Pattern
 */
public class ExternalAdapter implements INutritionGateway {
    
    private Map<String, NutrientInfo> nutritionData;
    private List<String> ingredientNames;
    private boolean initialized = false;
    private String dataPath;
    
    public ExternalAdapter() {
        this.dataPath = "Canada Nutrient File-20250622/";
        this.nutritionData = new HashMap<>();
        this.ingredientNames = new ArrayList<>();
    }
    
    public ExternalAdapter(String dataPath) {
        this.dataPath = dataPath;
        this.nutritionData = new HashMap<>();
        this.ingredientNames = new ArrayList<>();
    }
    
    @Override
    public void initialize() {
        try {
            loadNutritionData();
            initialized = true;
            System.out.println("Nutrition gateway initialized with " + nutritionData.size() + " ingredients");
        } catch (Exception e) {
            System.err.println("Failed to initialize nutrition gateway: " + e.getMessage());
            loadFallbackData();
        }
    }
    
    @Override
    public NutrientInfo lookupIngredient(String ingredientName) {
        if (!initialized) initialize();
        
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            return new NutrientInfo();
        }
        
        // Try exact match first
        String key = ingredientName.trim().toLowerCase();
        if (nutritionData.containsKey(key)) {
            return nutritionData.get(key);
        }
        
        // Try partial match
        String closest = findClosestMatch(ingredientName);
        if (closest != null) {
            return nutritionData.get(closest.toLowerCase());
        }
        
        // Return empty nutrients if not found
        return new NutrientInfo();
    }
    
    @Override
    public List<NutrientInfo> lookupMultipleIngredients(List<String> ingredientNames) {
        List<NutrientInfo> results = new ArrayList<>();
        for (String ingredient : ingredientNames) {
            results.add(lookupIngredient(ingredient));
        }
        return results;
    }
    
    @Override
    public List<String> searchIngredients(String searchTerm) {
        if (!initialized) initialize();
        
        List<String> results = new ArrayList<>();
        String term = searchTerm.toLowerCase().trim();
        
        for (String ingredient : ingredientNames) {
            if (ingredient.toLowerCase().contains(term)) {
                results.add(ingredient);
            }
        }
        
        return results.subList(0, Math.min(results.size(), 20)); // Limit to 20 results
    }
    
    @Override
    public List<String> getAllIngredients() {
        if (!initialized) initialize();
        return new ArrayList<>(ingredientNames);
    }
    
    @Override
    public boolean ingredientExists(String ingredientName) {
        if (!initialized) initialize();
        return nutritionData.containsKey(ingredientName.trim().toLowerCase());
    }
    
    @Override
    public String findClosestMatch(String ingredientName) {
        if (!initialized) initialize();
        
        String term = ingredientName.trim().toLowerCase();
        
        // Look for partial matches
        for (String ingredient : ingredientNames) {
            if (ingredient.toLowerCase().contains(term) || term.contains(ingredient.toLowerCase())) {
                return ingredient;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean isAvailable() {
        return initialized && !nutritionData.isEmpty();
    }
    
    // Private helper methods
    
    private void loadNutritionData() throws IOException {
        // Try to load from CSV files
        String foodNameFile = dataPath + "FOOD NAME.csv";
        String nutrientAmountFile = dataPath + "NUTRIENT AMOUNT.csv";
        
        if (Files.exists(Paths.get(foodNameFile)) && Files.exists(Paths.get(nutrientAmountFile))) {
            loadFromCSVFiles(foodNameFile, nutrientAmountFile);
        } else {
            System.out.println("CSV files not found, loading fallback data");
            loadFallbackData();
        }
    }
    
    private void loadFromCSVFiles(String foodNameFile, String nutrientAmountFile) throws IOException {
        // Load food names
        Map<String, String> foodNames = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(foodNameFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; } // Skip header
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String foodId = parts[0].trim();
                    String foodName = parts[1].trim().replace("\"", "");
                    foodNames.put(foodId, foodName);
                }
            }
        }
        
        // Load nutrient amounts and build nutrition data
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(nutrientAmountFile))) {
            String line;
            boolean isFirstLine = true;
            Map<String, Map<String, Double>> foodNutrients = new HashMap<>();
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; } // Skip header
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String foodId = parts[0].trim();
                    String nutrientId = parts[1].trim();
                    String valueStr = parts[2].trim();
                    
                    try {
                        double value = Double.parseDouble(valueStr);
                        foodNutrients.computeIfAbsent(foodId, k -> new HashMap<>()).put(nutrientId, value);
                    } catch (NumberFormatException e) {
                        // Skip invalid values
                    }
                }
            }
            
            // Build final nutrition data
            for (Map.Entry<String, String> entry : foodNames.entrySet()) {
                String foodId = entry.getKey();
                String foodName = entry.getValue();
                Map<String, Double> nutrients = foodNutrients.get(foodId);
                
                if (nutrients != null) {
                    NutrientInfo info = buildNutrientInfo(nutrients);
                    nutritionData.put(foodName.toLowerCase(), info);
                    ingredientNames.add(foodName);
                }
            }
        }
    }
    
    private NutrientInfo buildNutrientInfo(Map<String, Double> nutrients) {
        // Canadian Nutrient File nutrient IDs (simplified mapping)
        double calories = nutrients.getOrDefault("208", 0.0);    // Energy
        double protein = nutrients.getOrDefault("203", 0.0);     // Protein
        double carbs = nutrients.getOrDefault("205", 0.0);       // Carbohydrates
        double fat = nutrients.getOrDefault("204", 0.0);         // Total fat
        double fiber = nutrients.getOrDefault("291", 0.0);       // Fiber
        double sodium = nutrients.getOrDefault("307", 0.0);      // Sodium
        double sugar = nutrients.getOrDefault("269", 0.0);       // Sugars
        double calcium = nutrients.getOrDefault("301", 0.0);     // Calcium
        double iron = nutrients.getOrDefault("303", 0.0);        // Iron
        double vitaminC = nutrients.getOrDefault("401", 0.0);    // Vitamin C
        
        return new NutrientInfo(calories, protein, carbs, fat, fiber, 
                               sodium, sugar, calcium, iron, vitaminC);
    }
    
    private void loadFallbackData() {
        // Basic fallback nutrition data for common ingredients
        nutritionData.put("bread", new NutrientInfo(265, 9, 49, 3.2, 2.7));
        nutritionData.put("rice", new NutrientInfo(130, 2.7, 28, 0.3, 0.4));
        nutritionData.put("chicken breast", new NutrientInfo(165, 31, 0, 3.6, 0));
        nutritionData.put("egg", new NutrientInfo(155, 13, 1.1, 11, 0));
        nutritionData.put("milk", new NutrientInfo(42, 3.4, 5, 1, 0));
        nutritionData.put("apple", new NutrientInfo(52, 0.3, 14, 0.2, 2.4));
        nutritionData.put("banana", new NutrientInfo(89, 1.1, 23, 0.3, 2.6));
        nutritionData.put("tomato", new NutrientInfo(18, 0.9, 3.9, 0.2, 1.2));
        nutritionData.put("potato", new NutrientInfo(77, 2, 17, 0.1, 2.2));
        nutritionData.put("beef", new NutrientInfo(250, 26, 0, 15, 0));
        
        for (String ingredient : nutritionData.keySet()) {
            ingredientNames.add(ingredient);
        }
        
        initialized = true;
        System.out.println("Loaded fallback nutrition data for " + nutritionData.size() + " ingredients");
    }
} 