package ca.nutrisci.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SwapHistory - Domain entity for food swap history
 * Part of the Domain Layer
 */
public class SwapHistory {
    
    private UUID id;
    private UUID profileId;
    private String originalFood;
    private String replacementFood;
    private String swapReason;
    private String goalType;
    private double impactScore;
    private LocalDateTime createdAt;
    
    // Default constructor
    public SwapHistory() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.impactScore = 0.0;
    }
    
    // Full constructor
    public SwapHistory(UUID id, UUID profileId, String originalFood, String replacementFood, 
                      String swapReason, String goalType, double impactScore,
                      LocalDateTime createdAt) {
        this.id = id;
        this.profileId = profileId;
        this.originalFood = originalFood;
        this.replacementFood = replacementFood;
        this.swapReason = swapReason;
        this.goalType = goalType;
        this.impactScore = impactScore;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        validateSwapHistory();
    }
    
    // Validation
    private void validateSwapHistory() {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        if (originalFood == null || originalFood.trim().isEmpty()) {
            throw new IllegalArgumentException("Original food cannot be empty");
        }
        if (replacementFood == null || replacementFood.trim().isEmpty()) {
            throw new IllegalArgumentException("Replacement food cannot be empty");
        }
        if (goalType == null || goalType.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal type cannot be empty");
        }
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }
    
    public String getOriginalFood() { return originalFood; }
    public void setOriginalFood(String originalFood) { this.originalFood = originalFood; }
    
    public String getReplacementFood() { return replacementFood; }
    public void setReplacementFood(String replacementFood) { this.replacementFood = replacementFood; }
    
    public String getSwapReason() { return swapReason; }
    public void setSwapReason(String swapReason) { this.swapReason = swapReason; }
    
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
    
    public double getImpactScore() { return impactScore; }
    public void setImpactScore(double impactScore) { this.impactScore = impactScore; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return String.format("SwapHistory{id=%s, %s→%s, goal='%s', impact=%.2f}",
                id, originalFood, replacementFood, goalType, impactScore);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SwapHistory that = (SwapHistory) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 