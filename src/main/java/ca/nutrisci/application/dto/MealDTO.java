package ca.nutrisci.application.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MealDTO - Data Transfer Object for meal information
 * Part of the Application Layer
 */
public class MealDTO {
    
    private UUID id;
    private UUID profileId;
    private LocalDate date;
    private String mealType; // "breakfast", "lunch", "dinner", "snack"
    private List<String> ingredients;
    private List<Double> quantities; // in grams
    private NutrientInfo nutrients;
    
    // Default constructor
    public MealDTO() {
        this.id = UUID.randomUUID();
        this.ingredients = new ArrayList<>();
        this.quantities = new ArrayList<>();
        this.nutrients = new NutrientInfo();
        this.date = LocalDate.now();
    }
    
    // Constructor for new meal
    public MealDTO(UUID profileId, String mealType, LocalDate date) {
        this();
        this.profileId = profileId;
        this.mealType = mealType;
        this.date = date;
    }
    
    // Full constructor
    public MealDTO(UUID id, UUID profileId, LocalDate date, String mealType, 
                   List<String> ingredients, List<Double> quantities, NutrientInfo nutrients) {
        this.id = id;
        this.profileId = profileId;
        this.date = date;
        this.mealType = mealType;
        this.ingredients = new ArrayList<>(ingredients);
        this.quantities = new ArrayList<>(quantities);
        this.nutrients = nutrients;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    
    public List<String> getIngredients() { return new ArrayList<>(ingredients); }
    public void setIngredients(List<String> ingredients) { 
        this.ingredients = new ArrayList<>(ingredients); 
    }
    
    public List<Double> getQuantities() { return new ArrayList<>(quantities); }
    public void setQuantities(List<Double> quantities) { 
        this.quantities = new ArrayList<>(quantities); 
    }
    
    public NutrientInfo getNutrients() { return nutrients; }
    public void setNutrients(NutrientInfo nutrients) { this.nutrients = nutrients; }
    
    // Meal management methods
    public void addIngredient(String ingredient, double quantity) {
        ingredients.add(ingredient);
        quantities.add(quantity);
    }
    
    public void removeIngredient(int index) {
        if (index >= 0 && index < ingredients.size()) {
            ingredients.remove(index);
            quantities.remove(index);
        }
    }
    
    public void updateIngredient(int index, String ingredient, double quantity) {
        if (index >= 0 && index < ingredients.size()) {
            ingredients.set(index, ingredient);
            quantities.set(index, quantity);
        }
    }
    
    public int getIngredientCount() {
        return ingredients.size();
    }
    
    public double getTotalQuantity() {
        return quantities.stream().mapToDouble(Double::doubleValue).sum();
    }
    
    // Validation
    public boolean isValid() {
        return profileId != null && 
               mealType != null && !mealType.trim().isEmpty() &&
               date != null &&
               ingredients.size() == quantities.size() &&
               ingredients.size() > 0;
    }
    
    public static boolean isValidMealType(String mealType) {
        return "breakfast".equalsIgnoreCase(mealType) ||
               "lunch".equalsIgnoreCase(mealType) ||
               "dinner".equalsIgnoreCase(mealType) ||
               "snack".equalsIgnoreCase(mealType);
    }
    
    @Override
    public String toString() {
        return String.format("MealDTO{id=%s, mealType='%s', date=%s, ingredients=%d, calories=%.1f}",
                id, mealType, date, ingredients.size(), 
                nutrients != null ? nutrients.getCalories() : 0.0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MealDTO mealDTO = (MealDTO) obj;
        return id != null ? id.equals(mealDTO.id) : mealDTO.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 