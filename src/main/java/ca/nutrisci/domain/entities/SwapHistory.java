package ca.nutrisci.domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a food swap history record
 */
public class SwapHistory {
    private UUID id;
    private UUID profileId;
    private LocalDate date;
    private String originalFood;
    private String newFood;
    private String goalType;
    private String swapReason;
    private double impactScore;
    private LocalDateTime createdAt;
    private Map<NutrientType, Double> originalNutrients;
    private Map<NutrientType, Double> newNutrients;

    public SwapHistory() {
        this.originalNutrients = new HashMap<>();
        this.newNutrients = new HashMap<>();
    }

    public SwapHistory(UUID id, UUID profileId, String originalFood, String newFood, String swapReason, String goalType, double impactScore, LocalDateTime createdAt) {
        this.id = id;
        this.profileId = profileId;
        this.originalFood = originalFood;
        this.newFood = newFood;
        this.swapReason = swapReason;
        this.goalType = goalType;
        this.impactScore = impactScore;
        this.createdAt = createdAt;
        this.originalNutrients = new HashMap<>();
        this.newNutrients = new HashMap<>();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getOriginalFood() { return originalFood; }
    public void setOriginalFood(String originalFood) { this.originalFood = originalFood; }
    public String getNewFood() { return newFood; }
    public void setNewFood(String newFood) { this.newFood = newFood; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
    public String getSwapReason() { return swapReason; }
    public void setSwapReason(String swapReason) { this.swapReason = swapReason; }
    public double getImpactScore() { return impactScore; }
    public void setImpactScore(double impactScore) { this.impactScore = impactScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Map<NutrientType, Double> getOriginalNutrients() { return new HashMap<>(originalNutrients); }
    public void setOriginalNutrients(Map<NutrientType, Double> originalNutrients) { this.originalNutrients = new HashMap<>(originalNutrients); }
    public Map<NutrientType, Double> getNewNutrients() { return new HashMap<>(newNutrients); }
    public void setNewNutrients(Map<NutrientType, Double> newNutrients) { this.newNutrients = new HashMap<>(newNutrients); }
    // Alias for compatibility
    public String getReplacementFood() { return getNewFood(); }
} 