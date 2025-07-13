package ca.nutrisci.domain.strategies;

import ca.nutrisci.application.dto.SwapGoalDTO;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import java.util.HashMap;
import java.util.Map;

/**
 * SwapStrategyFactory - Factory for creating swap strategy instances
 * Part of the Domain Layer - Strategy Pattern + Factory Pattern
 */
public class SwapStrategyFactory {
    
    private Map<String, SwapStrategy> strategies;
    private INutritionGateway nutritionGateway;
    
    public SwapStrategyFactory(INutritionGateway nutritionGateway) {
        this.nutritionGateway = nutritionGateway;
        this.strategies = new HashMap<>();
        initializeStrategies();
    }
    
    /**
     * Initialize available strategies
     */
    private void initializeStrategies() {
        ReduceCaloriesStrategy reduceCaloriesStrategy = new ReduceCaloriesStrategy(nutritionGateway);
        IncreaseFiberStrategy increaseFiberStrategy = new IncreaseFiberStrategy(nutritionGateway);
        
        strategies.put("reduce_calories", reduceCaloriesStrategy);
        strategies.put("increase_fiber", increaseFiberStrategy);
        
        // Add more strategies as needed
        strategies.put("increase_protein", new IncreaseFiberStrategy(nutritionGateway)); // placeholder
        strategies.put("reduce_sugar", new ReduceCaloriesStrategy(nutritionGateway)); // placeholder
    }
    
    /**
     * Get strategy by goal type
     */
    public SwapStrategy getStrategy(String goalType) {
        if (goalType == null || goalType.trim().isEmpty()) {
            return null;
        }
        
        return strategies.get(goalType.trim().toLowerCase());
    }
    
    /**
     * Get strategy by goal DTO
     */
    public SwapStrategy getStrategy(SwapGoalDTO goal) {
        if (goal == null) {
            return null;
        }
        
        return getStrategy(goal.getGoalType());
    }
    
    /**
     * Check if strategy exists for goal type
     */
    public boolean hasStrategy(String goalType) {
        return goalType != null && strategies.containsKey(goalType.trim().toLowerCase());
    }
    
    /**
     * Get all available goal types
     */
    public String[] getAvailableGoalTypes() {
        return strategies.keySet().toArray(new String[0]);
    }
    
    /**
     * Add a new strategy
     */
    public void addStrategy(String goalType, SwapStrategy strategy) {
        if (goalType != null && strategy != null) {
            strategies.put(goalType.trim().toLowerCase(), strategy);
        }
    }
    
    /**
     * Remove a strategy
     */
    public void removeStrategy(String goalType) {
        if (goalType != null) {
            strategies.remove(goalType.trim().toLowerCase());
        }
    }
} 