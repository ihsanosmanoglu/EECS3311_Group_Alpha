package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.SwapDTO;
import ca.nutrisci.application.dto.SwapGoalDTO;
import ca.nutrisci.domain.entities.SwapHistory;
import ca.nutrisci.domain.strategies.SwapStrategy;
import ca.nutrisci.domain.strategies.SwapStrategyFactory;
import ca.nutrisci.infrastructure.data.repositories.SwapHistoryRepo;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SwapEngine - Facade for all food swap operations
 * Part of the Application Layer - Facade Pattern
 * This is the main entry point for UI swap operations
 */
public class SwapEngine implements ISwapFacade {
    
    private SwapStrategyFactory strategyFactory;
    private SwapHistoryRepo swapHistoryRepo;
    private INutritionGateway nutritionGateway;
    
    public SwapEngine(SwapStrategyFactory strategyFactory, SwapHistoryRepo swapHistoryRepo, 
                      INutritionGateway nutritionGateway) {
        this.strategyFactory = strategyFactory;
        this.swapHistoryRepo = swapHistoryRepo;
        this.nutritionGateway = nutritionGateway;
    }
    
    /**
     * Find food swaps based on goal
     */
    @Override
    public List<SwapDTO> findSwaps(String currentFood, SwapGoalDTO goal) {
        if (currentFood == null || currentFood.trim().isEmpty()) {
            throw new IllegalArgumentException("Current food cannot be empty");
        }
        
        if (goal == null) {
            throw new IllegalArgumentException("Swap goal cannot be null");
        }
        
        // Get the appropriate strategy for the goal
        SwapStrategy strategy = strategyFactory.getStrategy(goal.getGoalType());
        
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for goal: " + goal.getGoalType());
        }
        
        // Find swap options using the strategy
        List<SwapDTO> swaps = strategy.findSwaps(currentFood.trim(), goal);
        
        // Enrich with nutrition information
        return swaps.stream()
                .map(this::enrichSwapWithNutrition)
                .collect(Collectors.toList());
    }
    
    /**
     * Apply a food swap
     */
    @Override
    public SwapDTO applySwap(UUID profileId, SwapDTO swapDTO) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (swapDTO == null) {
            throw new IllegalArgumentException("Swap DTO cannot be null");
        }
        
        // Validate swap data
        if (!validateSwap(swapDTO)) {
            throw new IllegalArgumentException("Invalid swap data");
        }
        
        // Create swap history record
        SwapHistory swapHistory = new SwapHistory(
            UUID.randomUUID(),
            profileId,
            swapDTO.getOriginalFood(),
            swapDTO.getReplacementFood(),
            swapDTO.getSwapReason(),
            swapDTO.getGoalType(),
            swapDTO.getImpactScore(),
            null // createdAt will be set by entity
        );
        
        // Save to history
        swapHistoryRepo.save(swapHistory);
        SwapHistory savedHistory = swapHistory;
        
        // Return updated swap DTO with history ID
        SwapDTO result = new SwapDTO(
            swapDTO.getOriginalFood(),
            swapDTO.getReplacementFood(),
            swapDTO.getSwapReason(),
            swapDTO.getGoalType(),
            swapDTO.getImpactScore(),
            swapDTO.getOriginalNutrition(),
            swapDTO.getReplacementNutrition()
        );
        
        result.setSwapHistoryId(savedHistory.getId());
        return result;
    }
    
    /**
     * Get swap suggestions based on goal
     */
    @Override
    public List<SwapDTO> getSwapSuggestions(String goalType, List<String> currentFoods) {
        if (goalType == null || goalType.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal type cannot be empty");
        }
        
        if (currentFoods == null || currentFoods.isEmpty()) {
            throw new IllegalArgumentException("Current foods cannot be empty");
        }
        
        // Create default goal based on type
        SwapGoalDTO goal = createDefaultGoal(goalType.trim());
        
        // Get strategy for this goal type
        SwapStrategy strategy = strategyFactory.getStrategy(goalType.trim());
        
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for goal: " + goalType);
        }
        
        // Find swaps for each food
        List<SwapDTO> allSwaps = currentFoods.stream()
                .flatMap(food -> strategy.findSwaps(food, goal).stream())
                .collect(Collectors.toList());
        
        // Enrich with nutrition data
        return allSwaps.stream()
                .map(this::enrichSwapWithNutrition)
                .collect(Collectors.toList());
    }
    
    /**
     * Get swap history for a profile
     */
    @Override
    public List<SwapDTO> getSwapHistory(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        List<SwapHistory> history = swapHistoryRepo.findByProfileId(profileId);
        
        return history.stream()
                .map(this::convertHistoryToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get swap history by goal
     */
    @Override
    public List<SwapDTO> getSwapHistoryByGoal(UUID profileId, String goalType) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (goalType == null || goalType.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal type cannot be empty");
        }
        
        List<SwapHistory> history = swapHistoryRepo.findByProfileId(profileId)
            .stream()
            .filter(h -> h.getGoalType().equalsIgnoreCase(goalType.trim()))
            .collect(Collectors.toList());
        
        return history.stream()
                .map(this::convertHistoryToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Analyze swap impact
     */
    @Override
    public String analyzeSwapImpact(SwapDTO swapDTO) {
        if (swapDTO == null) {
            throw new IllegalArgumentException("Swap DTO cannot be null");
        }
        
        if (swapDTO.getOriginalNutrition() == null || swapDTO.getReplacementNutrition() == null) {
            return "Insufficient nutrition data for impact analysis";
        }
        
        double calorieChange = swapDTO.getReplacementNutrition().getCalories() - 
                              swapDTO.getOriginalNutrition().getCalories();
        double proteinChange = swapDTO.getReplacementNutrition().getProtein() - 
                              swapDTO.getOriginalNutrition().getProtein();
        double fiberChange = swapDTO.getReplacementNutrition().getFiber() - 
                            swapDTO.getOriginalNutrition().getFiber();
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("Swap Impact Analysis:\n");
        analysis.append(String.format("- Calories: %+.0f\n", calorieChange));
        analysis.append(String.format("- Protein: %+.1fg\n", proteinChange));
        analysis.append(String.format("- Fiber: %+.1fg\n", fiberChange));
        
        if (calorieChange < 0) {
            analysis.append("✓ Reduces calories\n");
        } else if (calorieChange > 0) {
            analysis.append("⚠ Increases calories\n");
        }
        
        if (proteinChange > 0) {
            analysis.append("✓ Increases protein\n");
        }
        
        if (fiberChange > 0) {
            analysis.append("✓ Increases fiber\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * Validate swap data
     */
    @Override
    public boolean validateSwap(SwapDTO swapDTO) {
        if (swapDTO == null) return false;
        if (swapDTO.getOriginalFood() == null || swapDTO.getOriginalFood().trim().isEmpty()) return false;
        if (swapDTO.getReplacementFood() == null || swapDTO.getReplacementFood().trim().isEmpty()) return false;
        if (swapDTO.getGoalType() == null || swapDTO.getGoalType().trim().isEmpty()) return false;
        if (swapDTO.getOriginalFood().equals(swapDTO.getReplacementFood())) return false;
        
        return true;
    }
    
    /**
     * Get available goal types
     */
    @Override
    public List<String> getAvailableGoalTypes() {
        return List.of("reduce_calories", "increase_fiber", "increase_protein", "reduce_sugar");
    }
    
    /**
     * Create swap goal
     */
    @Override
    public SwapGoalDTO createSwapGoal(String goalType, double targetValue) {
        if (goalType == null || goalType.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal type cannot be empty");
        }
        
        if (targetValue < 0) {
            throw new IllegalArgumentException("Target value must be non-negative");
        }
        
        return new SwapGoalDTO(goalType.trim(), targetValue);
    }
    
    /**
     * Delete swap from history
     */
    @Override
    public void deleteSwapHistory(UUID swapHistoryId) {
        if (swapHistoryId == null) {
            throw new IllegalArgumentException("Swap history ID cannot be null");
        }
        SwapHistory history = swapHistoryRepo.findById(swapHistoryId).orElse(null);
        if (history == null) {
            throw new IllegalArgumentException("Swap history not found: " + swapHistoryId);
        }
        swapHistoryRepo.delete(swapHistoryId);
    }
    
    /**
     * Get swap statistics for profile
     */
    @Override
    public String getSwapStatistics(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        List<SwapHistory> history = swapHistoryRepo.findByProfileId(profileId);
        
        if (history.isEmpty()) {
            return "No swap history found for this profile";
        }
        
        long totalSwaps = history.size();
        long calorieReductionSwaps = history.stream()
                .filter(h -> "reduce_calories".equals(h.getGoalType()))
                .count();
        long fiberIncreaseSwaps = history.stream()
                .filter(h -> "increase_fiber".equals(h.getGoalType()))
                .count();
        
        return String.format("Swap Statistics:\n- Total Swaps: %d\n- Calorie Reduction: %d\n- Fiber Increase: %d",
                totalSwaps, calorieReductionSwaps, fiberIncreaseSwaps);
    }
    
    // Helper methods
    
    private SwapDTO enrichSwapWithNutrition(SwapDTO swap) {
        if (swap.getOriginalNutrition() == null && swap.getOriginalFood() != null) {
            swap.setOriginalNutrition(nutritionGateway.lookupIngredient(swap.getOriginalFood()));
        }
        
        if (swap.getReplacementNutrition() == null && swap.getReplacementFood() != null) {
            swap.setReplacementNutrition(nutritionGateway.lookupIngredient(swap.getReplacementFood()));
        }
        
        return swap;
    }
    
    private SwapDTO convertHistoryToDTO(SwapHistory history) {
        SwapDTO dto = new SwapDTO(
            history.getOriginalFood(),
            history.getReplacementFood(),
            history.getSwapReason(),
            history.getGoalType(),
            history.getImpactScore(),
            null, // nutrition will be enriched if needed
            null
        );
        
        dto.setSwapHistoryId(history.getId());
        return dto;
    }
    
    private SwapGoalDTO createDefaultGoal(String goalType) {
        switch (goalType.toLowerCase()) {
            case "reduce_calories":
                return new SwapGoalDTO("reduce_calories", 100.0); // reduce by 100 calories
            case "increase_fiber":
                return new SwapGoalDTO("increase_fiber", 5.0); // increase by 5g fiber
            case "increase_protein":
                return new SwapGoalDTO("increase_protein", 10.0); // increase by 10g protein
            case "reduce_sugar":
                return new SwapGoalDTO("reduce_sugar", 10.0); // reduce by 10g sugar
            default:
                return new SwapGoalDTO(goalType, 0.0);
        }
    }
} 