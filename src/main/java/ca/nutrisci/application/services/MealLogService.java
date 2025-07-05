package ca.nutrisci.application.services;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.services.observers.MealLogListener;
import ca.nutrisci.domain.entities.Meal;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MealLogService - Business logic for meal logging operations
 * Part of the Application Layer - Observer Pattern Subject
 */
public class MealLogService {
    
    private INutritionGateway nutritionGateway;
    private List<MealLogListener> listeners;
    
    public MealLogService(INutritionGateway nutritionGateway) {
        this.nutritionGateway = nutritionGateway;
        this.listeners = new ArrayList<>();
    }
    
    /**
     * Validate meal data
     */
    public boolean validateMeal(MealDTO mealDTO) {
        if (mealDTO == null) return false;
        if (mealDTO.getProfileId() == null) return false;
        if (mealDTO.getMealType() == null || mealDTO.getMealType().trim().isEmpty()) return false;
        if (mealDTO.getDate() == null) return false;
        if (mealDTO.getIngredients().isEmpty()) return false;
        if (mealDTO.getIngredients().size() != mealDTO.getQuantities().size()) return false;
        
        // Check meal type is valid
        if (!MealDTO.isValidMealType(mealDTO.getMealType())) return false;
        
        // Check quantities are positive
        for (Double quantity : mealDTO.getQuantities()) {
            if (quantity == null || quantity <= 0) return false;
        }
        
        return true;
    }
    
    /**
     * Calculate nutrients for a meal
     */
    public MealDTO calculateNutrients(MealDTO mealDTO, INutritionGateway gateway) {
        if (!validateMeal(mealDTO)) return mealDTO;
        
        NutrientInfo totalNutrients = new NutrientInfo();
        List<String> ingredients = mealDTO.getIngredients();
        List<Double> quantities = mealDTO.getQuantities();
        
        for (int i = 0; i < ingredients.size(); i++) {
            String ingredient = ingredients.get(i);
            double quantity = quantities.get(i);
            
            // Get nutrition info for this ingredient
            NutrientInfo ingredientNutrition = gateway.lookupIngredient(ingredient);
            
            // Scale by quantity (assuming quantity is in grams, nutrition per 100g)
            NutrientInfo scaledNutrition = ingredientNutrition.multiply(quantity / 100.0);
            
            // Add to total
            totalNutrients = totalNutrients.add(scaledNutrition);
        }
        
        // Update meal with calculated nutrients
        mealDTO.setNutrients(totalNutrients);
        return mealDTO;
    }
    
    /**
     * Enforce one meal per type rule (except snacks)
     */
    public void enforceOneMealPerType(MealDTO mealDTO) {
        if (mealDTO == null || "snack".equalsIgnoreCase(mealDTO.getMealType())) {
            return; // Snacks are allowed multiple times per day
        }
        
        // This would typically check existing meals in the repository
        // For now, we'll just validate the meal type
        String mealType = mealDTO.getMealType().toLowerCase();
        if (!mealType.equals("breakfast") && !mealType.equals("lunch") && !mealType.equals("dinner")) {
            throw new IllegalArgumentException("Invalid meal type: " + mealType);
        }
    }
    
    /**
     * Enrich ingredients list with nutritional information
     */
    public NutrientInfo enrichWithNutrients(List<String> ingredients, INutritionGateway gateway) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new NutrientInfo();
        }
        
        NutrientInfo totalNutrients = new NutrientInfo();
        
        for (String ingredient : ingredients) {
            NutrientInfo ingredientNutrition = gateway.lookupIngredient(ingredient);
            totalNutrients = totalNutrients.add(ingredientNutrition);
        }
        
        return totalNutrients;
    }
    
    /**
     * Calculate daily totals for a profile
     */
    public NutrientInfo calculateDailyTotals(List<MealDTO> mealsForDay) {
        if (mealsForDay == null || mealsForDay.isEmpty()) {
            return new NutrientInfo();
        }
        
        NutrientInfo dailyTotal = new NutrientInfo();
        
        for (MealDTO meal : mealsForDay) {
            if (meal.getNutrients() != null) {
                dailyTotal = dailyTotal.add(meal.getNutrients());
            }
        }
        
        return dailyTotal;
    }
    
    /**
     * Get meal recommendations based on time and existing meals
     */
    public String getMealRecommendation(UUID profileId, String mealType, LocalDate date) {
        String type = mealType.toLowerCase();
        
        switch (type) {
            case "breakfast":
                return "Include whole grains, protein, and fruits for sustained energy";
            case "lunch":
                return "Balance proteins, vegetables, and complex carbohydrates";
            case "dinner":
                return "Focus on lean proteins and vegetables, lighter on carbs";
            case "snack":
                return "Choose nutrient-dense options like fruits, nuts, or yogurt";
            default:
                return "Aim for balanced nutrition with variety of food groups";
        }
    }
    
    /**
     * Convert domain entity to DTO
     */
    public MealDTO toDTO(Meal meal) {
        if (meal == null) return null;
        
        return new MealDTO(
            meal.getId(),
            meal.getProfileId(),
            meal.getDate(),
            meal.getMealType(),
            meal.getIngredients(),
            meal.getQuantities(),
            meal.getNutrients()
        );
    }
    
    /**
     * Convert DTO to domain entity
     */
    public Meal fromDTO(MealDTO dto) {
        if (dto == null) return null;
        
        return new Meal(
            dto.getId(),
            dto.getProfileId(),
            dto.getDate(),
            dto.getMealType(),
            dto.getIngredients(),
            dto.getQuantities(),
            dto.getNutrients(),
            null // createdAt will be set by entity
        );
    }
    
    // Observer pattern methods
    
    /**
     * Add listener for meal log events
     */
    public void addListener(MealLogListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove listener
     */
    public void removeListener(MealLogListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners when meal is logged
     */
    public void notifyMealLogged(MealDTO meal) {
        for (MealLogListener listener : listeners) {
            try {
                listener.onMealLogged(meal);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process meal logging with notifications
     */
    public MealDTO processMealLogging(MealDTO meal) {
        // Validate meal
        if (!validateMeal(meal)) {
            throw new IllegalArgumentException("Invalid meal data");
        }
        
        // Calculate nutrients
        MealDTO enrichedMeal = calculateNutrients(meal, nutritionGateway);
        
        // Enforce business rules
        enforceOneMealPerType(enrichedMeal);
        
        // Notify listeners (for background calculations)
        notifyMealLogged(enrichedMeal);
        
        return enrichedMeal;
    }
} 