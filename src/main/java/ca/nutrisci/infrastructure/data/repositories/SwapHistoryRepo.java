package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.domain.entities.SwapHistory;
import java.util.List;
import java.util.UUID;

/**
 * SwapHistoryRepo - Repository interface for swap history data access
 * Part of the Infrastructure Layer
 */
public interface SwapHistoryRepo {
    
    // Basic CRUD operations
    SwapHistory save(SwapHistory swapHistory);
    SwapHistory findById(UUID swapHistoryId);
    void delete(UUID swapHistoryId);
    List<SwapHistory> findAll();
    SwapHistory update(SwapHistory swapHistory);
    
    // Legacy method names for backward compatibility
    void saveSwapHistory(SwapHistory swapHistory);
    void deleteSwapHistory(UUID swapHistoryId);
    List<SwapHistory> listAllSwapHistories();
    
    // Swap history specific queries
    List<SwapHistory> findByProfileId(UUID profileId);
    List<SwapHistory> findByProfileIdAndGoalType(UUID profileId, String goalType);
    List<SwapHistory> findByGoalType(String goalType);
    List<SwapHistory> findByOriginalFood(String originalFood);
    List<SwapHistory> findByReplacementFood(String replacementFood);
    
    // Aggregate queries
    int countSwapsForProfile(UUID profileId);
    int countSwapsByGoalType(String goalType);
    boolean existsByProfileIdAndGoalType(UUID profileId, String goalType);
    
    // Statistics queries
    double getAverageImpactScore(UUID profileId);
    List<SwapHistory> findTopSwapsByImpactScore(int limit);
    List<SwapHistory> findRecentSwaps(UUID profileId, int limit);
} 