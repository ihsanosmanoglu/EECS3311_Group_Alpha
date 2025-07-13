package ca.nutrisci.infrastructure.data.repositories;

/**
 * IRepositoryFactory - Abstract factory for creating repository families
 * Part of the Infrastructure Layer - Abstract Factory Pattern
 */
public interface IRepositoryFactory {
    
    /**
     * Get profile repository
     */
    ProfileRepo getProfileRepository();
    
    /**
     * Get meal log repository
     */
    MealLogRepo getMealLogRepository();
    
    /**
     * Get swap history repository
     */
    SwapHistoryRepo getSwapHistoryRepository();
} 