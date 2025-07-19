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
        // Calorie strategies
        strategies.put("decrease_calories", new DecreaseCaloriesStrategy(nutritionGateway));
        strategies.put("increase_calories", new IncreaseCaloriesStrategy(nutritionGateway));
        
        // Protein strategies
        strategies.put("decrease_protein", new DecreaseProteinStrategy(nutritionGateway));
        strategies.put("increase_protein", new IncreaseProteinStrategy(nutritionGateway));
        
        // Carbohydrate strategies
        strategies.put("decrease_carbohydrates", new DecreaseCarbohydratesStrategy(nutritionGateway));
        strategies.put("increase_carbohydrates", new IncreaseCarbohydratesStrategy(nutritionGateway));
        
        // Fat strategies
        strategies.put("decrease_fat", new DecreaseFatStrategy(nutritionGateway));
        strategies.put("increase_fat", new IncreaseFatStrategy(nutritionGateway));
        
        // Fiber strategies
        strategies.put("decrease_fiber", new DecreaseFiberStrategy(nutritionGateway));
        strategies.put("increase_fiber", new IncreaseFiberStrategy(nutritionGateway));
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
     * Get strategy by goal target and action
     * This method maps from the SwapEngine's goal target names to strategy names
     */
    public SwapStrategy getStrategy(String goalTarget, SwapGoalDTO.GoalAction action) {
        if (goalTarget == null || action == null) {
            return null;
        }
        
        String actionPrefix = (action == SwapGoalDTO.GoalAction.DECREASE) ? "decrease" : "increase";
        String strategyKey = actionPrefix + "_" + mapGoalTarget(goalTarget);
        
        return strategies.get(strategyKey);
    }
    
    /**
     * Map goal target names from SwapEngine to strategy naming convention
     */
    private String mapGoalTarget(String goalTarget) {
        if (goalTarget == null) {
            return null;
        }
        
        switch (goalTarget.toLowerCase()) {
            case "calories":
                return "calories";
            case "protein":
                return "protein";
            case "carbs":
                return "carbohydrates";
            case "fat":
                return "fat";
            case "fiber":
                return "fiber";
            default:
                return goalTarget.toLowerCase();
        }
    }
    
    /**
     * Get strategy by goal DTO
     */
    public SwapStrategy getStrategy(SwapGoalDTO goal) {
        if (goal.getGoalTarget() == null || goal.getAction() == null) {
            return null;
        }
        
        String actionPrefix = (goal.getAction() == SwapGoalDTO.GoalAction.DECREASE) ? "decrease" : "increase";
        String strategyKey = actionPrefix + "_" + mapGoalTarget(goal.getGoalTarget());
        
        return strategies.get(strategyKey);
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
    public void addStrategy(String goalTarget, SwapGoalDTO.GoalAction action, SwapStrategy strategy) {
        if (goalTarget != null && strategy != null) {
            String actionPrefix = (action == SwapGoalDTO.GoalAction.DECREASE) ? "decrease" : "increase";
            String strategyKey = actionPrefix + "_" + mapGoalTarget(goalTarget);
            strategies.put(strategyKey, strategy);
        }
    }
    
    /**
     * Remove a strategy
     */
    public void removeStrategy(String goalTarget, SwapGoalDTO.GoalAction action) {
        if (goalTarget != null) {
            String actionPrefix = (action == SwapGoalDTO.GoalAction.DECREASE) ? "decrease" : "increase";
            String strategyKey = actionPrefix + "_" + mapGoalTarget(goalTarget);
            strategies.remove(strategyKey);
        }
    }
} 