package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.SwapDTO;
import ca.nutrisci.application.dto.SwapGoalDTO;

import java.util.List;
import java.util.UUID;

/**
 * ISwapFacade - Interface for food swap operations
 * Part of the Application Layer - Facade Pattern
 */
public interface ISwapFacade {
    
    /**
     * Find food swaps based on goal
     */
    List<SwapDTO> findSwaps(String currentFood, SwapGoalDTO goal);
    
    /**
     * Apply a food swap
     */
    SwapDTO applySwap(UUID profileId, SwapDTO swapDTO);
    
    /**
     * Get swap suggestions based on goal
     */
    List<SwapDTO> getSwapSuggestions(String goalType, List<String> currentFoods);
    
    /**
     * Get swap history for a profile
     */
    List<SwapDTO> getSwapHistory(UUID profileId);
    
    /**
     * Get swap history by goal
     */
    List<SwapDTO> getSwapHistoryByGoal(UUID profileId, String goalType);
    
    /**
     * Analyze swap impact
     */
    String analyzeSwapImpact(SwapDTO swapDTO);
    
    /**
     * Validate swap data
     */
    boolean validateSwap(SwapDTO swapDTO);
    
    /**
     * Get available goal types
     */
    List<String> getAvailableGoalTypes();
    
    /**
     * Create swap goal
     */
    SwapGoalDTO createSwapGoal(String goalType, double targetValue);
    
    /**
     * Delete swap from history
     */
    void deleteSwapHistory(UUID swapHistoryId);
    
    /**
     * Get swap statistics for profile
     */
    String getSwapStatistics(UUID profileId);
} 