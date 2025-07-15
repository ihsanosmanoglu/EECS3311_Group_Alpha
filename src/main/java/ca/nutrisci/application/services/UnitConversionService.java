package ca.nutrisci.application.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UnitConversionService - Handles unit conversions for food measurements
 * 
 * PURPOSE:
 * - Loads conversion factors from Canada Nutrient File (CNF) CSV data
 * - Provides unit conversion between different measurement types (g, ml, cup, slice, etc.)
 * - Converts all measurements to grams for consistent nutrition calculations
 * 
 * DATA SOURCES:
 * - CONVERSION FACTOR.csv: Contains conversion factors for each food-measure combination
 * - MEASURE NAME.csv: Contains readable names for measurement units
 * 
 * @author NutriSci Development Team
 * @version 2.0
 * @since 2.0
 */
public class UnitConversionService {

    private static UnitConversionService instance;
    private final String cnfDataPath;
    private final Map<Integer, String> measureIdToName = new HashMap<>();
    private final Map<Integer, Map<Integer, Double>> foodIdToMeasureFactors = new HashMap<>();
    private boolean isInitialized = false;

    private UnitConversionService(String cnfDataPath) {
        this.cnfDataPath = cnfDataPath;
    }

    /**
     * Get singleton instance of UnitConversionService
     * 
     * @return Singleton instance
     */
    public static synchronized UnitConversionService getInstance() {
        if (instance == null) {
            // Default path to CNF data
            String defaultPath = "Canada Nutrient File-20250622/";
            instance = new UnitConversionService(defaultPath);
        }
        return instance;
    }

    /**
     * Initialize the service by loading CSV data
     * 
     * @throws IOException if CSV files cannot be read
     */
    public synchronized void initialize() throws IOException {
        if (isInitialized) {
            return;
        }

        System.out.println("🔄 Initializing UnitConversionService...");
        
        loadMeasureNames();
        loadConversionFactors();
        
        isInitialized = true;
        System.out.println("✅ UnitConversionService initialized with " + measureIdToName.size() + 
                          " measures and " + foodIdToMeasureFactors.size() + " food conversion factors");
    }

    /**
     * Load measure names from MEASURE NAME.csv
     */
    private void loadMeasureNames() throws IOException {
        String measureFilePath = cnfDataPath + "MEASURE NAME.csv";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(measureFilePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        int measureId = Integer.parseInt(parts[0].trim());
                        String measureName = parts[1].trim().replace("\"", "");
                        measureIdToName.put(measureId, measureName);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing measure ID in line: " + line);
                    }
                }
            }
        }
        
        System.out.println("📋 Loaded " + measureIdToName.size() + " measure definitions");
    }

    /**
     * Load conversion factors from CONVERSION FACTOR.csv
     */
    private void loadConversionFactors() throws IOException {
        String conversionFilePath = cnfDataPath + "CONVERSION FACTOR.csv";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(conversionFilePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        int foodId = Integer.parseInt(parts[0].trim());
                        int measureId = Integer.parseInt(parts[1].trim());
                        double conversionFactor = Double.parseDouble(parts[2].trim());
                        
                        // Store conversion factor: foodId -> (measureId -> factor)
                        foodIdToMeasureFactors
                            .computeIfAbsent(foodId, k -> new HashMap<>())
                            .put(measureId, conversionFactor);
                            
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing conversion factor in line: " + line);
                    }
                }
            }
        }
        
        System.out.println("🔢 Loaded " + foodIdToMeasureFactors.size() + " food conversion factors");
    }

    /**
     * Get available measurement units for a specific food
     * 
     * @param foodId Food ID from the CNF database
     * @return List of available measurement unit names
     */
    public List<String> getAvailableUnitsForFood(int foodId) {
        if (!isInitialized) {
            try {
                initialize();
            } catch (IOException e) {
                System.err.println("Failed to initialize UnitConversionService: " + e.getMessage());
                return getDefaultUnits();
            }
        }

        List<String> units = new ArrayList<>();
        Map<Integer, Double> measureFactors = foodIdToMeasureFactors.get(foodId);
        
        if (measureFactors != null) {
            for (Integer measureId : measureFactors.keySet()) {
                String measureName = measureIdToName.get(measureId);
                if (measureName != null) {
                    units.add(measureName);
                }
            }
        }
        
        // Always include grams as a default option
        if (!units.contains("1 g") && !units.contains("grams")) {
            units.add(0, "1 g");
        }
        
        // If no specific units found, provide common defaults
        if (units.size() <= 1) {
            units.addAll(getDefaultUnits());
        }
        
        return units;
    }

    /**
     * Convert quantity from one unit to grams
     * 
     * @param foodId Food ID from CNF database
     * @param quantity Amount in the source unit
     * @param sourceUnit Source measurement unit
     * @return Equivalent weight in grams
     */
    public double convertToGrams(int foodId, double quantity, String sourceUnit) {
        if (!isInitialized) {
            try {
                initialize();
            } catch (IOException e) {
                System.err.println("Failed to initialize UnitConversionService: " + e.getMessage());
                return quantity; // Assume already in grams
            }
        }

        // If already in grams, return as-is
        if (isGramUnit(sourceUnit)) {
            return quantity;
        }

        // Find the measure ID for the source unit
        Integer measureId = findMeasureIdByName(sourceUnit);
        if (measureId == null) {
            System.err.println("Unknown unit: " + sourceUnit + " for food ID " + foodId + ", assuming grams");
            return quantity;
        }

        // Get conversion factor for this food and measure
        Map<Integer, Double> measureFactors = foodIdToMeasureFactors.get(foodId);
        if (measureFactors == null) {
            System.err.println("No conversion factors found for food ID " + foodId + ", assuming grams");
            return quantity;
        }

        Double conversionFactor = measureFactors.get(measureId);
        if (conversionFactor == null) {
            System.err.println("No conversion factor found for food ID " + foodId + 
                             " and measure " + sourceUnit + ", assuming grams");
            return quantity;
        }

        // Convert: quantity * conversionFactor = grams
        double gramsResult = quantity * conversionFactor;
        System.out.println("🔄 Converted " + quantity + " " + sourceUnit + " to " + 
                          String.format("%.2f", gramsResult) + "g for food ID " + foodId);
        
        return gramsResult;
    }

    /**
     * Check if a unit represents grams
     */
    private boolean isGramUnit(String unit) {
        String lowerUnit = unit.toLowerCase().trim();
        return lowerUnit.equals("g") || lowerUnit.equals("grams") || 
               lowerUnit.equals("1 g") || lowerUnit.equals("gram");
    }

    /**
     * Find measure ID by unit name
     */
    private Integer findMeasureIdByName(String unitName) {
        for (Map.Entry<Integer, String> entry : measureIdToName.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(unitName.trim())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get default units when specific units are not available
     */
    private List<String> getDefaultUnits() {
        List<String> defaults = new ArrayList<>();
        defaults.add("1 g");
        defaults.add("1 ml");
        defaults.add("1 cup");
        defaults.add("1 tbsp");
        defaults.add("1 tsp");
        defaults.add("1 serving");
        defaults.add("1 slice");
        defaults.add("1 piece");
        return defaults;
    }

    /**
     * Get status information for debugging
     */
    public String getStatus() {
        return String.format("UnitConversionService: initialized=%s, measures=%d, foods=%d", 
                           isInitialized, measureIdToName.size(), foodIdToMeasureFactors.size());
    }

    /**
     * Check if the service is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
} 