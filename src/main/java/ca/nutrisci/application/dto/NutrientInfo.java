package ca.nutrisci.application.dto;

/**
 * NutrientInfo - Data Transfer Object for nutrient information
 * Part of the Application Layer
 */
public class NutrientInfo {
    
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private double fiber;
    private double sodium;
    private double sugar;
    private double calcium;
    private double iron;
    private double vitaminC;
    
    // Default constructor
    public NutrientInfo() {
        this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
    
    // Full constructor
    public NutrientInfo(double calories, double protein, double carbs, double fat, double fiber,
                       double sodium, double sugar, double calcium, double iron, double vitaminC) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sodium = sodium;
        this.sugar = sugar;
        this.calcium = calcium;
        this.iron = iron;
        this.vitaminC = vitaminC;
    }
    
    // Core nutrients constructor (for basic calculations)
    public NutrientInfo(double calories, double protein, double carbs, double fat, double fiber) {
        this(calories, protein, carbs, fat, fiber, 0.0, 0.0, 0.0, 0.0, 0.0);
    }
    
    // Getters and Setters
    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }
    
    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }
    
    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    
    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
    
    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }
    
    public double getSodium() { return sodium; }
    public void setSodium(double sodium) { this.sodium = sodium; }
    
    public double getSugar() { return sugar; }
    public void setSugar(double sugar) { this.sugar = sugar; }
    
    public double getCalcium() { return calcium; }
    public void setCalcium(double calcium) { this.calcium = calcium; }
    
    public double getIron() { return iron; }
    public void setIron(double iron) { this.iron = iron; }
    
    public double getVitaminC() { return vitaminC; }
    public void setVitaminC(double vitaminC) { this.vitaminC = vitaminC; }
    
    // Utility methods for nutrient calculations
    public NutrientInfo add(NutrientInfo other) {
        return new NutrientInfo(
            this.calories + other.calories,
            this.protein + other.protein,
            this.carbs + other.carbs,
            this.fat + other.fat,
            this.fiber + other.fiber,
            this.sodium + other.sodium,
            this.sugar + other.sugar,
            this.calcium + other.calcium,
            this.iron + other.iron,
            this.vitaminC + other.vitaminC
        );
    }
    
    public NutrientInfo subtract(NutrientInfo other) {
        return new NutrientInfo(
            this.calories - other.calories,
            this.protein - other.protein,
            this.carbs - other.carbs,
            this.fat - other.fat,
            this.fiber - other.fiber,
            this.sodium - other.sodium,
            this.sugar - other.sugar,
            this.calcium - other.calcium,
            this.iron - other.iron,
            this.vitaminC - other.vitaminC
        );
    }
    
    public NutrientInfo multiply(double factor) {
        return new NutrientInfo(
            this.calories * factor,
            this.protein * factor,
            this.carbs * factor,
            this.fat * factor,
            this.fiber * factor,
            this.sodium * factor,
            this.sugar * factor,
            this.calcium * factor,
            this.iron * factor,
            this.vitaminC * factor
        );
    }
    
    @Override
    public String toString() {
        return String.format("NutrientInfo{calories=%.1f, protein=%.1fg, carbs=%.1fg, fat=%.1fg, fiber=%.1fg}",
                calories, protein, carbs, fat, fiber);
    }
} 