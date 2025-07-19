package ca.nutrisci.presentation.ui.meallog;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IMealLogFacade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * QuickAddPanel - Component for meal type quick-add functionality
 * 
 * PURPOSE:
 * - Provides quick-add buttons for different meal types (breakfast, lunch, dinner, snack)
 * - Handles date selection for meal logging
 * - Detects existing meals and switches to edit mode automatically
 * - Follows Single Responsibility Principle by focusing only on quick-add UI
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-1: Three-Tier Layered - Only depends on Application layer (IMealLogFacade)
 * - DD-2: Four Domain Façades - Uses IMealLogFacade for meal operations
 * - DD-8: Observer Pattern - Notifies parent components of user actions
 * - DD-9: Naming Conventions - QuickAddPanel follows <Domain><Component> pattern
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only quick-add button functionality
 * - Open/Closed: Extensible for new meal types without modification
 * - Liskov Substitution: Can be used anywhere a JPanel is expected
 * - Interface Segregation: Uses specific IMealLogFacade interface only
 * - Dependency Inversion: Depends on facade abstraction, not concrete implementation
 * 
 * @author NutriSci Development Team
 * @version 2.0
 * @since 1.0
 */
public class QuickAddPanel extends JPanel {
    
    // Constants for consistency (DRY principle)
    private static final String[] MEAL_TYPES = {"breakfast", "lunch", "dinner", "snack"};
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Dimension BUTTON_SIZE = new Dimension(120, 35);
    
    // Core dependencies (DD-1: Layered Architecture)
    private final IMealLogFacade mealLogFacade;
    
    // UI Components
    private JTextField dateField;
    private JButton quickAddBreakfastButton;
    private JButton quickAddLunchButton;
    private JButton quickAddDinnerButton;
    private JButton quickAddSnackButton;
    
    // Current state
    private ProfileDTO activeProfile;
    
    // Event handling (DD-8: Observer Pattern)
    private QuickAddListener listener;
    
    /**
     * Interface for handling quick-add events (DD-8: Observer Pattern)
     */
    public interface QuickAddListener {
        /**
         * Called when user wants to add/edit a meal
         * @param mealType Type of meal (breakfast, lunch, dinner, snack)
         * @param selectedDate Date for the meal
         * @param existingMeal Existing meal if found, null for new meal
         */
        void onQuickAddRequested(String mealType, LocalDate selectedDate, MealDTO existingMeal);
        
        /**
         * Called when the date selection changes in QuickAddPanel
         * @param newDate New date selected
         */
        void onDateChanged(LocalDate newDate);
    }
    
    /**
     * Constructor - Initializes the quick-add panel
     * 
     * @param mealLogFacade Facade for meal operations (DD-2: Domain Façades)
     * @param activeProfile Currently active profile
     */
    public QuickAddPanel(IMealLogFacade mealLogFacade, ProfileDTO activeProfile) {
        this.mealLogFacade = mealLogFacade;
        this.activeProfile = activeProfile;
        
        initializeComponents();
        layoutComponents();
        attachEventListeners();
    }
    
    /**
     * Initialize UI components with consistent styling
     * Follows Single Responsibility - only handles component initialization
     */
    private void initializeComponents() {
        // Date selection field
        dateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        dateField.setPreferredSize(new Dimension(120, 25));
        dateField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Quick-add buttons with distinct colors (DRY principle)
        quickAddBreakfastButton = createQuickAddButton("Add Breakfast", new Color(255, 230, 153));
        quickAddLunchButton = createQuickAddButton("Add Lunch", new Color(153, 255, 153));
        quickAddDinnerButton = createQuickAddButton("Add Dinner", new Color(153, 204, 255));
        quickAddSnackButton = createQuickAddButton("Add Snack", new Color(255, 153, 255));
    }
    
    /**
     * Create a quick-add button with consistent styling
     * Follows DRY principle - eliminates button styling duplication
     * 
     * @param text Button text
     * @param backgroundColor Button background color
     * @return Styled JButton
     */
    private JButton createQuickAddButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        return button;
    }
    
    /**
     * Layout components using BorderLayout
     * Follows Open/Closed - easy to modify without changing component logic
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2), 
            "Log new meal",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        // Date selection at top
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Date: "));
        datePanel.add(dateField);
        add(datePanel, BorderLayout.NORTH);
        
        // Quick add buttons in the center
        JPanel quickAddButtonPanel = new JPanel(new GridLayout(4, 1, 5, 10));
        quickAddButtonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        quickAddButtonPanel.add(quickAddBreakfastButton);
        quickAddButtonPanel.add(quickAddLunchButton);
        quickAddButtonPanel.add(quickAddDinnerButton);
        quickAddButtonPanel.add(quickAddSnackButton);
        
        add(quickAddButtonPanel, BorderLayout.CENTER);
    }
    
    /**
     * Attach event listeners to components
     * Follows Interface Segregation - each listener handles specific events
     */
    private void attachEventListeners() {
        // Date field change listener (DD-8: Observer Pattern)
        dateField.addActionListener(e -> handleDateChange());
        dateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                handleDateChange();
            }
        });
        
        // Quick-add button listeners using method references (Clean Code)
        quickAddBreakfastButton.addActionListener(e -> handleQuickAdd("breakfast"));
        quickAddLunchButton.addActionListener(e -> handleQuickAdd("lunch"));
        quickAddDinnerButton.addActionListener(e -> handleQuickAdd("dinner"));
        quickAddSnackButton.addActionListener(e -> handleQuickAdd("snack"));
    }
    
    /**
     * Handle date change from user input
     * Notifies mediator to synchronize date across all components (DD-8: Observer Pattern)
     */
    private void handleDateChange() {
        if (listener == null) return;
        
        try {
            String selectedDateStr = dateField.getText().trim();
            LocalDate newDate = LocalDate.parse(selectedDateStr);
            
            // Notify listener about date change (DD-8: Observer Pattern)
            listener.onDateChanged(newDate);
            
        } catch (Exception e) {
            // Reset to current date if invalid format
            dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
    
    /**
     * Handle quick-add button clicks
     * 
     * BUSINESS LOGIC:
     * - Automatically detects if meal already exists for selected date
     * - Switches to edit mode if meal exists, otherwise creates new meal
     * - Uses domain facade for meal existence checking (DD-2)
     * 
     * @param mealType Type of meal to add/edit
     */
    private void handleQuickAdd(String mealType) {
        if (listener == null) {
            System.err.println("No QuickAddListener set - cannot handle quick add");
            return;
        }
        
        if (activeProfile == null) {
            JOptionPane.showMessageDialog(this, "No active profile selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Get the selected date from the UI
            LocalDate selectedDate = getSelectedDateFromUI();
            
            // Check for existing meal using domain facade (DD-2: Domain Façades)
            MealDTO existingMeal = findExistingMeal(mealType, selectedDate);
            
            // Notify listener about the quick-add request (DD-8: Observer Pattern)
            listener.onQuickAddRequested(mealType, selectedDate, existingMeal);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error handling quick add: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Get the currently selected date from the UI date field
     * 
     * ERROR HANDLING:
     * - Gracefully handles invalid date formats
     * - Defaults to today's date if parsing fails
     * 
     * @return LocalDate representing the selected date, or today's date if parsing fails
     */
    private LocalDate getSelectedDateFromUI() {
        try {
            String selectedDateStr = dateField.getText().trim();
            return LocalDate.parse(selectedDateStr);
        } catch (Exception e) {
            System.err.println("Error parsing selected date, defaulting to today: " + e.getMessage());
            return LocalDate.now();
        }
    }
    
    /**
     * Find existing meal for the given type and date using domain facade
     * 
     * LAYERED ARCHITECTURE (DD-1):
     * - Uses IMealLogFacade to access business logic
     * - No direct access to data layer or domain entities
     * 
     * @param mealType Type of meal to search for
     * @param date Date to search for meals
     * @return MealDTO if found, null otherwise
     */
    private MealDTO findExistingMeal(String mealType, LocalDate date) {
        try {
            // Use facade to check meal existence (DD-2: Domain Façades)
            if (mealLogFacade.mealTypeExistsForDate(activeProfile.getId(), date, mealType)) {
                // Get meals for the date and find the specific meal type
                return mealLogFacade.getMealsForDate(date)
                    .stream()
                    .filter(meal -> meal.getMealType().equalsIgnoreCase(mealType))
                    .findFirst()
                    .orElse(null);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error finding existing meal: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Set the quick-add event listener
     * Follows Interface Segregation - provides only necessary callback registration
     * 
     * @param listener Listener for quick-add events
     */
    public void setQuickAddListener(QuickAddListener listener) {
        this.listener = listener;
    }
    
    /**
     * Update the active profile
     * Follows Single Responsibility - handles only profile updates
     * 
     * @param activeProfile New active profile
     */
    public void setActiveProfile(ProfileDTO activeProfile) {
        this.activeProfile = activeProfile;
    }
    
    /**
     * Get the selected date as a string
     * Follows Interface Segregation - provides only necessary data access
     * 
     * @return Selected date as string
     */
    public String getSelectedDate() {
        return dateField.getText();
    }
    
    /**
     * Set the date field value
     * Follows Interface Segregation - provides only necessary data modification
     * 
     * @param date Date to set
     */
    public void setSelectedDate(LocalDate date) {
        dateField.setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    
    /**
     * Set the date field value without triggering change listeners
     * Used by mediator for date synchronization to prevent infinite loops
     * 
     * @param date Date to set
     */
    public void setSelectedDateSilently(LocalDate date) {
        // Temporarily remove listeners
        ActionListener[] actionListeners = dateField.getActionListeners();
        java.awt.event.FocusListener[] focusListeners = dateField.getFocusListeners();
        
        // Remove all listeners
        for (ActionListener listener : actionListeners) {
            dateField.removeActionListener(listener);
        }
        for (java.awt.event.FocusListener listener : focusListeners) {
            dateField.removeFocusListener(listener);
        }
        
        // Set the date
        dateField.setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Re-add listeners
        for (ActionListener listener : actionListeners) {
            dateField.addActionListener(listener);
        }
        for (java.awt.event.FocusListener listener : focusListeners) {
            dateField.addFocusListener(listener);
        }
    }
} 