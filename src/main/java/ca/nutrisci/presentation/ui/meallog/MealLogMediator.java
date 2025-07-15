package ca.nutrisci.presentation.ui.meallog;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.infrastructure.external.adapters.ExternalAdapter;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * MealLogMediator - Mediator for coordinating meal logging sub-components
 * 
 * PURPOSE:
 * - Coordinates communication between QuickAddPanel, NutritionBreakdownPanel, and MealJournalPanel
 * - Implements the Mediator pattern to decouple components from each other
 * - Handles complex ingredient selection dialog and meal creation workflow
 * - Manages state synchronization across all meal logging components
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-3: NavigationMediator over MVC Controllers - Centralizes component coordination
 * - DD-1: Three-Tier Layered - Only depends on Application layer facades
 * - DD-2: Four Domain Façades - Uses IMealLogFacade for business operations
 * - DD-8: Observer Pattern - Coordinates events between components
 * - DD-9: Naming Conventions - MealLogMediator follows <Domain><Mediator> pattern
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only component coordination and mediation
 * - Open/Closed: Extensible for additional meal logging components
 * - Liskov Substitution: Can be used anywhere a mediator is expected
 * - Interface Segregation: Uses specific facade interfaces only
 * - Dependency Inversion: Depends on abstractions, not concrete implementations
 * 
 * MEDIATOR PATTERN:
 * - Components don't communicate directly with each other
 * - All inter-component communication goes through this mediator
 * - Reduces coupling between UI components
 * - Centralizes complex interaction logic
 * 
 * @author NutriSci Development Team
 * @version 2.0
 * @since 1.0
 */
public class MealLogMediator implements 
    QuickAddPanel.QuickAddListener,
    MealJournalPanel.MealJournalListener,
    NutritionBreakdownPanel.NutritionUpdateListener {
    
    // Core dependencies (DD-1: Layered Architecture, DD-2: Domain Façades)
    private final IMealLogFacade mealLogFacade;
    private final INutritionGateway nutritionGateway;
    
    // Coordinated components (Mediator Pattern)
    private QuickAddPanel quickAddPanel;
    private NutritionBreakdownPanel nutritionBreakdownPanel;
    private MealJournalPanel mealJournalPanel;
    
    // Current state
    private ProfileDTO activeProfile;
    private LocalDate currentViewDate;
    
    /**
     * Constructor - Initializes the meal log mediator
     * 
     * @param mealLogFacade Facade for meal operations (DD-2: Domain Façades)
     * @param activeProfile Currently active profile
     */
    public MealLogMediator(IMealLogFacade mealLogFacade, ProfileDTO activeProfile) {
        this.mealLogFacade = mealLogFacade;
        this.activeProfile = activeProfile;
        this.currentViewDate = LocalDate.now();
        this.nutritionGateway = ExternalAdapter.getInstance("Canada Nutrient File-20250622");
    }
    
    /**
     * Register the coordinated components
     * Follows Mediator Pattern - establishes component relationships
     * 
     * @param quickAddPanel Quick-add component
     * @param nutritionBreakdownPanel Nutrition display component  
     * @param mealJournalPanel Meal journal component
     */
    public void registerComponents(QuickAddPanel quickAddPanel, 
                                  NutritionBreakdownPanel nutritionBreakdownPanel,
                                  MealJournalPanel mealJournalPanel) {
        this.quickAddPanel = quickAddPanel;
        this.nutritionBreakdownPanel = nutritionBreakdownPanel;
        this.mealJournalPanel = mealJournalPanel;
        
        // Set mediator as listener for all components (DD-8: Observer Pattern)
        quickAddPanel.setQuickAddListener(this);
        nutritionBreakdownPanel.setNutritionUpdateListener(this);
        mealJournalPanel.setMealJournalListener(this);
    }
    
    // QuickAddPanel.QuickAddListener implementation (DD-8: Observer Pattern)
    
    /**
     * Handle quick-add meal request from QuickAddPanel
     * 
     * MEDIATOR COORDINATION:
     * - Shows ingredient selection dialog
     * - Handles meal creation/editing through domain facade
     * - Coordinates UI updates across all components
     * 
     * @param mealType Type of meal to add/edit
     * @param selectedDate Date for the meal
     * @param existingMeal Existing meal if editing, null if creating new
     */
    @Override
    public void onQuickAddRequested(String mealType, LocalDate selectedDate, MealDTO existingMeal) {
        try {
            // Show ingredient selection dialog
            IngredientSelectionResult result = showIngredientSelectionDialog(mealType, existingMeal);
            
            if (result != null && result.isConfirmed()) {
                // Create or update meal using domain facade (DD-2: Domain Façades)
                MealDTO savedMeal = createOrUpdateMeal(mealType, selectedDate, result, existingMeal);
                
                if (savedMeal != null) {
                    // Coordinate UI updates across components (Mediator Pattern)
                    refreshAllComponents();
                    
                    // Show success message
                    String action = existingMeal != null ? "updated" : "created";
                    showSuccessMessage(mealType, action, selectedDate, savedMeal);
                } else {
                    // Handle meal deletion case (empty meal auto-deleted)
                    refreshAllComponents();
                    showMealDeletedMessage(mealType);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling quick add request: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error processing meal: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handle date change from QuickAddPanel
     * 
     * MEDIATOR COORDINATION:
     * - Synchronizes date across all components
     * - Prevents infinite loops by tracking date changes
     * - Updates nutrition breakdown and meal journal for new date
     * 
     * @param newDate New date selected in QuickAddPanel
     */
    @Override
    public void onDateChanged(LocalDate newDate) {
        // Prevent infinite loops - only update if date actually changed
        if (!newDate.equals(currentViewDate)) {
            this.currentViewDate = newDate;
            
            // Coordinate date updates across ALL components (Mediator Pattern)
            synchronizeDateAcrossComponents(newDate);
            
            System.out.println("Date synchronized across all components: " + newDate);
        }
    }
    
    /**
     * Synchronize date across all components
     * 
     * MEDIATOR COORDINATION:
     * - Updates all components with the new date
     * - Prevents infinite loops by using silent update methods
     * - Ensures all meal logging components show data for the same date
     * 
     * @param newDate New date to synchronize across components
     */
    private void synchronizeDateAcrossComponents(LocalDate newDate) {
        // Update QuickAddPanel date field (without triggering listener to prevent loops)
        if (quickAddPanel != null) {
            quickAddPanel.setSelectedDateSilently(newDate);
        }
        
        // Update MealJournalPanel date field and refresh meal list (without triggering listener)
        if (mealJournalPanel != null) {
            mealJournalPanel.setDisplayDateSilently(newDate);
        }
        
        // Update NutritionBreakdownPanel for new date
        if (nutritionBreakdownPanel != null) {
            nutritionBreakdownPanel.setDisplayDate(newDate);
        }
        
        System.out.println("✅ All components synchronized to date: " + newDate);
    }
    
    // MealJournalPanel.MealJournalListener implementation (DD-8: Observer Pattern)
    
    /**
     * Handle view meal details request from MealJournalPanel
     * 
     * @param meal Meal to view details for
     */
    @Override
    public void onViewMealDetails(MealDTO meal) {
        showMealDetailsDialog(meal);
    }
    
    /**
     * Handle delete meal request from MealJournalPanel
     * 
     * MEDIATOR COORDINATION:
     * - Deletes meal through domain facade
     * - Coordinates UI updates across all components
     * 
     * @param meal Meal to delete
     */
    @Override
    public void onDeleteMeal(MealDTO meal) {
        try {
            // Delete meal using domain facade (DD-2: Domain Façades)
            mealLogFacade.deleteMeal(meal.getId());
            
            // Coordinate UI updates across components (Mediator Pattern)
            refreshAllComponents();
            
            // Show success message
            JOptionPane.showMessageDialog(null,
                "✅ " + meal.getMealType().toUpperCase() + " meal deleted successfully!",
                "Meal Deleted",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            System.err.println("Error deleting meal: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error deleting meal: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // NutritionBreakdownPanel.NutritionUpdateListener implementation (DD-8: Observer Pattern)
    
    /**
     * Handle nutrition update from NutritionBreakdownPanel
     * 
     * @param date Date for which nutrition was updated
     * @param nutrients Updated nutrition information
     */
    @Override
    public void onNutritionUpdated(LocalDate date, NutrientInfo nutrients) {
        System.out.println("Nutrition updated for " + date + ": " + 
                          String.format("%.0f calories", nutrients.getCalories()));
    }
    
    // Meal creation and ingredient selection methods
    
    /**
     * Show ingredient selection dialog for meal creation/editing
     * 
     * BUSINESS LOGIC:
     * - Creates modal dialog for ingredient selection
     * - Handles both new meal creation and existing meal editing
     * - Integrates with nutrition gateway for food lookup
     * 
     * @param mealType Type of meal
     * @param existingMeal Existing meal if editing, null if creating new
     * @return Result containing selected ingredients and quantities, or null if cancelled
     */
    private IngredientSelectionResult showIngredientSelectionDialog(String mealType, MealDTO existingMeal) {
        // Create proper ingredient selection dialog
        IngredientSelectionDialog dialog = new IngredientSelectionDialog(
            null, mealType, existingMeal, nutritionGateway);
        dialog.setVisible(true);
        
        return dialog.getResult();
    }
    
    /**
     * Create or update a meal using the domain facade
     * 
     * LAYERED ARCHITECTURE (DD-1):
     * - Uses IMealLogFacade for business operations
     * - No direct access to data layer or domain entities
     * 
     * @param mealType Type of meal
     * @param selectedDate Date for the meal
     * @param result Ingredient selection result
     * @param existingMeal Existing meal if editing, null if creating new
     * @return Created/updated meal, or null if auto-deleted
     */
    private MealDTO createOrUpdateMeal(String mealType, LocalDate selectedDate, 
                                      IngredientSelectionResult result, MealDTO existingMeal) {
        try {
            // Calculate nutrition using the nutrition gateway
            NutrientInfo totalNutrients = calculateTotalNutrition(result.getIngredients());
            
            if (existingMeal != null) {
                // Update existing meal
                MealDTO updatedMeal = new MealDTO(
                    existingMeal.getId(),
                    activeProfile.getId(),
                    existingMeal.getDate(),
                    mealType.toLowerCase(),
                    result.getIngredients(),
                    totalNutrients
                );
                
                return mealLogFacade.editMeal(existingMeal.getId(), updatedMeal);
            } else {
                // Create new meal
                MealDTO newMeal = new MealDTO(
                    null,
                    activeProfile.getId(),
                    selectedDate,
                    mealType.toLowerCase(),
                    result.getIngredients(),
                    totalNutrients
                );
                
                return mealLogFacade.addMeal(newMeal);
            }
        } catch (Exception e) {
            System.err.println("Error creating/updating meal: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Calculate total nutrition for selected ingredients
     * 
     * @param ingredients List of IngredientDTO objects
     * @return Total nutrition information
     */
    private NutrientInfo calculateTotalNutrition(java.util.List<ca.nutrisci.application.dto.IngredientDTO> ingredients) {
        NutrientInfo totalNutrients = new NutrientInfo();
        
        for (ca.nutrisci.application.dto.IngredientDTO ingredient : ingredients) {
            try {
                NutrientInfo ingredientNutrients = nutritionGateway.lookupIngredient(ingredient.getName());
                if (ingredientNutrients != null) {
                    // Convert quantity to grams using UnitConversionService
                    double quantityInGrams = ingredient.getQuantity();
                    
                    // If not already in grams, convert using UnitConversionService
                    if (!ingredient.getUnit().equalsIgnoreCase("g") && !ingredient.getUnit().equalsIgnoreCase("grams")) {
                        try {
                            ca.nutrisci.application.services.UnitConversionService unitService = 
                                ca.nutrisci.application.services.UnitConversionService.getInstance();
                            int foodId = nutritionGateway.getFoodId(ingredient.getName());
                            if (foodId > 0) {
                                quantityInGrams = unitService.convertToGrams(foodId, ingredient.getQuantity(), ingredient.getUnit());
                            }
                        } catch (Exception e) {
                            System.err.println("Error converting units for " + ingredient.getName() + ": " + e.getMessage());
                            // Use original quantity as fallback
                        }
                    }
                    
                    // Scale nutrition based on quantity (per 100g)
                    double scaleFactor = quantityInGrams / 100.0;
                    NutrientInfo scaledNutrients = ingredientNutrients.multiply(scaleFactor);
                    totalNutrients = totalNutrients.add(scaledNutrients);
                }
            } catch (Exception e) {
                System.err.println("Error getting nutrition for ingredient: " + ingredient.getName() + ", " + e.getMessage());
                // Continue with other ingredients
            }
        }
        
        return totalNutrients;
    }
    
    /**
     * Refresh all coordinated components
     * Follows Mediator Pattern - centralizes component updates
     */
    private void refreshAllComponents() {
        if (mealJournalPanel != null) {
            mealJournalPanel.refreshMealJournal();
        }
        
        if (nutritionBreakdownPanel != null) {
            nutritionBreakdownPanel.refresh();
        }
    }
    
    /**
     * Show meal details dialog
     * 
     * @param meal Meal to show details for
     */
    private void showMealDetailsDialog(MealDTO meal) {
        StringBuilder details = new StringBuilder();
        details.append("Meal Details\n\n");
        details.append("Type: ").append(meal.getMealType().toUpperCase()).append("\n");
        details.append("Date: ").append(meal.getDate()).append("\n\n");
        details.append("Ingredients:\n");
        
        for (ca.nutrisci.application.dto.IngredientDTO ingredient : meal.getIngredients()) {
            details.append("• ").append(ingredient.getName());
            details.append(" (").append(ingredient.getQuantity()).append(" ").append(ingredient.getUnit()).append(")");
            details.append("\n");
        }
        
        if (meal.getNutrients() != null) {
            NutrientInfo nutrients = meal.getNutrients();
            details.append("\nNutrition:\n");
            details.append("Calories: ").append(String.format("%.1f", nutrients.getCalories())).append("\n");
            details.append("Protein: ").append(String.format("%.1f", nutrients.getProtein())).append("g\n");
            details.append("Carbs: ").append(String.format("%.1f", nutrients.getCarbs())).append("g\n");
            details.append("Fat: ").append(String.format("%.1f", nutrients.getFat())).append("g\n");
            details.append("Fiber: ").append(String.format("%.1f", nutrients.getFiber())).append("g\n");
        }
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(350, 300));
        
        JOptionPane.showMessageDialog(null, scrollPane, "Meal Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show success message for meal creation/update
     * 
     * @param mealType Type of meal
     * @param action Action performed (created/updated)
     * @param date Date of the meal
     * @param meal The meal object
     */
    private void showSuccessMessage(String mealType, String action, LocalDate date, MealDTO meal) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        String successMessage = String.format(
            "✅ %s meal %s successfully for %s!\n\n" +
            "📊 Nutrition Information:\n" +
            "• Calories: %.0f\n" +
            "• Protein: %.1fg\n" +
            "• Carbs: %.1fg\n" +
            "• Fat: %.1fg\n" +
            "• Fiber: %.1fg\n\n" +
            "🍽️ Ingredients: %s",
            mealType.toUpperCase(),
            action,
            dateStr,
            meal.getNutrients().getCalories(),
            meal.getNutrients().getProtein(),
            meal.getNutrients().getCarbs(),
            meal.getNutrients().getFat(),
            meal.getNutrients().getFiber(),
            meal.getIngredients().stream()
                .map(ingredient -> ingredient.getName() + " (" + ingredient.getQuantity() + " " + ingredient.getUnit() + ")")
                .collect(java.util.stream.Collectors.joining(", "))
        );
        
        String titleText = "Meal " + (action.equals("created") ? "Added" : "Updated") + " Successfully!";
        JOptionPane.showMessageDialog(null, successMessage, titleText, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show meal deleted message
     * 
     * @param mealType Type of meal that was deleted
     */
    private void showMealDeletedMessage(String mealType) {
        String deleteMessage = String.format(
            "🗑️ %s meal deleted successfully!\n\n" +
            "The meal was automatically removed because it had no ingredients.",
            mealType.toUpperCase()
        );
        
        JOptionPane.showMessageDialog(null, deleteMessage, "Meal Deleted", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update the active profile for all components
     * Follows Mediator Pattern - coordinates profile updates
     * 
     * @param newActiveProfile New active profile
     */
    public void setActiveProfile(ProfileDTO newActiveProfile) {
        this.activeProfile = newActiveProfile;
        this.currentViewDate = LocalDate.now(); // Reset to today for new profile
        
        // Update all components with new profile
        if (quickAddPanel != null) {
            quickAddPanel.setActiveProfile(newActiveProfile);
        }
        
        refreshAllComponents();
    }
    
    /**
     * Get the current view date
     * 
     * @return Current view date
     */
    public LocalDate getCurrentViewDate() {
        return currentViewDate;
    }
    
    /**
     * Result class for ingredient selection dialog
     */
    public static class IngredientSelectionResult {
        private final java.util.List<ca.nutrisci.application.dto.IngredientDTO> ingredients;
        private final boolean confirmed;
        
        public IngredientSelectionResult(java.util.List<ca.nutrisci.application.dto.IngredientDTO> ingredients, 
                                       boolean confirmed) {
            this.ingredients = ingredients;
            this.confirmed = confirmed;
        }
        
        // Backward compatibility constructor
        public IngredientSelectionResult(java.util.List<String> ingredientNames, 
                                       java.util.List<Double> quantities, 
                                       boolean confirmed) {
            this.confirmed = confirmed;
            this.ingredients = new java.util.ArrayList<>();
            
            if (ingredientNames != null) {
                for (int i = 0; i < ingredientNames.size(); i++) {
                    String name = ingredientNames.get(i);
                    double quantity = (quantities != null && i < quantities.size()) ? quantities.get(i) : 100.0;
                    this.ingredients.add(new ca.nutrisci.application.dto.IngredientDTO(name, quantity, "g"));
                }
            }
        }
        
        public java.util.List<ca.nutrisci.application.dto.IngredientDTO> getIngredients() { return ingredients; }
        public boolean isConfirmed() { return confirmed; }
        
        // Backward compatibility methods
        public java.util.List<String> getIngredientNames() {
            java.util.List<String> names = new java.util.ArrayList<>();
            if (ingredients != null) {
                for (ca.nutrisci.application.dto.IngredientDTO ingredient : ingredients) {
                    names.add(ingredient.getName());
                }
            }
            return names;
        }
        
        public java.util.List<Double> getQuantities() {
            java.util.List<Double> quantities = new java.util.ArrayList<>();
            if (ingredients != null) {
                for (ca.nutrisci.application.dto.IngredientDTO ingredient : ingredients) {
                    quantities.add(ingredient.getQuantity());
                }
            }
            return quantities;
        }
    }
    
} 