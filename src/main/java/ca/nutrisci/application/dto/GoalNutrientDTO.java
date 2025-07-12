package ca.nutrisci.application.dto;

public class GoalNutrientDTO {
    private String nutrientName;
    private double amount;
    private String unit;
    
    // Default constructor
    public GoalNutrientDTO() {}
    
    // All-args constructor
    public GoalNutrientDTO(String nutrientName, double amount, String unit) {
        this.nutrientName = nutrientName;
        this.amount = amount;
        this.unit = unit;
    }
    
    // Getters and setters
    public String getNutrientName() {
        return nutrientName;
    }
    
    public void setNutrientName(String nutrientName) {
        this.nutrientName = nutrientName;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    @Override
    public String toString() {
        return String.format("GoalNutrientDTO{nutrientName='%s', amount=%.2f, unit='%s'}", 
                           nutrientName, amount, unit);
    }
} 