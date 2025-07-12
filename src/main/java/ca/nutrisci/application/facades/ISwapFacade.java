package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.GoalNutrientDTO;
import ca.nutrisci.application.dto.SwapDTO;
import ca.nutrisci.application.dto.SwapGoalDTO;
import ca.nutrisci.application.dto.SwapResultDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

/**
 * ISwapFacade - Interface for food swap operations
 * Part of the Application Layer - Facade Pattern
 */
public interface ISwapFacade {

    /**
     * Selects a swap strategy based on the goal and suggests swaps.
     * Used by ScreenControllers.
     * @param goal The swap goal.
     * @param nutrient The target nutrient for the goal.
     * @return A list of swap suggestions.
     */
    List<SwapDTO> selectStrategyAndSuggest(ArrayList<SwapGoalDTO> goals, ArrayList<GoalNutrientDTO> nutrients);

    /**
     * Previews the impact of a potential swap on a meal.
     * Used by ScreenControllers.
     * @param mealId The ID of the meal.
     * @param proposal The proposed swap.
     * @return The result of the swap preview.
     */
    SwapResultDTO previewSwap(UUID mealId, SwapDTO proposal);

    /**
     * Applies a chosen swap to a meal.
     * Used by ScreenControllers.
     * @param mealId The ID of the meal.
     * @param chosen The chosen swap.
     * @return The result of applying the swap.
     */
    SwapResultDTO applySwap(UUID mealId, SwapDTO chosen);

    /**
     * Lists all applied swaps within a given date range.
     * Used by ScreenControllers and for Visualization.
     * @param from The start date of the period.
     * @param to The end date of the period.
     * @return A list of applied swaps.
     */
    List<SwapDTO> listAppliedSwaps(LocalDate from, LocalDate to);
} 