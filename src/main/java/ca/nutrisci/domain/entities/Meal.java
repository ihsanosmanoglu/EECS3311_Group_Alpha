package ca.nutrisci.domain.entities;

import ca.nutrisci.application.dto.NutrientInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Meal - Domain entity for meal information
 * Part of the Domain Layer
 */
public class Meal {
    
    private UUID id;
    private UUID profileId;
    private LocalDate date;
    private String mealType;
    private List<String> ingredients;
    private List<Double> quantities;
    private NutrientInfo nutrients;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    
    // Valid meal types
    public static final String BREAKFAST = "breakfast";
    public static final String LUNCH = "lunch";
    public static final String DINNER = "dinner";
    public static final String SNACK = "snack";
    
    // Default constructor
    public Meal() {
        this.id = UUID.randomUUID();
        this.ingredients = new ArrayList<>();
        this.quantities = new ArrayList<>();
        this.nutrients = new NutrientInfo();
        this.date = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    // Constructor for new meal
    public Meal(UUID profileId, String mealType, LocalDate date) {
        this();
        this.profileId = profileId;
        this.mealType = mealType;
        this.date = date;
        validateMeal();
    }
    
    // Full constructor
    public Meal(UUID id, UUID profileId, LocalDate date, String mealType, 
                List<String> ingredients, List<Double> quantities, NutrientInfo nutrients,
                LocalDateTime createdAt) {
        this.id = id;
        this.profileId = profileId;
        this.date = date;
        this.mealType = mealType;
        this.ingredients = new ArrayList<>(ingredients);
        this.quantities = new ArrayList<>(quantities);
        this.nutrients = nutrients;
        this.createdAt = createdAt;
        this.lastModified = LocalDateTime.now();
        validateMeal();
    }
    
    // Business Logic Methods
    
    /**
     * Add an ingredient to the meal
     */
    public void addIngredient(String ingredient, double quantity) {
        if (ingredient == null || ingredient.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        ingredients.add(ingredient.trim());
        quantities.add(quantity);
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Remove an ingredient from the meal
     */
    public void removeIngredient(int index) {
        if (index < 0 || index >= ingredients.size()) {
            throw new IndexOutOfBoundsException("Invalid ingredient index");
        }
        
        ingredients.remove(index);
        quantities.remove(index);
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Update an existing ingredient
     */
    public void updateIngredient(int index, String ingredient, double quantity) {
        if (index < 0 || index >= ingredients.size()) {
            throw new IndexOutOfBoundsException("Invalid ingredient index");
        }
        if (ingredient == null || ingredient.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        ingredients.set(index, ingredient.trim());
        quantities.set(index, quantity);
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Clear all ingredients
     */
    public void clearIngredients() {
        ingredients.clear();
        quantities.clear();
        this.nutrients = new NutrientInfo();
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Get total quantity of all ingredients
     */
    public double getTotalQuantity() {
        return quantities.stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Check if ingredient already exists in meal
     */
    public boolean containsIngredient(String ingredient) {
        return ingredients.stream()
                .anyMatch(ing -> ing.equalsIgnoreCase(ingredient.trim()));
    }
    
    /**
     * Get ingredient quantity by name
     */
    public double getIngredientQuantity(String ingredient) {
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).equalsIgnoreCase(ingredient.trim())) {
                return quantities.get(i);
            }
        }
        return 0.0;
    }
    
    /**
     * Update nutritional information for the meal
     */
    public void updateNutrients(NutrientInfo nutrients) {
        if (nutrients == null) {
            throw new IllegalArgumentException("Nutrients cannot be null");
        }
        this.nutrients = nutrients;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Check if this meal conflicts with another meal on the same day
     * Business rule: Only one meal of each type (except snacks) per day
     */
    public boolean conflictsWith(Meal otherMeal) {
        if (otherMeal == null || !this.date.equals(otherMeal.date) || 
            !this.profileId.equals(otherMeal.profileId)) {
            return false;
        }
        
        // Snacks don't conflict with each other
        if (SNACK.equals(this.mealType) && SNACK.equals(otherMeal.mealType)) {
            return false;
        }
        
        // Other meal types conflict if they're the same type
        return this.mealType.equals(otherMeal.mealType);
    }
    
    /**
     * Check if this is a main meal (not a snack)
     */
    public boolean isMainMeal() {
        return BREAKFAST.equals(mealType) || LUNCH.equals(mealType) || DINNER.equals(mealType);
    }
    
    /**
     * Calculate meal score based on nutritional balance
     * Simple scoring system for educational purposes
     */
    public double calculateNutritionalScore() {
        if (nutrients == null) return 0.0;
        
        double score = 0.0;
        double totalCalories = nutrients.getCalories();
        
        if (totalCalories > 0) {
            // Balanced macronutrient ratios
            double proteinPercent = (nutrients.getProtein() * 4) / totalCalories * 100;
            double carbPercent = (nutrients.getCarbs() * 4) / totalCalories * 100;
            double fatPercent = (nutrients.getFat() * 9) / totalCalories * 100;
            
            // Score based on recommended ratios (protein: 15-25%, carbs: 45-65%, fat: 20-35%)
            if (proteinPercent >= 15 && proteinPercent <= 25) score += 25;
            if (carbPercent >= 45 && carbPercent <= 65) score += 25;
            if (fatPercent >= 20 && fatPercent <= 35) score += 25;
            
            // Bonus for fiber content
            if (nutrients.getFiber() >= 5) score += 25;
        }
        
        return score;
    }
    
    // Validation Methods
    
    private void validateMeal() {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        if (!isValidMealType(mealType)) {
            throw new IllegalArgumentException("Invalid meal type: " + mealType);
        }
        if (date == null) {
            throw new IllegalArgumentException("Meal date cannot be null");
        }
        if (ingredients.size() != quantities.size()) {
            throw new IllegalStateException("Ingredients and quantities lists must have the same size");
        }
    }
    
    public static boolean isValidMealType(String mealType) {
        return BREAKFAST.equalsIgnoreCase(mealType) ||
               LUNCH.equalsIgnoreCase(mealType) ||
               DINNER.equalsIgnoreCase(mealType) ||
               SNACK.equalsIgnoreCase(mealType);
    }
    
    /**
     * Check if meal has valid data
     */
    public boolean isValid() {
        return profileId != null && 
               mealType != null && isValidMealType(mealType) &&
               date != null &&
               ingredients.size() == quantities.size() &&
               !ingredients.isEmpty();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { 
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        this.profileId = profileId;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { 
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { 
        if (!isValidMealType(mealType)) {
            throw new IllegalArgumentException("Invalid meal type: " + mealType);
        }
        this.mealType = mealType;
        this.lastModified = LocalDateTime.now();
    }
    
    public List<String> getIngredients() { return new ArrayList<>(ingredients); }
    public List<Double> getQuantities() { return new ArrayList<>(quantities); }
    
    public NutrientInfo getNutrients() { return nutrients; }
    
    public int getIngredientCount() { return ingredients.size(); }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastModified() { return lastModified; }
    
    @Override
    public String toString() {
        return String.format("Meal{id=%s, type='%s', date=%s, ingredients=%d, calories=%.1f}",
                id, mealType, date, ingredients.size(), 
                nutrients != null ? nutrients.getCalories() : 0.0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Meal meal = (Meal) obj;
        return id != null ? id.equals(meal.id) : meal.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 