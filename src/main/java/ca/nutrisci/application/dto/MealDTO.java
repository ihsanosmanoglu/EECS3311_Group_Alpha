package ca.nutrisci.application.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MealDTO - Data transfer object for meal information
 * Contains meal details including ingredients, quantities, and nutritional information
 */
public class MealDTO {
    private UUID id;
    private UUID profileId;
    private LocalDate date;
    private String mealType; // breakfast, lunch, dinner, snack
    private List<IngredientDTO> ingredients;
    private NutrientInfo nutrients;

    // Default constructor
    public MealDTO() {}

    // Full constructor
    public MealDTO(UUID id, UUID profileId, LocalDate date, String mealType, 
                   List<IngredientDTO> ingredients, NutrientInfo nutrients) {
        this.id = id;
        this.profileId = profileId;
        this.date = date;
        this.mealType = mealType;
        this.ingredients = ingredients;
        this.nutrients = nutrients;
    }
    
    // Backward compatibility constructor
    public MealDTO(UUID id, UUID profileId, LocalDate date, String mealType, 
                   List<String> ingredientNames, List<Double> quantities, NutrientInfo nutrients) {
        this.id = id;
        this.profileId = profileId;
        this.date = date;
        this.mealType = mealType;
        this.nutrients = nutrients;
        
        // Convert to IngredientDTO list
        this.ingredients = new ArrayList<>();
        if (ingredientNames != null) {
            for (int i = 0; i < ingredientNames.size(); i++) {
                String name = ingredientNames.get(i);
                double quantity = (quantities != null && i < quantities.size()) ? quantities.get(i) : 100.0;
                this.ingredients.add(new IngredientDTO(name, quantity, "g")); // Default to grams
            }
        }
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

    public List<IngredientDTO> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientDTO> ingredients) {
        this.ingredients = ingredients;
    }

    // Backward compatibility method
    public List<Double> getQuantities() {
        List<Double> quantities = new ArrayList<>();
        if (ingredients != null) {
            for (IngredientDTO ingredient : ingredients) {
                quantities.add(ingredient.getQuantity());
            }
        }
        return quantities;
    }
    
    // Backward compatibility method
    public List<String> getIngredientNames() {
        List<String> names = new ArrayList<>();
        if (ingredients != null) {
            for (IngredientDTO ingredient : ingredients) {
                names.add(ingredient.getName());
            }
        }
        return names;
    }

    public NutrientInfo getNutrients() {
        return nutrients;
    }

    public void setNutrients(NutrientInfo nutrients) {
        this.nutrients = nutrients;
    }

    @Override
    public String toString() {
        return String.format("MealDTO{id=%s, profileId=%s, date=%s, mealType='%s', ingredients=%s, nutrients=%s}",
                id, profileId, date, mealType, ingredients, nutrients);
    }
} 