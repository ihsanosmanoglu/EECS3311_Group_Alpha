package ca.nutrisci.application.dto;

import ca.nutrisci.domain.entities.FoodGroup;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.EnumMap;

/**
 * MealDTO - Data transfer object for meal information
 * Contains meal details including ingredients, quantities, and nutritional information
 */
public class MealDTO {
    private UUID id;
    private UUID profileId;
    private LocalDate date;
    private String mealType; // breakfast, lunch, dinner, snack
    private List<String> ingredients;
    private List<Double> quantities;
    private NutrientInfo nutrients;
    private Map<FoodGroup, Double> foodGroupServings;

    // Default constructor
    public MealDTO() {
        this.foodGroupServings = new EnumMap<>(FoodGroup.class);
    }

    // Full constructor
    public MealDTO(UUID id, UUID profileId, LocalDate date, String mealType, 
                   List<String> ingredients, List<Double> quantities, NutrientInfo nutrients) {
        this.id = id;
        this.profileId = profileId;
        this.date = date;
        this.mealType = mealType;
        this.ingredients = ingredients;
        this.quantities = quantities;
        this.nutrients = nutrients;
        this.foodGroupServings = new EnumMap<>(FoodGroup.class);
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Double> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Double> quantities) {
        this.quantities = quantities;
    }

    public NutrientInfo getNutrients() {
        return nutrients;
    }

    public void setNutrients(NutrientInfo nutrients) {
        this.nutrients = nutrients;
    }

    public Map<FoodGroup, Double> getFoodGroupServings() {
        return new EnumMap<>(foodGroupServings);
    }

    public void setFoodGroupServings(Map<FoodGroup, Double> foodGroupServings) {
        this.foodGroupServings = new EnumMap<>(foodGroupServings);
    }

    public void addFoodGroupServing(FoodGroup group, double servings) {
        this.foodGroupServings.merge(group, servings, Double::sum);
    }

    @Override
    public String toString() {
        return String.format("MealDTO{id=%s, profileId=%s, date=%s, mealType='%s', ingredients=%s, quantities=%s, nutrients=%s}",
                id, profileId, date, mealType, ingredients, quantities, nutrients);
    }
} 