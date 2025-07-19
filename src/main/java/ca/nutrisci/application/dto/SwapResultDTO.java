package ca.nutrisci.application.dto;

import java.util.Map;
import java.util.ArrayList;

public class SwapResultDTO {
    private SwapDTO swap;
    private ArrayList<Map<String, Double>> nutrientChanges; // e.g., {"calories": -50, "protein": 5}
    
    // Default constructor
    public SwapResultDTO() {}
    
    // All-args constructor
    public SwapResultDTO(SwapDTO swap, ArrayList<Map<String, Double>> nutrientChanges) {
        this.swap = swap;
        this.nutrientChanges = nutrientChanges;
    }
    
    // Getters and setters
    public SwapDTO getSwap() {
        return swap;
    }
    
    public void setSwap(SwapDTO swap) {
        this.swap = swap;
    }
    
    public ArrayList<Map<String, Double>> getNutrientChanges() {
        return nutrientChanges;
    }
    
    public void setNutrientChanges(ArrayList<Map<String, Double>> nutrientChanges) {
        this.nutrientChanges = nutrientChanges;
    }
    
    @Override
    public String toString() {
        return String.format("SwapResultDTO{swap=%s, nutrientChanges=%s}", swap, nutrientChanges);
    }
} 