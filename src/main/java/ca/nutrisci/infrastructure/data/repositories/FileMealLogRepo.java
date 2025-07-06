package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * FileMealLogRepo - File-based implementation of MealLogRepo
 * Stores meal data in CSV files with JSON serialization for complex fields
 */
public class FileMealLogRepo implements MealLogRepo {
    
    private final String filePath;
    private final DateTimeFormatter dateFormatter;
    
    public FileMealLogRepo(String filePath) {
        this.filePath = filePath;
        this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        ensureFileExists();
    }
    
    /**
     * Clear all meal data - useful for testing or recovering from corrupted files
     */
    public void clearAllData() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            ensureFileExists();
            System.out.println("✅ Cleared all meal data from " + filePath);
        } catch (Exception e) {
            System.err.println("❌ Error clearing meal data: " + e.getMessage());
        }
    }
    
    private void ensureFileExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
                // Write header
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("id,profileId,date,mealType,ingredients,quantities,nutrients");
                }
            } catch (IOException e) {
                System.err.println("Error creating meal log file: " + e.getMessage());
            }
        }
    }
    
    @Override
    public List<MealDTO> getMealLogHistory(UUID profileId) {
        List<MealDTO> allMeals = loadAllMeals();
        return allMeals.stream()
            .filter(meal -> meal.getProfileId().equals(profileId))
            .sorted(Comparator.comparing(MealDTO::getDate).reversed())
            .collect(Collectors.toList());
    }
    
    @Override
    public List<MealDTO> getMealsByTimeInterval(UUID profileId, LocalDate startDate, LocalDate endDate) {
        List<MealDTO> allMeals = loadAllMeals();
        return allMeals.stream()
            .filter(meal -> meal.getProfileId().equals(profileId))
            .filter(meal -> !meal.getDate().isBefore(startDate) && !meal.getDate().isAfter(endDate))
            .sorted(Comparator.comparing(MealDTO::getDate))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<MealDTO> getMealsByDate(UUID profileId, LocalDate date) {
        List<MealDTO> allMeals = loadAllMeals();
        return allMeals.stream()
            .filter(meal -> meal.getProfileId().equals(profileId))
            .filter(meal -> meal.getDate().equals(date))
            .collect(Collectors.toList());
    }
    
    @Override
    public MealDTO getSingleMealById(UUID mealId) {
        List<MealDTO> allMeals = loadAllMeals();
        return allMeals.stream()
            .filter(meal -> meal.getId().equals(mealId))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public MealDTO addMeal(MealDTO meal) {
        if (meal.getId() == null) {
            meal.setId(UUID.randomUUID());
        }
        
        List<MealDTO> allMeals = loadAllMeals();
        allMeals.add(meal);
        saveMeals(allMeals);
        
        return meal;
    }
    
    @Override
    public MealDTO editMeal(UUID mealId, MealDTO updatedMeal) {
        List<MealDTO> allMeals = loadAllMeals();
        
        for (int i = 0; i < allMeals.size(); i++) {
            if (allMeals.get(i).getId().equals(mealId)) {
                updatedMeal.setId(mealId);
                allMeals.set(i, updatedMeal);
                saveMeals(allMeals);
                return updatedMeal;
            }
        }
        
        throw new IllegalArgumentException("Meal not found: " + mealId);
    }
    
    @Override
    public void deleteMeal(UUID mealId) {
        List<MealDTO> allMeals = loadAllMeals();
        boolean removed = allMeals.removeIf(meal -> meal.getId().equals(mealId));
        
        if (removed) {
            saveMeals(allMeals);
        } else {
            throw new IllegalArgumentException("Meal not found: " + mealId);
        }
    }
    
    @Override
    public boolean mealExists(UUID mealId) {
        List<MealDTO> allMeals = loadAllMeals();
        return allMeals.stream()
            .anyMatch(meal -> meal.getId().equals(mealId));
    }
    
    @Override
    public List<MealDTO> getMealsByTypeAndDate(UUID profileId, LocalDate date, String mealType) {
        List<MealDTO> allMeals = loadAllMeals();
        return allMeals.stream()
            .filter(meal -> meal.getProfileId().equals(profileId))
            .filter(meal -> meal.getDate().equals(date))
            .filter(meal -> meal.getMealType().equalsIgnoreCase(mealType))
            .collect(Collectors.toList());
    }
    
    private List<MealDTO> loadAllMeals() {
        List<MealDTO> meals = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                MealDTO meal = parseMealFromCSV(line);
                if (meal != null) {
                    meals.add(meal);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading meal log file: " + e.getMessage());
        }
        
        return meals;
    }
    
    private void saveMeals(List<MealDTO> meals) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("id,profileId,date,mealType,ingredients,quantities,nutrients");
            
            for (MealDTO meal : meals) {
                writer.println(formatMealToCSV(meal));
            }
        } catch (IOException e) {
            System.err.println("Error writing meal log file: " + e.getMessage());
        }
    }
    
    private MealDTO parseMealFromCSV(String line) {
        try {
            // Simple CSV parsing with quoted fields
            String[] parts = parseCSVLine(line);
            if (parts.length != 7) {
                System.err.println("Invalid CSV line format: " + line);
                return null;
            }
            
            UUID id = UUID.fromString(parts[0]);
            UUID profileId = UUID.fromString(parts[1]);
            LocalDate date = LocalDate.parse(parts[2], dateFormatter);
            String mealType = parts[3];
            
            // Parse ingredients and quantities using pipe delimiter
            List<String> ingredients = Arrays.asList(parts[4].split("\\|"));
            List<Double> quantities = Arrays.stream(parts[5].split("\\|"))
                .map(Double::parseDouble)
                .collect(Collectors.toList());
            
            // Parse nutrition info
            String[] nutritionParts = parts[6].split(",");
            NutrientInfo nutrients = new NutrientInfo(
                Double.parseDouble(nutritionParts[0]), // calories
                Double.parseDouble(nutritionParts[1]), // protein
                Double.parseDouble(nutritionParts[2]), // carbs
                Double.parseDouble(nutritionParts[3]), // fat
                Double.parseDouble(nutritionParts[4])  // fiber
            );
            
            return new MealDTO(id, profileId, date, mealType, ingredients, quantities, nutrients);
            
        } catch (Exception e) {
            System.err.println("Error parsing meal from CSV: " + e.getMessage());
            return null;
        }
    }
    
    // Simple CSV parsing helper
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
    
    private String formatMealToCSV(MealDTO meal) {
        // Simplified CSV format - use pipe delimiters for lists (KISS principle)
        String ingredientsStr = String.join("|", meal.getIngredients());
        String quantitiesStr = meal.getQuantities().stream()
            .map(String::valueOf)
            .collect(Collectors.joining("|"));
        
        // Simple nutrition format
        NutrientInfo nutrients = meal.getNutrients();
        String nutrientsStr = String.format("%.1f,%.1f,%.1f,%.1f,%.1f",
            nutrients.getCalories(), nutrients.getProtein(), nutrients.getCarbs(),
            nutrients.getFat(), nutrients.getFiber());
        
        return String.format("%s,%s,%s,%s,\"%s\",\"%s\",\"%s\"",
            meal.getId().toString(),
            meal.getProfileId().toString(),
            meal.getDate().format(dateFormatter),
            meal.getMealType(),
            ingredientsStr,
            quantitiesStr,
            nutrientsStr);
    }
    
    /**
     * Custom LocalDate adapter for Gson
     */
    private static class LocalDateAdapter implements com.google.gson.JsonSerializer<LocalDate>, com.google.gson.JsonDeserializer<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        
        @Override
        public com.google.gson.JsonElement serialize(LocalDate src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.format(formatter));
        }
        
        @Override
        public LocalDate deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            return LocalDate.parse(json.getAsString(), formatter);
        }
    }
} 