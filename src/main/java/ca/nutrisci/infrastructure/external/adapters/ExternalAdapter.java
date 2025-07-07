package ca.nutrisci.infrastructure.external.adapters;

import ca.nutrisci.application.dto.NutrientInfo;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ExternalAdapter - CSV-based adapter for Canadian Nutrient File data
 * Part of the Infrastructure Layer - Adapter Pattern
 * Singleton to prevent multiple loading of CNF data
 */
public class ExternalAdapter implements INutritionGateway {
    
    private static ExternalAdapter instance;
    private final String cnfDataPath;
    private final Map<String, NutrientInfo> nutritionCache;
    private final Map<String, String> foodNameToIdMap;
    private final Map<String, String> foodIdToNameMap;
    private final Map<Integer, String> foodGroups;
    private final Map<String, Integer> foodToGroupMap;
    private boolean initialized = false;
    
    private ExternalAdapter(String cnfDataPath) {
        this.cnfDataPath = cnfDataPath;
        this.nutritionCache = new HashMap<>();
        this.foodNameToIdMap = new HashMap<>();
        this.foodIdToNameMap = new HashMap<>();
        this.foodGroups = new HashMap<>();
        this.foodToGroupMap = new HashMap<>();
        initializeData();
    }
    
    /**
     * Get singleton instance of ExternalAdapter
     */
    public static synchronized ExternalAdapter getInstance(String cnfDataPath) {
        if (instance == null) {
            instance = new ExternalAdapter(cnfDataPath);
        }
        return instance;
    }
    
    /**
     * Get singleton instance with default CNF path
     */
    public static synchronized ExternalAdapter getInstance() {
        return getInstance("Canada Nutrient File-20250622");
    }
    
    /**
     * Initialize the nutrition data from CSV files
     */
    private void initializeData() {
        if (initialized) return;
        
        try {
            // Load food groups first
            loadFoodGroups();
            
            // Try to load CNF data
            if (cnfDataPath != null && Files.exists(Paths.get(cnfDataPath))) {
                System.out.println("Loading CNF data from: " + cnfDataPath);
                loadAllFoodNames();
                loadNutrientData();
                System.out.println("Successfully loaded " + nutritionCache.size() + " food items from CNF data");
                System.out.println("Food groups: " + foodGroups.size());
            }
        } catch (Exception e) {
            System.err.println("Error loading CNF data: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Always load default data to ensure we have basic ingredients
        loadDefaultData();
        initialized = true;
        
        System.out.println("Total ingredients available: " + nutritionCache.size());
    }
    
    /**
     * Load food groups from FOOD GROUP.csv
     */
    private void loadFoodGroups() throws IOException {
        String foodGroupFile = cnfDataPath + "/FOOD GROUP.csv";
        if (!Files.exists(Paths.get(foodGroupFile))) {
            System.out.println("CNF FOOD GROUP.csv not found: " + foodGroupFile);
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(foodGroupFile))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 3) {
                    int groupId = Integer.parseInt(parts[0].trim());
                    String groupName = parts[2].trim().replaceAll("\"", "");
                    foodGroups.put(groupId, groupName);
                }
            }
        }
        
        System.out.println("Loaded " + foodGroups.size() + " food groups");
    }
    
    /**
     * Load ALL food names from FOOD NAME.csv
     */
    private void loadAllFoodNames() throws IOException {
        String foodNameFile = cnfDataPath + "/FOOD NAME.csv";
        if (!Files.exists(Paths.get(foodNameFile))) {
            System.out.println("CNF FOOD NAME.csv not found: " + foodNameFile);
            return;
        }

        int foodCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(foodNameFile))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 5) {
                    String foodId = parts[0].trim();
                    String foodGroupId = parts[2].trim();
                    String rawFoodName = parts[4].trim().replaceAll("\"", "");
                    
                    // Clean up the food name by removing food group prefix
                    String cleanFoodName = cleanFoodName(rawFoodName).toLowerCase();
                    
                    // Store mappings
                    foodNameToIdMap.put(cleanFoodName, foodId);
                    foodIdToNameMap.put(foodId, cleanFoodName);
                    
                    // Store food group mapping
                    try {
                        int groupId = Integer.parseInt(foodGroupId);
                        foodToGroupMap.put(cleanFoodName, groupId);
                    } catch (NumberFormatException e) {
                        // Skip invalid group IDs
                    }
                    
                    foodCount++;
                }
            }
        }
        
        System.out.println("Loaded " + foodCount + " food names from CNF database");
    }
    
    /**
     * Clean up food name by removing food group prefix and extracting the main food name
     */
    private String cleanFoodName(String rawFoodName) {
        if (rawFoodName == null || rawFoodName.trim().isEmpty()) {
            return rawFoodName;
        }
        
        // Split by commas to separate the food group from the actual food name
        String[] parts = rawFoodName.split(",");
        
        if (parts.length == 1) {
            // No commas, return as-is
            return rawFoodName.trim();
        }
        
        // Check if the first part is a food group category that should be removed
        String firstPart = parts[0].trim().toLowerCase();
        
        // List of food group prefixes to remove
        String[] foodGroupPrefixes = {
            "babyfood", "baby food", "spices", "grains", "animal fat", "vegetable oil",
            "salad dressing", "shortening", "fish oil", "poultry food products",
            "chinese dish", "fast foods", "cereals", "baked products", "beverages",
            "dairy", "egg", "fats and oils", "legumes", "nuts and seeds", "sweets",
            "vegetables", "fruits", "meat", "poultry", "seafood"
        };
        
        boolean shouldRemoveFirstPart = false;
        for (String prefix : foodGroupPrefixes) {
            if (firstPart.contains(prefix) || firstPart.startsWith(prefix)) {
                shouldRemoveFirstPart = true;
                break;
            }
        }
        
        if (shouldRemoveFirstPart && parts.length > 1) {
            // Remove the first part and join the rest
            StringBuilder result = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) result.append(", ");
                result.append(parts[i].trim());
            }
            return result.toString();
        } else {
            // Keep the original name but reformat it nicely
            // For names like "Cheese, blue" -> "blue cheese"
            if (parts.length == 2) {
                String category = parts[0].trim();
                String type = parts[1].trim();
                
                // For simple category-type pairs, reverse the order
                if (category.toLowerCase().matches("cheese|milk|bread|rice|potato|meat|fish|chicken|beef|turkey|pork")) {
                    return type + " " + category.toLowerCase();
                }
            }
            
            // For more complex names, just remove redundant parts and clean up
            return rawFoodName.trim();
        }
    }
    
    /**
     * Load nutrient data from NUTRIENT AMOUNT.csv
     */
    private void loadNutrientData() throws IOException {
        String nutrientFile = cnfDataPath + "/NUTRIENT AMOUNT.csv";
        if (!Files.exists(Paths.get(nutrientFile))) {
            System.out.println("CNF NUTRIENT AMOUNT.csv not found: " + nutrientFile);
            return;
        }
        
        Map<String, Map<String, Double>> foodNutrients = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(nutrientFile))) {
            String line;
            boolean firstLine = true;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                lineCount++;
                if (lineCount % 10000 == 0) {
                    System.out.println("Processing nutrient data line: " + lineCount);
                }
                
                String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 4) {
                    String foodId = parts[0].trim();
                    String nutrientId = parts[1].trim();
                    String nutrientValue = parts[2].trim();
                    
                    if (!nutrientValue.isEmpty() && !nutrientValue.equals("0")) {
                        try {
                            double value = Double.parseDouble(nutrientValue);
                            foodNutrients.putIfAbsent(foodId, new HashMap<>());
                            foodNutrients.get(foodId).put(nutrientId, value);
                        } catch (NumberFormatException e) {
                            // Skip invalid numbers
                        }
                    }
                }
            }
        }
        
        System.out.println("Processed nutrient data, creating nutrition info objects...");
        
        // Convert to NutrientInfo objects
        int nutritionCount = 0;
        for (Map.Entry<String, String> entry : foodIdToNameMap.entrySet()) {
            String foodId = entry.getKey();
            String foodName = entry.getValue();
            
            Map<String, Double> nutrients = foodNutrients.get(foodId);
            if (nutrients != null && !nutrients.isEmpty()) {
                NutrientInfo nutrientInfo = createNutrientInfo(nutrients);
                nutritionCache.put(foodName, nutrientInfo);
                nutritionCount++;
            }
        }
        
        System.out.println("Created nutrition info for " + nutritionCount + " foods");
    }
    
    /**
     * Create NutrientInfo from raw nutrient data
     */
    private NutrientInfo createNutrientInfo(Map<String, Double> nutrients) {
        // CNF nutrient IDs for key nutrients
        double calories = nutrients.getOrDefault("208", 0.0);    // Energy (kcal)
        double protein = nutrients.getOrDefault("203", 0.0);     // Protein (g)
        double carbs = nutrients.getOrDefault("205", 0.0);       // Carbohydrates (g)
        double fat = nutrients.getOrDefault("204", 0.0);         // Total fat (g)
        double fiber = nutrients.getOrDefault("291", 0.0);       // Fiber (g)
        
        // If no energy data, try to calculate from macronutrients
        if (calories == 0.0) {
            calories = (protein * 4.0) + (carbs * 4.0) + (fat * 9.0);
        }
        
        return new NutrientInfo(calories, protein, carbs, fat, fiber);
    }
    
    /**
     * Load comprehensive default data for fallback
     */
    private void loadDefaultData() {
        System.out.println("Loading default nutrition data...");
        
        // Enhanced default nutrition database
        Map<String, NutrientInfo> defaultNutrition = new HashMap<>();
        
        // Proteins
        defaultNutrition.put("chicken breast", new NutrientInfo(165, 31.0, 0.0, 3.6, 0.0));
        defaultNutrition.put("chicken", new NutrientInfo(165, 31.0, 0.0, 3.6, 0.0));
        defaultNutrition.put("beef", new NutrientInfo(250, 26.0, 0.0, 15.0, 0.0));
        defaultNutrition.put("pork", new NutrientInfo(242, 27.0, 0.0, 14.0, 0.0));
        defaultNutrition.put("fish", new NutrientInfo(206, 22.0, 0.0, 12.0, 0.0));
        defaultNutrition.put("salmon", new NutrientInfo(208, 20.0, 0.0, 13.0, 0.0));
        defaultNutrition.put("tuna", new NutrientInfo(184, 30.0, 0.0, 6.0, 0.0));
        defaultNutrition.put("egg", new NutrientInfo(155, 13.0, 1.1, 11.0, 0.0));
        defaultNutrition.put("eggs", new NutrientInfo(155, 13.0, 1.1, 11.0, 0.0));
        defaultNutrition.put("turkey", new NutrientInfo(135, 30.0, 0.0, 1.0, 0.0));
        
        // Dairy
        defaultNutrition.put("milk", new NutrientInfo(42, 3.4, 5.0, 1.0, 0.0));
        defaultNutrition.put("cheese", new NutrientInfo(113, 25.0, 1.0, 9.0, 0.0));
        defaultNutrition.put("yogurt", new NutrientInfo(59, 10.0, 3.6, 0.4, 0.0));
        defaultNutrition.put("butter", new NutrientInfo(717, 0.9, 0.1, 81.0, 0.0));
        defaultNutrition.put("cream", new NutrientInfo(195, 2.8, 3.4, 20.0, 0.0));
        
        // Grains and starches
        defaultNutrition.put("bread", new NutrientInfo(265, 9.0, 49.0, 3.2, 2.7));
        defaultNutrition.put("rice", new NutrientInfo(130, 2.7, 28.0, 0.3, 0.4));
        defaultNutrition.put("pasta", new NutrientInfo(131, 5.0, 25.0, 1.1, 1.8));
        defaultNutrition.put("oats", new NutrientInfo(389, 16.9, 66.3, 6.9, 10.6));
        defaultNutrition.put("cereal", new NutrientInfo(357, 7.5, 84.0, 2.8, 7.0));
        defaultNutrition.put("quinoa", new NutrientInfo(368, 14.1, 64.2, 6.1, 7.0));
        defaultNutrition.put("wheat", new NutrientInfo(327, 12.6, 71.2, 1.5, 12.2));
        defaultNutrition.put("barley", new NutrientInfo(354, 12.5, 73.5, 2.3, 17.3));
        
        // Vegetables
        defaultNutrition.put("tomato", new NutrientInfo(18, 0.9, 3.9, 0.2, 1.2));
        defaultNutrition.put("tomatoes", new NutrientInfo(18, 0.9, 3.9, 0.2, 1.2));
        defaultNutrition.put("lettuce", new NutrientInfo(15, 1.4, 2.9, 0.2, 1.3));
        defaultNutrition.put("spinach", new NutrientInfo(23, 2.9, 3.6, 0.4, 2.2));
        defaultNutrition.put("broccoli", new NutrientInfo(34, 2.8, 7.0, 0.4, 2.6));
        defaultNutrition.put("carrots", new NutrientInfo(41, 0.9, 10.0, 0.2, 2.8));
        defaultNutrition.put("onion", new NutrientInfo(40, 1.1, 9.3, 0.1, 1.7));
        defaultNutrition.put("bell pepper", new NutrientInfo(31, 1.0, 7.0, 0.3, 2.5));
        defaultNutrition.put("cucumber", new NutrientInfo(16, 0.7, 4.0, 0.1, 0.5));
        defaultNutrition.put("potato", new NutrientInfo(77, 2.0, 17.0, 0.1, 2.2));
        defaultNutrition.put("corn", new NutrientInfo(86, 3.3, 19.0, 1.4, 2.7));
        
        // Fruits
        defaultNutrition.put("apple", new NutrientInfo(52, 0.3, 14.0, 0.2, 2.4));
        defaultNutrition.put("apples", new NutrientInfo(52, 0.3, 14.0, 0.2, 2.4));
        defaultNutrition.put("banana", new NutrientInfo(89, 1.1, 23.0, 0.3, 2.6));
        defaultNutrition.put("bananas", new NutrientInfo(89, 1.1, 23.0, 0.3, 2.6));
        defaultNutrition.put("orange", new NutrientInfo(47, 0.9, 12.0, 0.1, 2.4));
        defaultNutrition.put("berries", new NutrientInfo(57, 0.7, 14.0, 0.3, 2.4));
        defaultNutrition.put("strawberries", new NutrientInfo(32, 0.7, 7.7, 0.3, 2.0));
        defaultNutrition.put("grapes", new NutrientInfo(62, 0.6, 16.0, 0.2, 0.9));
        
        // Nuts and seeds
        defaultNutrition.put("almonds", new NutrientInfo(579, 21.0, 22.0, 50.0, 12.0));
        defaultNutrition.put("walnuts", new NutrientInfo(654, 15.0, 14.0, 65.0, 6.7));
        defaultNutrition.put("peanuts", new NutrientInfo(567, 26.0, 16.0, 49.0, 8.5));
        defaultNutrition.put("sunflower seeds", new NutrientInfo(584, 20.8, 20.0, 51.5, 8.6));
        
        // Oils and fats
        defaultNutrition.put("olive oil", new NutrientInfo(884, 0.0, 0.0, 100.0, 0.0));
        defaultNutrition.put("oil", new NutrientInfo(884, 0.0, 0.0, 100.0, 0.0));
        defaultNutrition.put("vegetable oil", new NutrientInfo(884, 0.0, 0.0, 100.0, 0.0));
        
        // Legumes
        defaultNutrition.put("beans", new NutrientInfo(347, 22.0, 63.0, 1.2, 15.0));
        defaultNutrition.put("lentils", new NutrientInfo(353, 25.0, 60.0, 1.1, 10.7));
        defaultNutrition.put("chickpeas", new NutrientInfo(378, 20.0, 63.0, 6.0, 12.0));
        defaultNutrition.put("black beans", new NutrientInfo(341, 21.6, 62.4, 1.4, 15.0));
        
        // Add all default data to the cache (only if not already present from CNF)
        for (Map.Entry<String, NutrientInfo> entry : defaultNutrition.entrySet()) {
            if (!nutritionCache.containsKey(entry.getKey())) {
                nutritionCache.put(entry.getKey(), entry.getValue());
                foodNameToIdMap.put(entry.getKey(), "default_" + entry.getKey().hashCode());
            }
        }
        
        System.out.println("Added " + defaultNutrition.size() + " default nutrition entries");
    }
    
    @Override
    public NutrientInfo lookupIngredient(String ingredient) {
        if (!initialized) {
            initializeData();
        }
        
        if (ingredient == null || ingredient.trim().isEmpty()) {
            return new NutrientInfo(0, 0, 0, 0, 0);
        }
        
        String normalizedIngredient = ingredient.toLowerCase().trim();
        
        // Try exact match first
        NutrientInfo result = nutritionCache.get(normalizedIngredient);
        if (result != null) {
            return result;
        }
        
        // Try partial matches
        for (Map.Entry<String, NutrientInfo> entry : nutritionCache.entrySet()) {
            if (entry.getKey().contains(normalizedIngredient) || 
                normalizedIngredient.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Return minimal default if nothing found
        System.out.println("Warning: No nutrition data found for ingredient: " + ingredient);
        return new NutrientInfo(50, 2.0, 5.0, 1.0, 0.5);
    }
    
    @Override
    public boolean ingredientExists(String ingredient) {
        if (!initialized) {
            initializeData();
        }
        
        return ingredient != null && nutritionCache.containsKey(ingredient.toLowerCase().trim());
    }
    
    @Override
    public List<String> searchIngredients(String partialName) {
        if (!initialized) {
            initializeData();
        }
        
        List<String> results = new ArrayList<>();
        if (partialName == null || partialName.trim().isEmpty()) {
            return results;
        }
        
        String searchTerm = partialName.toLowerCase().trim();
        
        for (String ingredient : nutritionCache.keySet()) {
            if (ingredient.contains(searchTerm)) {
                results.add(ingredient);
            }
        }
        
        // Sort results by relevance (exact matches first, then partial matches)
        results.sort((a, b) -> {
            boolean aExact = a.startsWith(searchTerm);
            boolean bExact = b.startsWith(searchTerm);
            if (aExact && !bExact) return -1;
            if (!aExact && bExact) return 1;
            return a.compareTo(b);
        });
        
        return results;
    }
    
    @Override
    public List<String> getAllIngredients() {
        if (!initialized) {
            initializeData();
        }
        
        List<String> ingredients = new ArrayList<>(nutritionCache.keySet());
        Collections.sort(ingredients);
        return ingredients;
    }
    
    /**
     * Get ingredients by food group
     */
    public List<String> getIngredientsByGroup(String groupName) {
        if (!initialized) {
            initializeData();
        }
        
        List<String> results = new ArrayList<>();
        
        // Find group ID by name
        Integer targetGroupId = null;
        for (Map.Entry<Integer, String> entry : foodGroups.entrySet()) {
            if (entry.getValue().toLowerCase().contains(groupName.toLowerCase())) {
                targetGroupId = entry.getKey();
                break;
            }
        }
        
        if (targetGroupId != null) {
            for (Map.Entry<String, Integer> entry : foodToGroupMap.entrySet()) {
                if (entry.getValue().equals(targetGroupId)) {
                    String foodName = entry.getKey();
                    if (nutritionCache.containsKey(foodName)) {
                        results.add(foodName);
                    }
                }
            }
        }
        
        Collections.sort(results);
        return results;
    }
    
    /**
     * Get all food groups
     */
    public List<String> getAllFoodGroups() {
        if (!initialized) {
            initializeData();
        }
        
        List<String> groups = new ArrayList<>(foodGroups.values());
        Collections.sort(groups);
        return groups;
    }
    
    @Override
    public List<NutrientInfo> lookupMultipleIngredients(List<String> ingredientNames) {
        if (!initialized) {
            initializeData();
        }
        
        List<NutrientInfo> results = new ArrayList<>();
        
        for (String ingredient : ingredientNames) {
            NutrientInfo nutrientInfo = lookupIngredient(ingredient);
            results.add(nutrientInfo);
        }
        
        return results;
    }
    
    @Override
    public String findClosestMatch(String ingredientName) {
        if (!initialized) {
            initializeData();
        }
        
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            return null;
        }
        
        String searchTerm = ingredientName.toLowerCase().trim();
        
        // First try exact match
        if (nutritionCache.containsKey(searchTerm)) {
            return searchTerm;
        }
        
        // Then try partial matches
        for (String ingredient : nutritionCache.keySet()) {
            if (ingredient.contains(searchTerm) || searchTerm.contains(ingredient)) {
                return ingredient;
            }
        }
        
        return null;
    }
    
    @Override
    public void initialize() {
        initializeData();
    }
    
    @Override
    public boolean isAvailable() {
        return initialized && !nutritionCache.isEmpty();
    }
} 