package ca.nutrisci.application.dto;

/**
 * NutrientInfo - Data transfer object for nutritional information
 * Contains key nutritional values for meals and food items
 */
public class NutrientInfo {
    private double calories;
    private double protein;  // in grams
    private double carbs;    // in grams
    private double fat;      // in grams
    private double fiber;    // in grams
    private double sugar;    // in grams

    // Default constructor
    public NutrientInfo() {}

    // Full constructor
    public NutrientInfo(double calories, double protein, double carbs, double fat, double fiber, double sugar) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
    }

    // Getters and setters
    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getFiber() {
        return fiber;
    }

    public void setFiber(double fiber) {
        this.fiber = fiber;
    }

    public double getSugar() {
        return sugar;
    }

    public void setSugar(double sugar) {
        this.sugar = sugar;
    }

    // Helper methods
    public NutrientInfo add(NutrientInfo other) {
        return new NutrientInfo(
            this.calories + other.calories,
            this.protein + other.protein,
            this.carbs + other.carbs,
            this.fat + other.fat,
            this.fiber + other.fiber,
            this.sugar + other.sugar
        );
    }

    public NutrientInfo multiply(double factor) {
        return new NutrientInfo(
            this.calories * factor,
            this.protein * factor,
            this.carbs * factor,
            this.fat * factor,
            this.fiber * factor,
            this.sugar * factor
        );
    }

    @Override
    public String toString() {
        return String.format("NutrientInfo{calories=%.1f, protein=%.1fg, carbs=%.1fg, fat=%.1fg, fiber=%.1fg, sugar=%.1fg}",
                calories, protein, carbs, fat, fiber, sugar);
    }
} 