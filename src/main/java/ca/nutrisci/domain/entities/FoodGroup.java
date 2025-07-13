package ca.nutrisci.domain.entities;

/**
 * Represents the main food groups according to Canada's Food Guide
 */
public enum FoodGroup {
    VEGETABLES_AND_FRUITS("Vegetables and Fruits"),
    WHOLE_GRAINS("Whole Grains"),
    PROTEIN_FOODS("Protein Foods"),
    UNKNOWN("Unknown");

    private final String displayName;

    FoodGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 