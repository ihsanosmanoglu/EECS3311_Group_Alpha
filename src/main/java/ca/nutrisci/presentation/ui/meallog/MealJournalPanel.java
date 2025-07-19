package ca.nutrisci.presentation.ui.meallog;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.application.services.observers.ProfileChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MealJournalPanel - Component for displaying and managing meal journal
 * 
 * PURPOSE:
 * - Displays meals for a selected date in chronological order
 * - Provides date-based filtering for meal history
 * - Handles meal viewing, editing, and deletion operations
 * - Follows Single Responsibility Principle by focusing only on meal journal functionality
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-1: Three-Tier Layered - Only depends on Application layer (IMealLogFacade)
 * - DD-2: Four Domain Façades - Uses IMealLogFacade for meal operations
 * - DD-8: Observer Pattern - Implements ProfileChangeListener and notifies of meal actions
 * - DD-9: Naming Conventions - MealJournalPanel follows <Domain><Component> pattern
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only meal journal display and interactions
 * - Open/Closed: Extensible for additional journal features without modification
 * - Liskov Substitution: Can be used anywhere a JPanel is expected
 * - Interface Segregation: Uses specific IMealLogFacade interface only
 * - Dependency Inversion: Depends on facade abstraction, not concrete implementation
 * 
 * @author NutriSci Development Team
 * @version 2.0
 * @since 1.0
 */
public class MealJournalPanel extends JPanel implements ProfileChangeListener {
    
    // Constants for consistency (DRY principle)
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Color PANEL_BORDER_COLOR = Color.GRAY;
    private static final String[] MEAL_ORDER = {"breakfast", "lunch", "dinner", "snack"};
    
    // Core dependencies (DD-1: Layered Architecture)
    private final IMealLogFacade mealLogFacade;
    
    // UI Components
    private JTextField selectedDateField;
    private JButton todayButton;
    private JList<String> mealJournalList;
    private DefaultListModel<String> journalListModel;
    private JButton viewMealDetailsButton;
    private JButton deleteMealButton;
    
    // Current state
    private ProfileDTO activeProfile;
    private LocalDate currentDate;
    private List<MealDTO> currentMeals;
    
    // Event handling (DD-8: Observer Pattern)
    private MealJournalListener listener;
    
    /**
     * Interface for handling meal journal events (DD-8: Observer Pattern)
     */
    public interface MealJournalListener {
        /**
         * Called when user wants to view meal details
         * @param meal Meal to view details for
         */
        void onViewMealDetails(MealDTO meal);
        
        /**
         * Called when user wants to delete a meal
         * @param meal Meal to delete
         */
        void onDeleteMeal(MealDTO meal);
        
        /**
         * Called when the journal date changes
         * @param newDate New date selected for journal viewing
         */
        void onDateChanged(LocalDate newDate);
    }
    
    /**
     * Constructor - Initializes the meal journal panel
     * 
     * @param mealLogFacade Facade for meal operations (DD-2: Domain Façades)
     * @param activeProfile Currently active profile
     */
    public MealJournalPanel(IMealLogFacade mealLogFacade, ProfileDTO activeProfile) {
        this.mealLogFacade = mealLogFacade;
        this.activeProfile = activeProfile;
        this.currentDate = LocalDate.now();
        this.currentMeals = List.of();
        
        initializeComponents();
        layoutComponents();
        attachEventListeners();
        refreshMealJournal();
    }
    
    /**
     * Initialize UI components with consistent styling
     * Follows Single Responsibility - only handles component initialization
     */
    private void initializeComponents() {
        // Date selection components
        selectedDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        selectedDateField.setPreferredSize(new Dimension(120, 25));
        selectedDateField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        todayButton = new JButton("Today");
        todayButton.setPreferredSize(new Dimension(80, 25));
        todayButton.setFont(new Font("Arial", Font.PLAIN, 11));
        
        // Meal journal list
        journalListModel = new DefaultListModel<>();
        mealJournalList = new JList<>(journalListModel);
        mealJournalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mealJournalList.setCellRenderer(new MealJournalCellRenderer());
        
        // Action buttons
        viewMealDetailsButton = new JButton("View Details");
        viewMealDetailsButton.setFont(new Font("Arial", Font.PLAIN, 11));
        viewMealDetailsButton.setEnabled(false);
        
        deleteMealButton = new JButton("Delete");
        deleteMealButton.setFont(new Font("Arial", Font.PLAIN, 11));
        deleteMealButton.setEnabled(false);
    }
    
    /**
     * Layout components using BorderLayout
     * Follows Open/Closed - easy to modify without changing component logic
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PANEL_BORDER_COLOR, 2), 
            "Meal Journal",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            TITLE_FONT
        ));
        
        // Date selector panel at the top
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        datePanel.add(new JLabel("View Date:"));
        datePanel.add(selectedDateField);
        datePanel.add(todayButton);
        add(datePanel, BorderLayout.NORTH);
        
        // Meal list in the center
        JScrollPane scrollPane = new JScrollPane(mealJournalList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // Action buttons at the bottom
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(viewMealDetailsButton);
        buttonPanel.add(deleteMealButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Attach event listeners to components
     * Follows Interface Segregation - each listener handles specific events
     */
    private void attachEventListeners() {
        // Date field listener
        selectedDateField.addActionListener(e -> handleDateChange());
        
        // Today button listener
        todayButton.addActionListener(e -> {
            selectedDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            handleDateChange();
        });
        
        // Meal list selection listener
        mealJournalList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Double-click to view details
        mealJournalList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    handleViewMealDetails();
                }
            }
        });
        
        // Action button listeners
        viewMealDetailsButton.addActionListener(e -> handleViewMealDetails());
        deleteMealButton.addActionListener(e -> handleDeleteMeal());
    }
    
    /**
     * Handle date change from user input
     * Follows Single Responsibility - handles only date change logic
     */
    private void handleDateChange() {
        try {
            String selectedDateStr = selectedDateField.getText().trim();
            LocalDate newDate = LocalDate.parse(selectedDateStr);
            
            if (!newDate.equals(currentDate)) {
                currentDate = newDate;
                refreshMealJournal();
                
                // Notify listener about date change (DD-8: Observer Pattern)
                if (listener != null) {
                    listener.onDateChanged(newDate);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing selected date: " + e.getMessage());
            // Reset to current date if invalid
            selectedDateField.setText(currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
    
    /**
     * Refresh the meal journal for the current date
     * 
     * LAYERED ARCHITECTURE (DD-1):
     * - Uses IMealLogFacade to get meals from Application layer
     * - No direct access to data layer or domain entities
     */
    public void refreshMealJournal() {
        try {
            if (activeProfile != null && currentDate != null) {
                // Get meals for the current date using domain facade (DD-2: Domain Façades)
                List<MealDTO> mealsForDate = mealLogFacade.getMealsForDate(currentDate)
                    .stream()
                    .filter(meal -> meal.getProfileId().equals(activeProfile.getId()))
                    .sorted(this::compareMealsByType)
                    .collect(Collectors.toList());
                
                currentMeals = mealsForDate;
                updateJournalDisplay();
            } else {
                currentMeals = List.of();
                updateJournalDisplay();
            }
        } catch (Exception e) {
            System.err.println("Error refreshing meal journal: " + e.getMessage());
            e.printStackTrace();
            
            // Show error state
            journalListModel.clear();
            journalListModel.addElement("Error loading meals");
        }
    }
    
    /**
     * Update the journal display with current meals
     * Follows Single Responsibility - handles only display updates
     */
    private void updateJournalDisplay() {
        journalListModel.clear();
        
        if (currentMeals.isEmpty()) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            journalListModel.addElement("No meals logged for " + dateStr);
        } else {
            for (MealDTO meal : currentMeals) {
                String displayText = formatMealDisplayText(meal);
                journalListModel.addElement(displayText);
            }
        }
        
        // Reset selection and button states
        mealJournalList.clearSelection();
        updateButtonStates();
    }
    
    /**
     * Format meal display text for the journal list
     * Follows DRY principle - centralized meal formatting
     * 
     * @param meal Meal to format
     * @return Formatted display text
     */
    private String formatMealDisplayText(MealDTO meal) {
        double calories = meal.getNutrients() != null ? meal.getNutrients().getCalories() : 0.0;
        return String.format("%s (%.0f cal)", 
            meal.getMealType().toUpperCase(),
            calories);
    }
    
    /**
     * Compare meals by type for sorting (breakfast, lunch, dinner, snack)
     * Follows Single Responsibility - handles only meal sorting logic
     * 
     * @param m1 First meal
     * @param m2 Second meal
     * @return Comparison result
     */
    private int compareMealsByType(MealDTO m1, MealDTO m2) {
        int index1 = java.util.Arrays.asList(MEAL_ORDER).indexOf(m1.getMealType().toLowerCase());
        int index2 = java.util.Arrays.asList(MEAL_ORDER).indexOf(m2.getMealType().toLowerCase());
        return Integer.compare(index1, index2);
    }
    
    /**
     * Handle view meal details action
     * Follows Single Responsibility - handles only view details logic
     */
    private void handleViewMealDetails() {
        int selectedIndex = mealJournalList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentMeals.size()) {
            MealDTO selectedMeal = currentMeals.get(selectedIndex);
            
            // Notify listener about view details request (DD-8: Observer Pattern)
            if (listener != null) {
                listener.onViewMealDetails(selectedMeal);
            }
        }
    }
    
    /**
     * Handle delete meal action
     * Follows Single Responsibility - handles only delete logic
     */
    private void handleDeleteMeal() {
        int selectedIndex = mealJournalList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentMeals.size()) {
            MealDTO selectedMeal = currentMeals.get(selectedIndex);
            
            // Confirm deletion
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this " + selectedMeal.getMealType() + " meal?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // Notify listener about delete request (DD-8: Observer Pattern)
                if (listener != null) {
                    listener.onDeleteMeal(selectedMeal);
                }
            }
        }
    }
    
    /**
     * Update button states based on current selection
     * Follows Single Responsibility - handles only button state management
     */
    private void updateButtonStates() {
        int selectedIndex = mealJournalList.getSelectedIndex();
        boolean hasValidSelection = selectedIndex >= 0 && 
                                   selectedIndex < currentMeals.size() && 
                                   !currentMeals.isEmpty();
        
        viewMealDetailsButton.setEnabled(hasValidSelection);
        deleteMealButton.setEnabled(hasValidSelection);
    }
    
    /**
     * Set the meal journal event listener
     * Follows Interface Segregation - provides only necessary callback registration
     * 
     * @param listener Listener for meal journal events
     */
    public void setMealJournalListener(MealJournalListener listener) {
        this.listener = listener;
    }
    
    /**
     * Set the display date for the journal
     * Follows Interface Segregation - provides only necessary date modification
     * 
     * @param date Date to display meals for
     */
    public void setDisplayDate(LocalDate date) {
        this.currentDate = date;
        selectedDateField.setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        refreshMealJournal();
    }
    
    /**
     * Set the display date for the journal without triggering change listeners
     * Used by mediator for date synchronization to prevent infinite loops
     * 
     * @param date Date to display meals for
     */
    public void setDisplayDateSilently(LocalDate date) {
        // Temporarily remove listeners
        ActionListener[] actionListeners = selectedDateField.getActionListeners();
        
        // Remove all listeners
        for (ActionListener listener : actionListeners) {
            selectedDateField.removeActionListener(listener);
        }
        
        // Set the date and refresh
        this.currentDate = date;
        selectedDateField.setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        refreshMealJournal();
        
        // Re-add listeners
        for (ActionListener listener : actionListeners) {
            selectedDateField.addActionListener(listener);
        }
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
     * Custom cell renderer for meal journal list
     * Follows Single Responsibility - handles only list item rendering
     */
    private static class MealJournalCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof String) {
                String mealText = (String) value;
                // Style based on meal content
                if (mealText.contains("cal")) {
                    setFont(new Font("Arial", Font.PLAIN, 12));
                } else if (mealText.contains("No meals") || mealText.contains("Error")) {
                    setFont(new Font("Arial", Font.ITALIC, 12));
                    setForeground(isSelected ? Color.WHITE : Color.GRAY);
                }
            }
            
            return this;
        }
    }
    
    // ProfileChangeListener implementation (DD-8: Observer Pattern)
    
    /**
     * Handle profile activation event
     * Updates journal for new active profile
     * 
     * @param activeProfile Newly activated profile
     */
    @Override
    public void onProfileActivated(ProfileDTO activeProfile) {
        this.activeProfile = activeProfile;
        this.currentDate = LocalDate.now(); // Reset to today for new profile
        selectedDateField.setText(currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        refreshMealJournal();
        System.out.println("✅ MealJournalPanel updated for new active profile: " + activeProfile.getName());
    }
    
    /**
     * Handle profile deactivation event
     * Clears journal if deactivated profile was our active profile
     * 
     * @param deactivatedProfile Profile that was deactivated
     */
    @Override
    public void onProfileDeactivated(ProfileDTO deactivatedProfile) {
        if (activeProfile != null && activeProfile.getId().equals(deactivatedProfile.getId())) {
            this.activeProfile = null;
            this.currentMeals = List.of();
            updateJournalDisplay();
        }
    }
    
    /**
     * Handle profile creation event
     * No action needed for meal journal
     * 
     * @param newProfile Newly created profile
     */
    @Override
    public void onProfileCreated(ProfileDTO newProfile) {
        // No action needed for meal journal
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
            // No need to refresh journal as profile updates don't affect meals
        }
    }
    
    /**
     * Handle profile deletion event
     * Clears journal if deleted profile was our active profile
     * 
     * @param deletedProfileId ID of deleted profile
     */
    @Override
    public void onProfileDeleted(String deletedProfileId) {
        if (activeProfile != null && activeProfile.getId().toString().equals(deletedProfileId)) {
            this.activeProfile = null;
            this.currentMeals = List.of();
            updateJournalDisplay();
            System.out.println("Active profile deleted - cleared meal journal");
        }
    }
} 