package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.dto.SwapGoalDTO;
import ca.nutrisci.application.dto.GoalNutrientDTO;
import ca.nutrisci.application.dto.SwapDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.facades.ISwapFacade;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.application.services.observers.ProfileChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SwapPanel - Main panel for food swap functionality
 * 
 * PURPOSE:
 * - Provides interface for getting food swap suggestions
 * - Allows users to apply food swaps to their meals
 * - Displays nutritional impact of proposed swaps
 * - Follows the same architecture pattern as MealLogPanel
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-1: Three-Tier Layered - Only depends on Application layer (ISwapFacade)
 * - DD-2: Four Domain Façades - Uses ISwapFacade for swap operations
 * - DD-8: Observer Pattern - Implements ProfileChangeListener for profile updates
 * - DD-9: Naming Conventions - SwapPanel follows established naming patterns
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only swap UI coordination
 * - Open/Closed: Extensible for additional swap features
 * - Liskov Substitution: Can be used anywhere a JPanel is expected
 * - Interface Segregation: Uses specific facade interfaces only
 * - Dependency Inversion: Depends on abstractions, not concrete implementations
 */
public class SwapPanel extends JPanel implements ProfileChangeListener {
    
    // Core dependencies (DD-1: Layered Architecture, DD-2: Domain Façades)
    private final ISwapFacade swapFacade;
    private IMealLogFacade mealLogFacade; // For loading meal history
    
    // UI Components
    private JPanel goalSelectionPanel;
    private JPanel suggestionsPanel;
    private JPanel mealSelectionPanel;
    
    // Goal selection components
    private JPanel goal1Panel;
    private JPanel goal2Panel;
    private JComboBox<String> goal1TargetCombo;
    private JComboBox<String> goal1ActionCombo;
    private JComboBox<String> goal1IntensityCombo;
    private JComboBox<String> goal1PercentageCombo;
    private JComboBox<String> goal2TargetCombo;
    private JComboBox<String> goal2ActionCombo;
    private JComboBox<String> goal2IntensityCombo;
    private JComboBox<String> goal2PercentageCombo;
    private JButton addGoalButton;
    private JButton removeGoalButton;
    private JButton getSuggestionsButton;
    
    // Suggestions display components
    private JPanel suggestionsListPanel;
    private JScrollPane suggestionsScrollPane;
    
    // Meal selection components (similar to MealJournalPanel)
    private JTextField selectedDateField;
    private JButton todayButton;
    private JList<String> mealSelectionList;
    private DefaultListModel<String> mealListModel;
    private JButton selectMealButton;
    
    // Current state
    private ProfileDTO activeProfile;
    private boolean dualGoalsEnabled = false;
    private List<SwapDTO> currentSuggestions = new ArrayList<>();
    private LocalDate currentMealDate;
    private List<MealDTO> currentMeals = new ArrayList<>();
    private MealDTO selectedMeal;
    
    // Constants
    private static final String[] NUTRIENTS = {"Calories", "Protein", "Carbohydrates", "Fat", "Fiber"};
    private static final String[] ACTIONS = {"Decrease", "Increase"};
    private static final String[] INTENSITIES = {"High", "Normal", "Precise"};
    private static final String[] PERCENTAGES = {"5%", "10%", "15%", "20%", "25%", "30%", "35%", "40%", "45%", "50%"};
    
    /**
     * Constructor - Initializes the swap panel
     * 
     * @param swapFacade Facade for swap operations (DD-2: Domain Façades)
     * @param activeProfile Currently active user profile
     */
    public SwapPanel(ISwapFacade swapFacade, ProfileDTO activeProfile) {
        this.swapFacade = swapFacade;
        this.activeProfile = activeProfile;
        this.currentMealDate = LocalDate.now();
        
        // Template Method pattern - standardized initialization sequence
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        System.out.println("✅ SwapPanel initialized for profile: " + 
                         (activeProfile != null ? activeProfile.getName() : "none"));
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Goal Selection Panel - advanced dropdown interface
        goalSelectionPanel = createGoalSelectionPanel();
        
        // Suggestions Panel - display up to 4 swap suggestions
        suggestionsPanel = createSuggestionsPanel();
        
        // Meal Selection Panel - where users select which meal to apply swaps to
        mealSelectionPanel = createMealSelectionPanel();
        
        System.out.println("✅ SwapPanel components initialized");
    }
    
    /**
     * Create the advanced goal selection panel with dropdown menus
     */
    private JPanel createGoalSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nutrition Goals"));
        panel.setPreferredSize(new Dimension(350, 400));
        
        // Title
        JLabel titleLabel = new JLabel("Select Your Nutrition Goals", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Goals container
        JPanel goalsContainer = new JPanel();
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Goal 1 (always visible)
        goal1Panel = createGoalPanel("Goal 1", 1);
        goalsContainer.add(goal1Panel);
        
        // Goal 2 (initially hidden)
        goal2Panel = createGoalPanel("Goal 2", 2);
        goal2Panel.setVisible(false);
        goalsContainer.add(goal2Panel);
        
        // Add/Remove goal buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        addGoalButton = new JButton("Add Second Goal");
        addGoalButton.setFont(new Font("Arial", Font.BOLD, 13));
        addGoalButton.setBackground(new Color(33, 150, 243));
        addGoalButton.setForeground(Color.WHITE);
        addGoalButton.setFocusPainted(false);
        addGoalButton.setBorderPainted(false);
        addGoalButton.setOpaque(true);
        
        removeGoalButton = new JButton("Remove Second Goal");
        removeGoalButton.setFont(new Font("Arial", Font.BOLD, 13));
        removeGoalButton.setBackground(new Color(244, 67, 54));
        removeGoalButton.setForeground(Color.WHITE);
        removeGoalButton.setFocusPainted(false);
        removeGoalButton.setBorderPainted(false);
        removeGoalButton.setOpaque(true);
        removeGoalButton.setVisible(false);
        
        buttonPanel.add(addGoalButton);
        buttonPanel.add(removeGoalButton);
        
        // Get Suggestions button
        getSuggestionsButton = new JButton("Get Swap Suggestions");
        getSuggestionsButton.setFont(new Font("Arial", Font.BOLD, 14));
        getSuggestionsButton.setBackground(new Color(76, 175, 80));
        getSuggestionsButton.setForeground(Color.WHITE);
        getSuggestionsButton.setFocusPainted(false);
        getSuggestionsButton.setBorderPainted(false);
        getSuggestionsButton.setOpaque(true);
        getSuggestionsButton.setPreferredSize(new Dimension(200, 40));
        
        JPanel suggestionsButtonPanel = new JPanel(new FlowLayout());
        suggestionsButtonPanel.add(getSuggestionsButton);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(goalsContainer, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(suggestionsButtonPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create a single goal panel with dropdown menus
     */
    private JPanel createGoalPanel(String title, int goalNumber) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2), 
            title, 
            0, 
            0, 
            new Font("Arial", Font.BOLD, 14), 
            Color.BLACK
        ));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(300, 140));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE); // Ensure white background for better contrast
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Increase spacing for better readability
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nutrient selection
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nutrientLabel = new JLabel("Nutrient:");
        nutrientLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nutrientLabel.setForeground(Color.BLACK);
        formPanel.add(nutrientLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> targetCombo = new JComboBox<>(NUTRIENTS);
        targetCombo.setPreferredSize(new Dimension(120, 25));
        formPanel.add(targetCombo, gbc);
        
        // Action selection
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        actionLabel.setForeground(Color.BLACK);
        formPanel.add(actionLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> actionCombo = new JComboBox<>(ACTIONS);
        actionCombo.setPreferredSize(new Dimension(120, 25));
        formPanel.add(actionCombo, gbc);
        
        // Intensity selection
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel intensityLabel = new JLabel("Intensity:");
        intensityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        intensityLabel.setForeground(Color.BLACK);
        formPanel.add(intensityLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> intensityCombo = new JComboBox<>(INTENSITIES);
        intensityCombo.setPreferredSize(new Dimension(120, 25));
        intensityCombo.setSelectedItem("Normal");
        formPanel.add(intensityCombo, gbc);
        
        // Percentage selection (always visible, enabled only for Precise)
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel percentageLabel = new JLabel("Percentage:");
        percentageLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Make label more visible
        percentageLabel.setForeground(Color.GRAY); // Start with gray text
        formPanel.add(percentageLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> percentageCombo = new JComboBox<>(PERCENTAGES);
        percentageCombo.setPreferredSize(new Dimension(120, 25));
        percentageCombo.setSelectedItem("20%"); // Default for Normal
        percentageCombo.setEnabled(false); // Disabled by default (Normal intensity)
        percentageCombo.setBackground(new Color(240, 240, 240)); // Start with gray background
        formPanel.add(percentageCombo, gbc);
        
        // Store references based on goal number
        if (goalNumber == 1) {
            goal1TargetCombo = targetCombo;
            goal1ActionCombo = actionCombo;
            goal1IntensityCombo = intensityCombo;
            goal1PercentageCombo = percentageCombo;
        } else {
            goal2TargetCombo = targetCombo;
            goal2ActionCombo = actionCombo;
            goal2IntensityCombo = intensityCombo;
            goal2PercentageCombo = percentageCombo;
        }
        
        // Setup intensity change listener
        intensityCombo.addActionListener(e -> {
            String selectedIntensity = (String) intensityCombo.getSelectedItem();
            boolean isPrecise = "Precise".equals(selectedIntensity);
            
            // Always show percentage, but enable/disable based on intensity
            percentageCombo.setEnabled(isPrecise);
            
            // Auto-set percentage for High/Normal, keep current for Precise
            if ("High".equals(selectedIntensity)) {
                percentageCombo.setSelectedItem("30%");
            } else if ("Normal".equals(selectedIntensity)) {
                percentageCombo.setSelectedItem("20%");
            }
            // For Precise, keep whatever is currently selected
            
            // Visual feedback - change background color when disabled
            if (isPrecise) {
                percentageCombo.setBackground(Color.WHITE);
                percentageLabel.setForeground(Color.BLACK);
            } else {
                percentageCombo.setBackground(new Color(240, 240, 240)); // Light gray
                percentageLabel.setForeground(Color.GRAY);
            }
            
            panel.revalidate();
            panel.repaint();
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Create the suggestions display panel
     */
    private JPanel createSuggestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Swap Suggestions"));
        panel.setPreferredSize(new Dimension(400, 400));
        
        // Title
        JLabel titleLabel = new JLabel("Suggestions (Max 4)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Suggestions list panel
        suggestionsListPanel = new JPanel();
        suggestionsListPanel.setLayout(new BoxLayout(suggestionsListPanel, BoxLayout.Y_AXIS));
        
        // Scroll pane for suggestions
        suggestionsScrollPane = new JScrollPane(suggestionsListPanel);
        suggestionsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        suggestionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Initial placeholder
        showPlaceholderMessage();
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(suggestionsScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create the meal selection panel (similar to MealJournalPanel)
     */
    private JPanel createMealSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2), 
            "Select Meal for Swaps", 
            0, 
            0, 
            new Font("Arial", Font.BOLD, 14), 
            Color.BLACK
        ));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(350, 400));
        
        // Date selector panel at the top
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        datePanel.setBackground(Color.WHITE);
        
        JLabel dateLabel = new JLabel("View Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        datePanel.add(dateLabel);
        
        selectedDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        selectedDateField.setPreferredSize(new Dimension(120, 25));
        selectedDateField.setFont(new Font("Arial", Font.PLAIN, 12));
        datePanel.add(selectedDateField);
        
        todayButton = new JButton("Today");
        todayButton.setPreferredSize(new Dimension(80, 25));
        todayButton.setFont(new Font("Arial", Font.BOLD, 11));
        todayButton.setBackground(new Color(33, 150, 243));
        todayButton.setForeground(Color.WHITE);
        todayButton.setFocusPainted(false);
        todayButton.setBorderPainted(false);
        todayButton.setOpaque(true);
        datePanel.add(todayButton);
        
        panel.add(datePanel, BorderLayout.NORTH);
        
        // Meal list in the center
        mealListModel = new DefaultListModel<>();
        mealSelectionList = new JList<>(mealListModel);
        mealSelectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mealSelectionList.setCellRenderer(new MealSelectionCellRenderer());
        mealSelectionList.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(mealSelectionList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Select meal button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        selectMealButton = new JButton("Select This Meal");
        selectMealButton.setFont(new Font("Arial", Font.BOLD, 12));
        selectMealButton.setBackground(new Color(76, 175, 80));
        selectMealButton.setForeground(Color.WHITE);
        selectMealButton.setFocusPainted(false);
        selectMealButton.setBorderPainted(false);
        selectMealButton.setOpaque(true);
        selectMealButton.setEnabled(false);
        selectMealButton.setPreferredSize(new Dimension(150, 35));
        
        buttonPanel.add(selectMealButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Layout all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main container with proper spacing
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Food Swap Engine - Smart Nutrition Recommendations", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Layout the three panels horizontally
        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        contentPanel.add(goalSelectionPanel);
        contentPanel.add(suggestionsPanel);
        contentPanel.add(mealSelectionPanel);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        
        System.out.println("✅ SwapPanel layout completed");
    }
    
    /**
     * Setup event handlers for all interactive components
     */
    private void setupEventHandlers() {
        // Add goal button
        addGoalButton.addActionListener(e -> {
            dualGoalsEnabled = true;
            goal2Panel.setVisible(true);
            addGoalButton.setVisible(false);
            removeGoalButton.setVisible(true);
            goalSelectionPanel.revalidate();
            goalSelectionPanel.repaint();
            System.out.println("✅ Second goal enabled");
        });
        
        // Remove goal button
        removeGoalButton.addActionListener(e -> {
            dualGoalsEnabled = false;
            goal2Panel.setVisible(false);
            addGoalButton.setVisible(true);
            removeGoalButton.setVisible(false);
            goalSelectionPanel.revalidate();
            goalSelectionPanel.repaint();
            System.out.println("✅ Second goal disabled");
        });
        
        // Get suggestions button
        getSuggestionsButton.addActionListener(e -> generateSuggestions());
        
        // Meal selection event handlers
        setupMealSelectionEventHandlers();
        
        System.out.println("✅ SwapPanel event handlers configured");
    }
    
    /**
     * Setup event handlers for meal selection components
     */
    private void setupMealSelectionEventHandlers() {
        // Date field listener
        selectedDateField.addActionListener(e -> handleMealDateChange());
        
        // Today button listener
        todayButton.addActionListener(e -> {
            selectedDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            handleMealDateChange();
        });
        
        // Meal list selection listener
        mealSelectionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateMealSelectionButtonState();
            }
        });
        
        // Select meal button listener
        selectMealButton.addActionListener(e -> handleMealSelection());
        
        // Double-click to select meal
        mealSelectionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    handleMealSelection();
                }
            }
        });
    }
    
    /**
     * Handle date change for meal selection
     */
    private void handleMealDateChange() {
        try {
            String selectedDateStr = selectedDateField.getText().trim();
            LocalDate newDate = LocalDate.parse(selectedDateStr);
            
            if (!newDate.equals(currentMealDate)) {
                currentMealDate = newDate;
                refreshMealSelection();
            }
        } catch (Exception e) {
            System.err.println("Error parsing selected date: " + e.getMessage());
            // Reset to current date if invalid
            selectedDateField.setText(currentMealDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
    
    /**
     * Refresh the meal selection list for the current date
     */
    private void refreshMealSelection() {
        try {
            if (activeProfile != null && currentMealDate != null && mealLogFacade != null) {
                // Get meals for the current date
                List<MealDTO> mealsForDate = mealLogFacade.getMealsForDate(currentMealDate)
                    .stream()
                    .filter(meal -> meal.getProfileId().equals(activeProfile.getId()))
                    .sorted(this::compareMealsByType)
                    .collect(Collectors.toList());
                
                currentMeals = mealsForDate;
                updateMealSelectionDisplay();
            } else {
                currentMeals = new ArrayList<>();
                updateMealSelectionDisplay();
            }
        } catch (Exception e) {
            System.err.println("Error refreshing meal selection: " + e.getMessage());
            e.printStackTrace();
            
            // Show error state
            mealListModel.clear();
            mealListModel.addElement("Error loading meals");
        }
    }
    
    /**
     * Update the meal selection display
     */
    private void updateMealSelectionDisplay() {
        mealListModel.clear();
        
        if (currentMeals.isEmpty()) {
            String dateStr = currentMealDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            mealListModel.addElement("No meals logged for " + dateStr);
        } else {
            for (MealDTO meal : currentMeals) {
                String displayText = formatMealDisplayText(meal);
                mealListModel.addElement(displayText);
            }
        }
        
        // Reset selection and button states
        mealSelectionList.clearSelection();
        selectedMeal = null;
        updateMealSelectionButtonState();
    }
    
    /**
     * Format meal display text (similar to MealJournalPanel)
     */
    private String formatMealDisplayText(MealDTO meal) {
        double calories = meal.getNutrients() != null ? meal.getNutrients().getCalories() : 0.0;
        return String.format("%s (%.0f cal)", 
            meal.getMealType().toUpperCase(),
            calories);
    }
    
    /**
     * Compare meals by type for sorting (breakfast, lunch, dinner, snack)
     */
    private int compareMealsByType(MealDTO m1, MealDTO m2) {
        String[] mealOrder = {"breakfast", "lunch", "dinner", "snack"};
        int index1 = java.util.Arrays.asList(mealOrder).indexOf(m1.getMealType().toLowerCase());
        int index2 = java.util.Arrays.asList(mealOrder).indexOf(m2.getMealType().toLowerCase());
        return Integer.compare(index1, index2);
    }
    
    /**
     * Handle meal selection
     */
    private void handleMealSelection() {
        int selectedIndex = mealSelectionList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentMeals.size()) {
            selectedMeal = currentMeals.get(selectedIndex);
            System.out.println("✅ Selected meal: " + selectedMeal.getMealType() + " for swaps");
            
            // Update UI to show selected meal
            selectMealButton.setText("✓ " + selectedMeal.getMealType().toUpperCase() + " Selected");
            selectMealButton.setBackground(new Color(46, 125, 50));
        }
    }
    
    /**
     * Update meal selection button state
     */
    private void updateMealSelectionButtonState() {
        int selectedIndex = mealSelectionList.getSelectedIndex();
        boolean hasValidSelection = selectedIndex >= 0 && 
                                   selectedIndex < currentMeals.size() && 
                                   !currentMeals.isEmpty();
        
        if (hasValidSelection) {
            selectMealButton.setEnabled(true);
            selectMealButton.setText("Select This Meal");
            selectMealButton.setBackground(new Color(76, 175, 80));
        } else {
            selectMealButton.setEnabled(false);
            if (selectedMeal != null) {
                selectMealButton.setText("✓ " + selectedMeal.getMealType().toUpperCase() + " Selected");
                selectMealButton.setBackground(new Color(46, 125, 50));
            }
        }
    }
    
    /**
     * Generate swap suggestions based on selected goals
     */
    private void generateSuggestions() {
        System.out.println("🎯 generateSuggestions() called");
        
        try {
            // Validate goal selection
            if (goal1TargetCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a nutrient target for Goal 1", 
                    "Invalid Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Build goals list
            ArrayList<SwapGoalDTO> goals = new ArrayList<>();
            
            // Goal 1 (always present)
            SwapGoalDTO goal1 = createGoalFromSelection(1);
            goals.add(goal1);
            System.out.println("🎯 Goal 1: " + goal1.getGoalTarget() + " " + goal1.getAction() + " " + goal1.getTargetValue() + "%");
            
            // Goal 2 (if dual goals enabled)
            if (dualGoalsEnabled) {
                SwapGoalDTO goal2 = createGoalFromSelection(2);
                goals.add(goal2);
                System.out.println("🎯 Goal 2: " + goal2.getGoalTarget() + " " + goal2.getAction() + " " + goal2.getTargetValue() + "%");
            }
            
            // Validate meal selection
            if (selectedMeal == null) {
                JOptionPane.showMessageDialog(this, "Please select a meal first", 
                    "No Meal Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Generate suggestions using SwapEngine with the actual selected meal
            System.out.println("🎯 Calling SwapEngine with " + goals.size() + " goals for meal: " + selectedMeal.getMealType());
            List<SwapDTO> suggestions = swapFacade.selectStrategyAndSuggest(goals, selectedMeal);
            System.out.println("🎯 SwapEngine returned " + suggestions.size() + " suggestions");
            
            // Display suggestions (limit to 4)
            int displayCount = Math.min(4, suggestions.size());
            if (displayCount > 0) {
                displaySuggestions(suggestions.subList(0, displayCount), goals);
            } else {
                displaySuggestions(suggestions, goals); // Show "no suggestions" message
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating suggestions: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating suggestions: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Create a SwapGoalDTO from UI selection
     */
    private SwapGoalDTO createGoalFromSelection(int goalNumber) {
        JComboBox<String> targetCombo = (goalNumber == 1) ? goal1TargetCombo : goal2TargetCombo;
        JComboBox<String> actionCombo = (goalNumber == 1) ? goal1ActionCombo : goal2ActionCombo;
        JComboBox<String> intensityCombo = (goalNumber == 1) ? goal1IntensityCombo : goal2IntensityCombo;
        JComboBox<String> percentageCombo = (goalNumber == 1) ? goal1PercentageCombo : goal2PercentageCombo;
        
        String target = (String) targetCombo.getSelectedItem();
        String action = (String) actionCombo.getSelectedItem();
        String intensity = (String) intensityCombo.getSelectedItem();
        String percentage = (String) percentageCombo.getSelectedItem();
        
        // Convert to DTO format
        String goalTarget = target.toLowerCase();
        SwapGoalDTO.GoalAction goalAction = "Decrease".equals(action) ? 
            SwapGoalDTO.GoalAction.DECREASE : SwapGoalDTO.GoalAction.INCREASE;
        
        // Calculate target percentage based on intensity
        double targetPercentage;
        if ("Precise".equals(intensity)) {
            targetPercentage = Double.parseDouble(percentage.replace("%", ""));
        } else if ("High".equals(intensity)) {
            targetPercentage = 30.0;
        } else { // Normal
            targetPercentage = 20.0;
        }
        
        SwapGoalDTO goal = new SwapGoalDTO();
        goal.setGoalTarget(goalTarget);
        goal.setAction(goalAction);
        goal.setTargetValue(targetPercentage);
        goal.setIntensity(targetPercentage / 100.0); // Convert percentage to 0.0-1.0 range
        
        return goal;
    }
    

    
    /**
     * Display suggestions in the middle panel
     */
    private void displaySuggestions(List<SwapDTO> suggestions, ArrayList<SwapGoalDTO> goals) {
        suggestionsListPanel.removeAll();
        currentSuggestions = suggestions;
        
        if (suggestions.isEmpty()) {
            JLabel noSuggestionsLabel = new JLabel("No suitable swaps found for your goals", SwingConstants.CENTER);
            noSuggestionsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noSuggestionsLabel.setForeground(Color.GRAY);
            suggestionsListPanel.add(noSuggestionsLabel);
        } else {
            for (int i = 0; i < suggestions.size(); i++) {
                SwapDTO suggestion = suggestions.get(i);
                JPanel suggestionPanel = createSuggestionPanel(suggestion, goals, i + 1);
                suggestionsListPanel.add(suggestionPanel);
                
                // Add separator between suggestions
                if (i < suggestions.size() - 1) {
                    suggestionsListPanel.add(Box.createVerticalStrut(5));
                    suggestionsListPanel.add(new JSeparator());
                    suggestionsListPanel.add(Box.createVerticalStrut(5));
                }
            }
        }
        
        suggestionsListPanel.revalidate();
        suggestionsListPanel.repaint();
        
        System.out.println("✅ Displayed " + suggestions.size() + " swap suggestions");
    }
    
    /**
     * Create a single suggestion panel
     */
    private JPanel createSuggestionPanel(SwapDTO suggestion, ArrayList<SwapGoalDTO> goals, int index) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("#" + index + " - " + suggestion.getOriginalFood() + " → " + 
                                    suggestion.getReplacementFood(), SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Goal satisfaction metrics
        JPanel metricsPanel = new JPanel(new GridLayout(goals.size(), 1, 2, 2));
        for (int i = 0; i < goals.size(); i++) {
            SwapGoalDTO goal = goals.get(i);
            double satisfaction = calculateGoalSatisfaction(suggestion, goal);
            
            JLabel metricLabel = new JLabel(String.format("Goal %d (%s %s): %.1f%% satisfied", 
                i + 1, goal.getAction().toString().toLowerCase(), goal.getGoalTarget(), satisfaction));
            metricLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            
            // Color-code based on satisfaction level
            if (satisfaction >= 80) {
                metricLabel.setForeground(new Color(76, 175, 80)); // Green
            } else if (satisfaction >= 60) {
                metricLabel.setForeground(new Color(255, 193, 7)); // Yellow
            } else {
                metricLabel.setForeground(new Color(244, 67, 54)); // Red
            }
            
            metricsPanel.add(metricLabel);
        }
        
        // Nutritional impact
        JLabel impactLabel = new JLabel(String.format("Impact: %.1f calories, %.1f protein, %.1f carbs", 
            suggestion.getCalorieChange(), suggestion.getProteinChange(), suggestion.getCarbohydrateChange()));
        impactLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        impactLabel.setForeground(Color.GRAY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(metricsPanel, BorderLayout.CENTER);
        panel.add(impactLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Calculate goal satisfaction percentage for a suggestion
     */
    private double calculateGoalSatisfaction(SwapDTO suggestion, SwapGoalDTO goal) {
        double change = 0;
        
        // Get the actual change based on goal target
        switch (goal.getGoalTarget().toLowerCase()) {
            case "calories":
                change = suggestion.getCalorieChange();
                break;
            case "protein":
                change = suggestion.getProteinChange();
                break;
            case "carbohydrates":
                change = suggestion.getCarbohydrateChange();
                break;
            case "fat":
                change = suggestion.getFatChange();
                break;
            case "fiber":
                change = suggestion.getFiberChange();
                break;
        }
        
        // Calculate satisfaction based on goal action and target percentage
        double targetChange = goal.getTargetValue();
        double actualChange = Math.abs(change);
        
        if (goal.getAction() == SwapGoalDTO.GoalAction.DECREASE && change < 0) {
            return Math.min(100, (actualChange / targetChange) * 100);
        } else if (goal.getAction() == SwapGoalDTO.GoalAction.INCREASE && change > 0) {
            return Math.min(100, (actualChange / targetChange) * 100);
        }
        
        return 0; // Wrong direction
    }
    
    /**
     * Show placeholder message when no suggestions are available
     */
    private void showPlaceholderMessage() {
        suggestionsListPanel.removeAll();
        
        JLabel placeholderLabel = new JLabel(
            "<html><div style='text-align: center;'>" +
            "Select your nutrition goals and click<br/>" +
            "'Get Swap Suggestions' to see up to 4<br/>" +
            "personalized recommendations<br/><br/>" +
            "Each suggestion will show:<br/>" +
            "• Food replacement options<br/>" +
            "• Goal satisfaction metrics<br/>" +
            "• Nutritional impact details" +
            "</div></html>", 
            SwingConstants.CENTER
        );
        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        placeholderLabel.setForeground(Color.GRAY);
        
        suggestionsListPanel.add(placeholderLabel);
        suggestionsListPanel.revalidate();
        suggestionsListPanel.repaint();
    }
    
    // ProfileChangeListener implementation (DD-8: Observer Pattern)
    
    @Override
    public void onProfileActivated(ProfileDTO activeProfile) {
        this.activeProfile = activeProfile;
        System.out.println("✅ SwapPanel updated for new active profile: " + activeProfile.getName());
    }
    
    @Override
    public void onProfileDeactivated(ProfileDTO deactivatedProfile) {
        if (activeProfile != null && activeProfile.getId().equals(deactivatedProfile.getId())) {
            this.activeProfile = null;
        }
    }
    
    @Override
    public void onProfileCreated(ProfileDTO newProfile) {
        // No specific action needed
    }
    
    @Override
    public void onProfileUpdated(ProfileDTO updatedProfile) {
        if (activeProfile != null && activeProfile.getId().equals(updatedProfile.getId())) {
            this.activeProfile = updatedProfile;
        }
    }
    
    @Override
    public void onProfileDeleted(String deletedProfileId) {
        if (activeProfile != null && activeProfile.getId().toString().equals(deletedProfileId)) {
            this.activeProfile = null;
            System.out.println("Active profile deleted - cleared swap panel data");
        }
    }
    
    // Public API methods
    
    public ProfileDTO getActiveProfile() {
        return activeProfile;
    }
    
    public void setActiveProfile(ProfileDTO activeProfile) {
        this.activeProfile = activeProfile;
    }
    
    public void refreshPanel() {
        // Refresh logic for external triggers
        showPlaceholderMessage();
        repaint();
        System.out.println("✅ SwapPanel refreshed");
    }
    
    public List<SwapDTO> getCurrentSuggestions() {
        return currentSuggestions;
    }
    
    public boolean isDualGoalsEnabled() {
        return dualGoalsEnabled;
    }
    
    /**
     * Set the meal log facade for loading meal history
     */
    public void setMealLogFacade(IMealLogFacade mealLogFacade) {
        this.mealLogFacade = mealLogFacade;
        // Refresh meal selection when facade is available
        if (mealLogFacade != null) {
            refreshMealSelection();
        }
    }
    
    /**
     * Custom cell renderer for meal selection list
     */
    private static class MealSelectionCellRenderer extends DefaultListCellRenderer {
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
} 