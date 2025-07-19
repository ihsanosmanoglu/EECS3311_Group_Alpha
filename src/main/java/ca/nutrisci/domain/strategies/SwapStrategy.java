package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.SwapDTO;
import ca.nutrisci.application.dto.SwapGoalDTO;
import java.util.List;

/**
 * SwapStrategy - Strategy interface for food swap algorithms
 * Part of the Domain Layer - Strategy Pattern
 */
public interface SwapStrategy {
    
    // Main strategy method
    List<SwapDTO> generateSwaps(MealDTO meal, SwapGoalDTO goal);
    
    // Ranking method
    List<SwapDTO> rankByGoal(List<SwapDTO> swaps);
    
    // Strategy identification
    String getStrategyType();
    String getDescription();

    /**
     * Find food swaps for a given food item based on the goal
     */
    List<SwapDTO> findSwaps(String currentFood, SwapGoalDTO goal);
    
    /**
     * Get the goal type this strategy handles
     */
    String getGoalType();
    
    /**
     * Check if this strategy can handle the given goal
     */
    boolean canHandle(SwapGoalDTO goal);
    
    /**
     * Calculate impact score for a swap
     */
    double calculateImpactScore(SwapDTO swap, SwapGoalDTO goal);
} 