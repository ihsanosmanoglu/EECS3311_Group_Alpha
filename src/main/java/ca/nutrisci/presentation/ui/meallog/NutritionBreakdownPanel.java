package ca.nutrisci.presentation.ui.meallog;

import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.application.services.observers.ProfileChangeListener;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * NutritionBreakdownPanel - Component for displaying nutritional breakdown
 * 
 * PURPOSE:
 * - Displays real-time nutritional totals for a selected date
 * - Updates automatically when meals are added/edited/deleted
 * - Provides clear visualization of daily nutrient intake
 * - Follows Single Responsibility Principle by focusing only on nutrition display
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-1: Three-Tier Layered - Only depends on Application layer (IMealLogFacade)
 * - DD-2: Four Domain Façades - Uses IMealLogFacade for nutrition calculations
 * - DD-8: Observer Pattern - Implements ProfileChangeListener for profile updates
 * - DD-9: Naming Conventions - NutritionBreakdownPanel follows <Domain><Component> pattern
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only nutrition breakdown display
 * - Open/Closed: Extensible for additional nutrients without modification
 * - Liskov Substitution: Can be used anywhere a JPanel is expected
 * - Interface Segregation: Uses specific IMealLogFacade interface only
 * - Dependency Inversion: Depends on facade abstraction, not concrete implementation
 * 
 * @author NutriSci Development Team
 * @version 2.0
 * @since 1.0
 */
public class NutritionBreakdownPanel extends JPanel implements ProfileChangeListener {
    
    // Constants for consistency (DRY principle)
    private static final Font NUTRIENT_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Color PANEL_BORDER_COLOR = Color.GRAY;
    
    // Core dependencies (DD-1: Layered Architecture)
    private final IMealLogFacade mealLogFacade;
    
    // UI Components for nutrient display
    private JLabel proteinLabel;
    private JLabel fatLabel;
    private JLabel carbsLabel;
    private JLabel caloriesLabel;
    private JLabel fiberLabel;
    private JLabel dateDisplayLabel;
    
    // Current state
    private ProfileDTO activeProfile;
    private LocalDate currentDate;
    
    // Event handling (DD-8: Observer Pattern)
    private NutritionUpdateListener listener;
    
    /**
     * Interface for handling nutrition update events (DD-8: Observer Pattern)
     */
    public interface NutritionUpdateListener {
        /**
         * Called when nutrition data is updated
         * @param date Date for which nutrition was updated
         * @param nutrients Updated nutrition information
         */
        void onNutritionUpdated(LocalDate date, NutrientInfo nutrients);
    }
    
    /**
     * Constructor - Initializes the nutrition breakdown panel
     * 
     * @param mealLogFacade Facade for meal operations (DD-2: Domain Façades)
     * @param activeProfile Currently active profile
     */
    public NutritionBreakdownPanel(IMealLogFacade mealLogFacade, ProfileDTO activeProfile) {
        this.mealLogFacade = mealLogFacade;
        this.activeProfile = activeProfile;
        this.currentDate = LocalDate.now();
        
        initializeComponents();
        layoutComponents();
        updateNutritionDisplay();
    }
    
    /**
     * Initialize UI components with consistent styling
     * Follows Single Responsibility - only handles component initialization
     */
    private void initializeComponents() {
        // Date display label
        dateDisplayLabel = new JLabel("Today's Breakdown");
        dateDisplayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dateDisplayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Nutrition display labels (DRY principle)
        proteinLabel = createNutrientLabel("Protein: 0g");
        fatLabel = createNutrientLabel("Fat: 0g");
        carbsLabel = createNutrientLabel("Carbs: 0g");
        caloriesLabel = createNutrientLabel("Calories: 0");
        fiberLabel = createNutrientLabel("Fiber: 0g");
    }
    
    /**
     * Create a nutrient display label with consistent styling
     * Follows DRY principle - eliminates label styling duplication
     * 
     * @param text Initial label text
     * @return Styled JLabel for nutrient display
     */
    private JLabel createNutrientLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(NUTRIENT_FONT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    /**
     * Layout components using BorderLayout
     * Follows Open/Closed - easy to modify without changing component logic
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PANEL_BORDER_COLOR, 2), 
            "Today's breakdown",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        // Date display at top
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.add(dateDisplayLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Nutrition breakdown in center
        JPanel nutritionPanel = new JPanel(new GridLayout(5, 1, 5, 10));
        nutritionPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        
        nutritionPanel.add(proteinLabel);
        nutritionPanel.add(fatLabel);
        nutritionPanel.add(carbsLabel);
        nutritionPanel.add(fiberLabel);
        nutritionPanel.add(caloriesLabel);
        
        add(nutritionPanel, BorderLayout.CENTER);
    }
    
    /**
     * Update the nutrition display for the current date and profile
     * 
     * LAYERED ARCHITECTURE (DD-1):
     * - Uses IMealLogFacade to get daily totals from Application layer
     * - No direct access to data layer or domain entities
     * 
     * ERROR HANDLING:
     * - Gracefully handles null values and exceptions
     * - Provides user-friendly error messages
     * - Maintains UI consistency even when errors occur
     */
    public void updateNutritionDisplay() {
        try {
            if (activeProfile != null && currentDate != null) {
                // Get daily totals using domain facade (DD-2: Domain Façades)
                NutrientInfo dailyTotals = mealLogFacade.getDailyTotals(activeProfile.getId(), currentDate);
                
                if (dailyTotals != null) {
                    // Update labels with formatted nutrition values
                    proteinLabel.setText(String.format("Protein: %.1fg", dailyTotals.getProtein()));
                    fatLabel.setText(String.format("Fat: %.1fg", dailyTotals.getFat()));
                    carbsLabel.setText(String.format("Carbs: %.1fg", dailyTotals.getCarbs()));
                    fiberLabel.setText(String.format("Fiber: %.1fg", dailyTotals.getFiber()));
                    caloriesLabel.setText(String.format("Calories: %.0f", dailyTotals.getCalories()));
                    
                    // Notify listener if set (DD-8: Observer Pattern)
                    if (listener != null) {
                        listener.onNutritionUpdated(currentDate, dailyTotals);
                    }
                } else {
                    // Display zero values when no data available
                    clearNutritionDisplay();
                }
                
                // Update date display
                updateDateDisplay();
                
            } else {
                clearNutritionDisplay();
            }
        } catch (Exception e) {
            System.err.println("Error updating nutrition display: " + e.getMessage());
            e.printStackTrace();
            
            // Show error state but maintain UI consistency
            proteinLabel.setText("Protein: Error");
            fatLabel.setText("Fat: Error");
            carbsLabel.setText("Carbs: Error");
            fiberLabel.setText("Fiber: Error");
            caloriesLabel.setText("Calories: Error");
        }
    }
    
    /**
     * Clear the nutrition display to show zero values
     * Follows Single Responsibility - handles only display clearing
     */
    private void clearNutritionDisplay() {
        proteinLabel.setText("Protein: 0.0g");
        fatLabel.setText("Fat: 0.0g");
        carbsLabel.setText("Carbs: 0.0g");
        fiberLabel.setText("Fiber: 0.0g");
        caloriesLabel.setText("Calories: 0");
    }
    
    /**
     * Update the date display based on current date
     * Follows Single Responsibility - handles only date display updates
     */
    private void updateDateDisplay() {
        if (currentDate != null) {
            if (currentDate.equals(LocalDate.now())) {
                dateDisplayLabel.setText("Today's Breakdown");
            } else {
                dateDisplayLabel.setText(currentDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " Breakdown");
            }
        } else {
            dateDisplayLabel.setText("Nutrition Breakdown");
        }
    }
    
    /**
     * Set the date for nutrition display
     * Follows Interface Segregation - provides only necessary date modification
     * 
     * @param date Date to display nutrition for
     */
    public void setDisplayDate(LocalDate date) {
        this.currentDate = date;
        updateNutritionDisplay();
    }
    
    /**
     * Get the current display date
     * Follows Interface Segregation - provides only necessary data access
     * 
     * @return Current display date
     */
    public LocalDate getDisplayDate() {
        return currentDate;
    }
    
    /**
     * Set the nutrition update listener
     * Follows Interface Segregation - provides only necessary callback registration
     * 
     * @param listener Listener for nutrition update events
     */
    public void setNutritionUpdateListener(NutritionUpdateListener listener) {
        this.listener = listener;
    }
    
    /**
     * Refresh the nutrition display (useful after meal changes)
     * Follows Interface Segregation - provides only necessary refresh functionality
     */
    public void refresh() {
        updateNutritionDisplay();
    }
    
    // ProfileChangeListener implementation (DD-8: Observer Pattern)
    
    /**
     * Handle profile activation event
     * Updates display for new active profile
     * 
     * @param activeProfile Newly activated profile
     */
    @Override
    public void onProfileActivated(ProfileDTO activeProfile) {
        this.activeProfile = activeProfile;
        this.currentDate = LocalDate.now(); // Reset to today for new profile
        updateNutritionDisplay();
        System.out.println("✅ NutritionBreakdownPanel updated for new active profile: " + activeProfile.getName());
    }
    
    /**
     * Handle profile deactivation event
     * Clears display if deactivated profile was our active profile
     * 
     * @param deactivatedProfile Profile that was deactivated
     */
    @Override
    public void onProfileDeactivated(ProfileDTO deactivatedProfile) {
        if (activeProfile != null && activeProfile.getId().equals(deactivatedProfile.getId())) {
            this.activeProfile = null;
            clearNutritionDisplay();
        }
    }
    
    /**
     * Handle profile creation event
     * No action needed for nutrition display
     * 
     * @param newProfile Newly created profile
     */
    @Override
    public void onProfileCreated(ProfileDTO newProfile) {
        // No action needed for nutrition display
    }
    
    /**
     * Handle profile update event
     * Updates profile reference if it's our active profile
     * 
     * @param updatedProfile Updated profile
     */
    @Override
    public void onProfileUpdated(ProfileDTO updatedProfile) {
        if (activeProfile != null && activeProfile.getId().equals(updatedProfile.getId())) {
            this.activeProfile = updatedProfile;
            // No need to refresh nutrition display as profile updates don't affect nutrition
        }
    }
    
    /**
     * Handle profile deletion event
     * Clears display if deleted profile was our active profile
     * 
     * @param deletedProfileId ID of deleted profile
     */
    @Override
    public void onProfileDeleted(String deletedProfileId) {
        if (activeProfile != null && activeProfile.getId().toString().equals(deletedProfileId)) {
            this.activeProfile = null;
            clearNutritionDisplay();
            System.out.println("Active profile deleted - cleared nutrition display");
        }
    }
} 