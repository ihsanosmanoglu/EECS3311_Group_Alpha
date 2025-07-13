package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.domain.entities.SwapHistory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for swap history operations
 */
public interface SwapHistoryRepo {
    void save(SwapHistory swapHistory);
    Optional<SwapHistory> findById(UUID id);
    List<SwapHistory> findByProfileId(UUID profileId);
    List<SwapHistory> getSwapsByDateRange(UUID profileId, LocalDate from, LocalDate to);
    void delete(UUID id);
} 