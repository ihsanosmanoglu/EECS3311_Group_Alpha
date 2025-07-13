package ca.nutrisci.domain.entities;

/**
 * Enum representing different types of nutrients tracked in the system
 */
public enum NutrientType {
    CALORIES("Calories", "kcal"),
    PROTEIN("Protein", "g"),
    CARBOHYDRATES("Carbohydrates", "g"),
    FAT("Fat", "g"),
    FIBER("Fiber", "g"),
    SUGAR("Sugar", "g"),
    SODIUM("Sodium", "mg"),
    CALCIUM("Calcium", "mg"),
    IRON("Iron", "mg"),
    VITAMIN_A("Vitamin A", "IU"),
    VITAMIN_C("Vitamin C", "mg"),
    VITAMIN_D("Vitamin D", "IU");

    private final String displayName;
    private final String unit;

    NutrientType(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }
} 