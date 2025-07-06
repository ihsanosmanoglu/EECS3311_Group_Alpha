package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.application.services.observers.ProfileChangeListener;
import ca.nutrisci.presentation.controllers.MealLogController;
import ca.nutrisci.infrastructure.external.adapters.ExternalAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * MealLogPanel - Redesigned UI for meal logging with better UX
 * Matches the wireframe with Log new meal | Today's breakdown | Meal Journal layout
 * Implements ProfileChangeListener to refresh data when active profile changes (DD-8)
 */
public class MealLogPanel extends JPanel implements ProfileChangeListener {
    
    // UI Components - Redesigned layout
    private JTextField dateField;
    private JComboBox<String> mealTypeCombo;
    private JTextField selectedDateField;
    private JButton quickAddBreakfastButton;
    private JButton quickAddLunchButton;
    private JButton quickAddDinnerButton;
    private JButton quickAddSnackButton;
    
    // Today's breakdown components
    private JLabel proteinLabel;
    private JLabel fatLabel;
    private JLabel carbsLabel;
    private JLabel caloriesLabel;
    private JLabel fiberLabel;
    
    // Meal journal components
    private JList<String> mealJournalList;
    private DefaultListModel<String> journalListModel;
    private JButton viewMealDetailsButton;
    private JButton editMealButton;
    private JButton deleteMealButton;
    
    // Data
    private List<String> currentIngredients;
    private List<Double> currentQuantities;
    private MealDTO selectedMeal;
    private UUID currentProfileId;
    private List<MealDTO> todaysMeals;
    private List<MealDTO> allMeals;
    
    // Callbacks
    private ActionListener addMealCallback;
    private ActionListener editMealCallback;
    private ActionListener deleteMealCallback;
    private ActionListener refreshMealsCallback;
    private ActionListener getDaySummaryCallback;
    
    // Instance variables
    private IMealLogFacade mealLogFacade;
    private List<MealDTO> meals;
    private ProfileDTO activeProfile;
    private String selectedMealId;
    private ExternalAdapter nutritionAdapter;
    
    public MealLogPanel(IMealLogFacade mealLogFacade, ProfileDTO activeProfile) {
        this.mealLogFacade = mealLogFacade;
        this.activeProfile = activeProfile;
        this.meals = new ArrayList<>();
        this.nutritionAdapter = ExternalAdapter.getInstance("Canada Nutrient File-20250622");
        
        this.currentIngredients = new ArrayList<>();
        this.currentQuantities = new ArrayList<>();
        this.todaysMeals = new ArrayList<>();
        this.allMeals = new ArrayList<>();
        this.selectedMealId = null;
        
        initializeComponents();
        layoutComponents();
        loadMeals();
    }
    
    private void initializeComponents() {
        // Initialize input components
        dateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        dateField.setPreferredSize(new Dimension(120, 25));
        mealTypeCombo = new JComboBox<>(new String[]{"breakfast", "lunch", "dinner", "snack"});
        
        // Date picker for meal journal filtering
        selectedDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        selectedDateField.setPreferredSize(new Dimension(120, 25));
        selectedDateField.addActionListener(e -> filterMealsByDate());
        
        // Quick add buttons for different meal types
        quickAddBreakfastButton = new JButton("Add Breakfast");
        quickAddLunchButton = new JButton("Add Lunch");
        quickAddDinnerButton = new JButton("Add Dinner");
        quickAddSnackButton = new JButton("Add Snack");
        
        // Style the quick add buttons
        styleQuickAddButton(quickAddBreakfastButton, new Color(255, 230, 153));
        styleQuickAddButton(quickAddLunchButton, new Color(153, 255, 153));
        styleQuickAddButton(quickAddDinnerButton, new Color(153, 204, 255));
        styleQuickAddButton(quickAddSnackButton, new Color(255, 153, 255));
        
        // Today's breakdown labels
        proteinLabel = createNutrientLabel("Protein: 0g");
        fatLabel = createNutrientLabel("Fat: 0g");
        carbsLabel = createNutrientLabel("Carbs: 0g");
        caloriesLabel = createNutrientLabel("Calories: 0");
        fiberLabel = createNutrientLabel("Fiber: 0g");
        
        // Meal journal components
        journalListModel = new DefaultListModel<>();
        mealJournalList = new JList<>(journalListModel);
        mealJournalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mealJournalList.setCellRenderer(new MealJournalCellRenderer());
        mealJournalList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateJournalButtons();
            }
        });
        
        viewMealDetailsButton = new JButton("View Details");
        editMealButton = new JButton("Edit");
        deleteMealButton = new JButton("Delete");
        
        // Add action listeners
        quickAddBreakfastButton.addActionListener(e -> handleQuickAdd("breakfast"));
        quickAddLunchButton.addActionListener(e -> handleQuickAdd("lunch"));
        quickAddDinnerButton.addActionListener(e -> handleQuickAdd("dinner"));
        quickAddSnackButton.addActionListener(e -> handleQuickAdd("snack"));
        
        viewMealDetailsButton.addActionListener(e -> handleViewMealDetails());
        editMealButton.addActionListener(e -> handleEditMeal());
        deleteMealButton.addActionListener(e -> handleDeleteMeal());
        
        // Initially disable journal buttons
        viewMealDetailsButton.setEnabled(false);
        editMealButton.setEnabled(false);
        deleteMealButton.setEnabled(false);
    }
    
    private void styleQuickAddButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 35));
    }
    
    private JLabel createNutrientLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main container with proper spacing
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create three main sections
        JPanel leftPanel = createLogNewMealPanel();
        JPanel middlePanel = createTodaysBreakdownPanel();
        JPanel rightPanel = createMealJournalPanel();
        
        // Set preferred sizes to match wireframe proportions
        leftPanel.setPreferredSize(new Dimension(300, 400));
        middlePanel.setPreferredSize(new Dimension(250, 400));
        rightPanel.setPreferredSize(new Dimension(300, 400));
        
        // Layout the three panels horizontally
        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        contentPanel.add(leftPanel);
        contentPanel.add(middlePanel);
        contentPanel.add(rightPanel);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add title
        JLabel titleLabel = new JLabel("Meal Logging - Food Journal", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createLogNewMealPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2), 
            "Log new meal",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        );
        panel.setBorder(border);
        
        // Date selection at top
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Date: "));
        datePanel.add(dateField);
        panel.add(datePanel, BorderLayout.NORTH);
        
        // Quick add buttons in the center
        JPanel quickAddPanel = new JPanel(new GridLayout(4, 1, 5, 10));
        quickAddPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        quickAddPanel.add(quickAddBreakfastButton);
        quickAddPanel.add(quickAddLunchButton);
        quickAddPanel.add(quickAddDinnerButton);
        quickAddPanel.add(quickAddSnackButton);
        
        panel.add(quickAddPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTodaysBreakdownPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2), 
            "Today's breakdown",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        );
        panel.setBorder(border);
        
        // Nutrition breakdown
        JPanel nutritionPanel = new JPanel(new GridLayout(5, 1, 5, 10));
        nutritionPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        
        nutritionPanel.add(proteinLabel);
        nutritionPanel.add(fatLabel);
        nutritionPanel.add(carbsLabel);
        nutritionPanel.add(fiberLabel);
        nutritionPanel.add(caloriesLabel);
        
        panel.add(nutritionPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMealJournalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2), 
            "Meal Journal",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        );
        panel.setBorder(border);
        
        // Date selector panel at the top (DD-8 requirement for date-based filtering)
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        datePanel.add(new JLabel("View Date:"));
        datePanel.add(selectedDateField);
        JButton todayButton = new JButton("Today");
        todayButton.setPreferredSize(new Dimension(80, 25));
        todayButton.addActionListener(e -> {
            selectedDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            filterMealsByDate();
        });
        datePanel.add(todayButton);
        panel.add(datePanel, BorderLayout.NORTH);
        
        // Meal list
        JScrollPane scrollPane = new JScrollPane(mealJournalList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Action buttons (edit removed - use quick-add buttons to edit meals)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(viewMealDetailsButton);
        buttonPanel.add(deleteMealButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Custom cell renderer for meal journal
    private class MealJournalCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof String) {
                String mealText = (String) value;
                // Extract calories from the meal text for styling
                if (mealText.contains("cal")) {
                    setFont(new Font("Arial", Font.PLAIN, 12));
                }
            }
            
            return this;
        }
    }
    
    // Event handlers
    private void handleQuickAdd(String mealType) {
        // Check if meal already exists for this type today
        LocalDate today = LocalDate.now();
        MealDTO existingMeal = findExistingMeal(mealType, today);
        
        if (existingMeal != null) {
            // Show ingredient selection dialog with existing meal data for editing
            showIngredientSelectionDialog(mealType, existingMeal);
        } else {
            // Show ingredient selection dialog for new meal
            showIngredientSelectionDialog(mealType, null);
        }
    }
    
    /**
     * Find existing meal for the given type and date
     */
    private MealDTO findExistingMeal(String mealType, LocalDate date) {
        if (activeProfile == null || allMeals == null) return null;
        
        return allMeals.stream()
            .filter(meal -> meal.getProfileId().equals(activeProfile.getId()))
            .filter(meal -> meal.getDate().equals(date))
            .filter(meal -> meal.getMealType().equalsIgnoreCase(mealType))
            .findFirst()
            .orElse(null);
    }
    
    private void showIngredientSelectionDialog(String mealType, MealDTO existingMeal) {
        // Determine if we're adding or editing
        boolean isEditing = (existingMeal != null);
        String dialogTitle = (isEditing ? "Edit " : "Add ") + mealType.toUpperCase() + " Meal";
        
        // Create a dialog for ingredient selection
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), dialogTitle, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        String titleText = (isEditing ? "Edit " : "Add ") + mealType.toUpperCase() + " Meal - Choose from 5,700+ Foods";
        if (isEditing) {
            titleText += " (Currently has " + existingMeal.getIngredients().size() + " ingredients)";
        }
        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        dialogPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Top panel - Search and filter controls
        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Foods"));
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 25));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Food group filter
        JPanel groupPanel = new JPanel(new BorderLayout(5, 5));
        groupPanel.setBorder(BorderFactory.createTitledBorder("Filter by Food Group"));
        JComboBox<String> foodGroupCombo = new JComboBox<>();
        foodGroupCombo.addItem("All Foods");
        foodGroupCombo.addItem("Dairy and Egg Products");
        foodGroupCombo.addItem("Poultry Products");
        foodGroupCombo.addItem("Vegetables and Vegetable Products");
        foodGroupCombo.addItem("Fruits and fruit juices");
        foodGroupCombo.addItem("Beef Products");
        foodGroupCombo.addItem("Pork Products");
        foodGroupCombo.addItem("Finfish and Shellfish Products");
        foodGroupCombo.addItem("Cereals, Grains and Pasta");
        foodGroupCombo.addItem("Baked Products");
        foodGroupCombo.addItem("Nuts and Seeds");
        foodGroupCombo.addItem("Legumes and Legume Products");
        foodGroupCombo.addItem("Spices and Herbs");
        foodGroupCombo.addItem("Fats and Oils");
        foodGroupCombo.addItem("Beverages");
        foodGroupCombo.addItem("Sweets");
        groupPanel.add(foodGroupCombo, BorderLayout.CENTER);
        
        controlPanel.add(searchPanel, BorderLayout.CENTER);
        controlPanel.add(groupPanel, BorderLayout.EAST);
        
        // Create the ingredient selection panel
        JPanel selectionPanel = new JPanel(new BorderLayout(10, 10));
        
        // Left panel - Available ingredients
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Available Foods"));
        
        DefaultListModel<String> availableModel = new DefaultListModel<>();
        JList<String> availableList = new JList<>(availableModel);
        availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Start with common ingredients for the meal type
        String[] commonIngredients = getCommonIngredients(mealType);
        for (String ingredient : commonIngredients) {
            availableModel.addElement(ingredient);
        }
        
        // Add search functionality
        Runnable updateIngredientList = () -> {
            String searchText = searchField.getText().toLowerCase().trim();
            String selectedGroup = (String) foodGroupCombo.getSelectedItem();
            availableModel.clear();
            
            // Use the meal type's common ingredients if no search or specific group
            if (searchText.isEmpty() && "All Foods".equals(selectedGroup)) {
                for (String ingredient : commonIngredients) {
                    availableModel.addElement(ingredient);
                }
                return;
            }
            
            // Get all ingredients from the nutrition adapter if available
            try {
                // We'll simulate access to the adapter's search functionality
                if (!searchText.isEmpty()) {
                    // For search, we'll use a subset of the full database
                    addSearchResults(availableModel, searchText);
                } else if (!"All Foods".equals(selectedGroup)) {
                    // For food groups, show some representative foods
                    addFoodGroupItems(availableModel, selectedGroup);
                } else {
                    // Default to popular foods from all categories
                    addPopularFoods(availableModel);
                }
            } catch (Exception e) {
                // Fallback to common ingredients
                for (String ingredient : commonIngredients) {
                    if (searchText.isEmpty() || ingredient.toLowerCase().contains(searchText)) {
                        availableModel.addElement(ingredient);
                    }
                }
            }
        };
        
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateIngredientList.run();
            }
        });
        
        foodGroupCombo.addActionListener(e -> updateIngredientList.run());
        
        JScrollPane availableScroll = new JScrollPane(availableList);
        availableScroll.setPreferredSize(new Dimension(280, 250));
        leftPanel.add(availableScroll, BorderLayout.CENTER);
        
        // Middle panel - Transfer buttons
        JPanel middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setPreferredSize(new Dimension(120, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JButton addButton = new JButton("Add →");
        addButton.setPreferredSize(new Dimension(100, 35));
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.setBackground(new Color(76, 175, 80));
        addButton.setForeground(Color.WHITE);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 0;
        middlePanel.add(addButton, gbc);
        
        JButton removeButton = new JButton("← Remove");
        removeButton.setPreferredSize(new Dimension(100, 35));
        removeButton.setFont(new Font("Arial", Font.BOLD, 12));
        removeButton.setBackground(new Color(244, 67, 54));
        removeButton.setForeground(Color.WHITE);
        removeButton.setOpaque(true);
        removeButton.setBorderPainted(false);
        removeButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 1;
        middlePanel.add(removeButton, gbc);
        
        JButton clearButton = new JButton("Clear All");
        clearButton.setPreferredSize(new Dimension(100, 35));
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.setBackground(new Color(255, 152, 0));
        clearButton.setForeground(Color.WHITE);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 2;
        middlePanel.add(clearButton, gbc);
        
        // Right panel - Selected ingredients
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Selected Foods"));
        
        DefaultListModel<String> selectedModel = new DefaultListModel<>();
        JList<String> selectedList = new JList<>(selectedModel);
        selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Pre-populate with existing meal ingredients if editing
        if (isEditing && existingMeal.getIngredients() != null) {
            for (String ingredient : existingMeal.getIngredients()) {
                selectedModel.addElement(ingredient);
            }
        }
        
        JScrollPane selectedScroll = new JScrollPane(selectedList);
        selectedScroll.setPreferredSize(new Dimension(280, 250));
        rightPanel.add(selectedScroll, BorderLayout.CENTER);
        
        // Quantity input panel
        JPanel quantityPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        quantityPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        quantityPanel.add(new JLabel("Quantity (grams):"));
        
        // Set default quantity - use existing meal's quantity if editing
        String defaultQuantity = "100";
        if (isEditing && existingMeal.getQuantities() != null && !existingMeal.getQuantities().isEmpty()) {
            defaultQuantity = String.valueOf(existingMeal.getQuantities().get(0).intValue());
        }
        JTextField quantityField = new JTextField(defaultQuantity);
        quantityField.setPreferredSize(new Dimension(80, 25));
        quantityPanel.add(quantityField);
        quantityPanel.add(new JLabel("Same quantity for all"));
        quantityPanel.add(new JLabel("selected foods"));
        quantityPanel.add(new JLabel("Tip: Double-click to add"));
        quantityPanel.add(new JLabel("foods quickly"));
        rightPanel.add(quantityPanel, BorderLayout.SOUTH);
        
        // Layout the selection panel
        selectionPanel.add(leftPanel, BorderLayout.WEST);
        selectionPanel.add(middlePanel, BorderLayout.CENTER);
        selectionPanel.add(rightPanel, BorderLayout.EAST);
        
        // Add control panel and selection panel to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(selectionPanel, BorderLayout.CENTER);
        
        // Add/Remove button functionality
        addButton.addActionListener(e -> {
            List<String> selected = availableList.getSelectedValuesList();
            for (String ingredient : selected) {
                if (!selectedModel.contains(ingredient)) {
                    selectedModel.addElement(ingredient);
                }
            }
            availableList.clearSelection();
        });
        
        removeButton.addActionListener(e -> {
            List<String> selected = selectedList.getSelectedValuesList();
            for (String ingredient : selected) {
                selectedModel.removeElement(ingredient);
            }
            selectedList.clearSelection();
        });
        
        clearButton.addActionListener(e -> {
            selectedModel.clear();
        });
        
        // Double-click to add ingredients
        availableList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selected = availableList.getSelectedValue();
                    if (selected != null && !selectedModel.contains(selected)) {
                        selectedModel.addElement(selected);
                    }
                }
            }
        });
        
        // Double-click to remove ingredients
        selectedList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selected = selectedList.getSelectedValue();
                    if (selected != null) {
                        selectedModel.removeElement(selected);
                    }
                }
            }
        });
        
        // Bottom buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        String buttonText = isEditing ? "✓ Update " + mealType.toUpperCase() + " Meal" : "✓ Create " + mealType.toUpperCase() + " Meal";
        JButton createMealButton = new JButton(buttonText);
        createMealButton.setPreferredSize(new Dimension(200, 45));
        createMealButton.setFont(new Font("Arial", Font.BOLD, 14));
        createMealButton.setBackground(new Color(33, 150, 243));
        createMealButton.setForeground(Color.WHITE);
        createMealButton.setFocusPainted(false);
        createMealButton.setBorder(BorderFactory.createEtchedBorder());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 45));
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEtchedBorder());
        
        createMealButton.addActionListener(e -> {
            // Allow empty meals when editing (user can remove all ingredients)
            // But require at least one ingredient when creating new meals
            if (selectedModel.isEmpty() && existingMeal == null) {
                JOptionPane.showMessageDialog(dialog, "Please select at least one food item", 
                                            "No Foods Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                double quantity = Double.parseDouble(quantityField.getText());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be positive", 
                                                "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Convert selected ingredients to list
                List<String> selectedIngredients = new ArrayList<>();
                for (int i = 0; i < selectedModel.size(); i++) {
                    selectedIngredients.add(selectedModel.getElementAt(i));
                }
                
                createMealFromSelection(mealType, selectedIngredients, quantity, existingMeal);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number for quantity", 
                                            "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createMealButton);
        buttonPanel.add(cancelButton);
        
        // Add components to dialog
        dialogPanel.add(mainPanel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(dialogPanel);
        dialog.setVisible(true);
    }
    
    // Helper methods for ingredient search and filtering
    private void addSearchResults(DefaultListModel<String> model, String searchText) {
        try {
            if (nutritionAdapter != null && nutritionAdapter.isAvailable()) {
                // Use the real CNF database search
                List<String> searchResults = nutritionAdapter.searchIngredients(searchText);
                
                // Add search results, limiting to 50 for better performance
                int count = 0;
                for (String ingredient : searchResults) {
                    if (count >= 50) break;
                    model.addElement(ingredient);
                    count++;
                }
                
                // If we got results, we're done
                if (count > 0) {
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching CNF database: " + e.getMessage());
        }
        
        // Fallback to simulated search if CNF database is not available
        Map<String, String[]> searchDatabase = new HashMap<>();
        searchDatabase.put("chicken", new String[]{"chicken breast", "chicken thigh", "chicken wing", "chicken drumstick", "chicken liver", "chicken heart", "chicken feet", "ground chicken"});
        searchDatabase.put("beef", new String[]{"beef sirloin", "ground beef", "beef brisket", "beef ribs", "beef liver", "beef tenderloin", "beef chuck", "beef flank"});
        searchDatabase.put("milk", new String[]{"whole milk", "skim milk", "2% milk", "chocolate milk", "buttermilk", "condensed milk", "evaporated milk", "goat milk"});
        searchDatabase.put("cheese", new String[]{"cheddar cheese", "mozzarella cheese", "swiss cheese", "parmesan cheese", "cottage cheese", "cream cheese", "blue cheese", "goat cheese"});
        searchDatabase.put("bread", new String[]{"white bread", "whole wheat bread", "sourdough bread", "rye bread", "pita bread", "bagel", "croissant", "biscuit"});
        searchDatabase.put("rice", new String[]{"white rice", "brown rice", "basmati rice", "jasmine rice", "wild rice", "rice noodles", "rice cakes", "rice flour"});
        searchDatabase.put("potato", new String[]{"russet potato", "red potato", "sweet potato", "mashed potato", "baked potato", "potato chips", "french fries", "hash browns"});
        searchDatabase.put("tomato", new String[]{"fresh tomato", "cherry tomato", "plum tomato", "tomato sauce", "tomato paste", "sun-dried tomato", "tomato juice", "canned tomato"});
        searchDatabase.put("apple", new String[]{"red apple", "green apple", "gala apple", "granny smith apple", "apple juice", "apple sauce", "dried apple", "apple pie"});
        searchDatabase.put("fish", new String[]{"salmon", "tuna", "cod", "mackerel", "sardines", "tilapia", "bass", "trout"});
        searchDatabase.put("vegetable", new String[]{"broccoli", "carrots", "spinach", "bell pepper", "onion", "garlic", "celery", "lettuce"});
        searchDatabase.put("fruit", new String[]{"banana", "orange", "grapes", "strawberries", "blueberries", "pineapple", "mango", "kiwi"});
        
        // Add exact matches and partial matches
        for (Map.Entry<String, String[]> entry : searchDatabase.entrySet()) {
            if (entry.getKey().contains(searchText) || searchText.contains(entry.getKey())) {
                for (String food : entry.getValue()) {
                    if (food.toLowerCase().contains(searchText) && !model.contains(food)) {
                        model.addElement(food);
                        if (model.size() >= 50) return; // Limit results
                    }
                }
            }
        }
        
        // Add some additional common foods that match the search
        String[] additionalFoods = {
            "eggs", "butter", "olive oil", "vegetable oil", "salt", "pepper", "sugar", "flour",
            "pasta", "noodles", "beans", "lentils", "quinoa", "oats", "yogurt", "cream",
            "bacon", "ham", "sausage", "turkey", "pork", "lamb", "shrimp", "crab"
        };
        
        for (String food : additionalFoods) {
            if (food.contains(searchText) && !model.contains(food)) {
                model.addElement(food);
                if (model.size() >= 50) return;
            }
        }
    }
    
    private void addFoodGroupItems(DefaultListModel<String> model, String groupName) {
        try {
            if (nutritionAdapter != null && nutritionAdapter.isAvailable()) {
                // Try to get ingredients by food group from CNF database
                List<String> groupIngredients = nutritionAdapter.getIngredientsByGroup(groupName);
                
                // Add group ingredients, limiting to 50 for better performance
                int count = 0;
                for (String ingredient : groupIngredients) {
                    if (count >= 50) break;
                    model.addElement(ingredient);
                    count++;
                }
                
                // If we got results, we're done
                if (count > 0) {
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting food group from CNF database: " + e.getMessage());
        }
        
        // Fallback to predefined groups
        Map<String, String[]> groupFoods = new HashMap<>();
        groupFoods.put("Dairy and Egg Products", new String[]{"whole milk", "skim milk", "cheddar cheese", "mozzarella cheese", "cottage cheese", "yogurt", "butter", "cream", "eggs", "egg whites"});
        groupFoods.put("Poultry Products", new String[]{"chicken breast", "chicken thigh", "turkey breast", "duck", "chicken liver", "ground chicken", "chicken wings", "turkey bacon"});
        groupFoods.put("Vegetables and Vegetable Products", new String[]{"broccoli", "carrots", "spinach", "bell pepper", "onion", "tomato", "lettuce", "cucumber", "celery", "garlic"});
        groupFoods.put("Fruits and fruit juices", new String[]{"apple", "banana", "orange", "grapes", "strawberries", "blueberries", "pineapple", "mango", "orange juice", "apple juice"});
        groupFoods.put("Beef Products", new String[]{"ground beef", "beef sirloin", "beef tenderloin", "beef ribs", "beef brisket", "beef liver", "beef chuck roast", "steak"});
        groupFoods.put("Pork Products", new String[]{"pork chops", "bacon", "ham", "pork tenderloin", "ground pork", "pork ribs", "pork shoulder", "sausage"});
        groupFoods.put("Finfish and Shellfish Products", new String[]{"salmon", "tuna", "cod", "shrimp", "crab", "lobster", "mackerel", "sardines", "tilapia", "scallops"});
        groupFoods.put("Cereals, Grains and Pasta", new String[]{"white rice", "brown rice", "quinoa", "oats", "pasta", "noodles", "barley", "wheat flour", "bread", "cereal"});
        groupFoods.put("Baked Products", new String[]{"white bread", "whole wheat bread", "bagel", "muffin", "croissant", "crackers", "cookies", "cake", "pie", "pastry"});
        groupFoods.put("Nuts and Seeds", new String[]{"almonds", "walnuts", "peanuts", "cashews", "sunflower seeds", "pumpkin seeds", "pecans", "pistachios", "peanut butter", "almond butter"});
        groupFoods.put("Legumes and Legume Products", new String[]{"black beans", "kidney beans", "chickpeas", "lentils", "pinto beans", "navy beans", "lima beans", "split peas", "tofu", "soy milk"});
        groupFoods.put("Spices and Herbs", new String[]{"salt", "pepper", "garlic powder", "onion powder", "paprika", "cumin", "oregano", "basil", "thyme", "cinnamon"});
        groupFoods.put("Fats and Oils", new String[]{"olive oil", "vegetable oil", "butter", "margarine", "coconut oil", "canola oil", "sesame oil", "avocado oil", "lard", "shortening"});
        groupFoods.put("Beverages", new String[]{"water", "coffee", "tea", "soda", "juice", "beer", "wine", "energy drink", "sports drink", "milk shake"});
        groupFoods.put("Sweets", new String[]{"sugar", "honey", "chocolate", "candy", "ice cream", "cookies", "cake", "pie", "jam", "syrup"});
        
        String[] foods = groupFoods.get(groupName);
        if (foods != null) {
            for (String food : foods) {
                model.addElement(food);
            }
        }
    }
    
    private void addPopularFoods(DefaultListModel<String> model) {
        try {
            if (nutritionAdapter != null && nutritionAdapter.isAvailable()) {
                // Get a mix of popular foods from different categories
                List<String> allIngredients = nutritionAdapter.getAllIngredients();
                
                // Add some popular foods from the database
                String[] popularSearchTerms = {"chicken", "beef", "milk", "cheese", "bread", "rice", "potato", "apple", "banana", "salmon"};
                
                for (String term : popularSearchTerms) {
                    List<String> searchResults = nutritionAdapter.searchIngredients(term);
                    for (int i = 0; i < Math.min(3, searchResults.size()); i++) {
                        String ingredient = searchResults.get(i);
                        if (!model.contains(ingredient)) {
                            model.addElement(ingredient);
                            if (model.size() >= 50) return;
                        }
                    }
                }
                
                // If we got results, we're done
                if (model.size() > 0) {
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting popular foods from CNF database: " + e.getMessage());
        }
        
        // Fallback to predefined popular foods
        String[] popularFoods = {
            // Proteins
            "chicken breast", "ground beef", "salmon", "eggs", "tuna", "turkey breast", "pork chops", "shrimp",
            // Dairy
            "whole milk", "cheddar cheese", "yogurt", "butter", "cream cheese", "mozzarella cheese",
            // Vegetables
            "broccoli", "carrots", "spinach", "tomato", "onion", "bell pepper", "lettuce", "cucumber",
            // Fruits
            "apple", "banana", "orange", "grapes", "strawberries", "blueberries",
            // Grains
            "white rice", "brown rice", "pasta", "bread", "oats", "quinoa",
            // Pantry staples
            "olive oil", "vegetable oil", "salt", "pepper", "garlic", "flour", "sugar"
        };
        
        for (String food : popularFoods) {
            model.addElement(food);
        }
    }
    
    private String[] getCommonIngredients(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return new String[]{"eggs", "bread", "butter", "milk", "banana", "orange", "oats", "yogurt", 
                                  "cereal", "apple", "strawberries", "cheese", "olive oil"};
            case "lunch":
                return new String[]{"chicken", "rice", "pasta", "tomato", "lettuce", "cheese", "bread", 
                                  "bell pepper", "onion", "cucumber", "carrots", "salmon", "tuna", "beans"};
            case "dinner":
                return new String[]{"beef", "fish", "chicken", "broccoli", "carrots", "onion", "rice", "pasta", 
                                  "quinoa", "spinach", "bell pepper", "olive oil", "salmon", "potatoes"};
            case "snack":
                return new String[]{"apple", "banana", "almonds", "walnuts", "yogurt", "cheese", "orange", 
                                  "berries", "peanuts", "crackers", "milk"};
            default:
                return new String[]{"apple", "banana", "chicken", "rice", "bread", "milk", "eggs", "cheese", 
                                  "tomato", "lettuce", "pasta", "beef", "fish", "yogurt", "oats", "olive oil"};
        }
    }
    
    private void createMealFromSelection(String mealType, List<String> selectedIngredients, double quantity, MealDTO existingMeal) {
        try {
            if (activeProfile == null) {
                JOptionPane.showMessageDialog(this, "No active profile selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean isEditing = (existingMeal != null);
            
            // Check if meal type already exists for today (business rule) - only for new meals
            LocalDate today = LocalDate.now();
            if (!isEditing && !mealType.equalsIgnoreCase("snack") && 
                mealLogFacade.mealTypeExistsForDate(activeProfile.getId(), today, mealType)) {
                JOptionPane.showMessageDialog(this, 
                    "You already have a " + mealType + " meal for today.\nYou can only have one of each meal type per day (except snacks).", 
                    "Meal Already Exists", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create quantities list (same quantity for all ingredients)
            List<Double> quantities = new ArrayList<>();
            for (int i = 0; i < selectedIngredients.size(); i++) {
                quantities.add(quantity);
            }
            
            // Calculate nutrition using the nutrition adapter
            NutrientInfo totalNutrients = new NutrientInfo();
            for (int i = 0; i < selectedIngredients.size(); i++) {
                String ingredient = selectedIngredients.get(i);
                double ingredientQuantity = quantities.get(i);
                
                try {
                    NutrientInfo ingredientNutrients = nutritionAdapter.lookupIngredient(ingredient);
                    if (ingredientNutrients != null) {
                        // Scale nutrition based on quantity (per 100g)
                        double scaleFactor = ingredientQuantity / 100.0;
                        NutrientInfo scaledNutrients = ingredientNutrients.multiply(scaleFactor);
                        totalNutrients = totalNutrients.add(scaledNutrients);
                    }
                } catch (Exception e) {
                    System.err.println("Error getting nutrition for ingredient: " + ingredient + ", " + e.getMessage());
                    // Continue with other ingredients
                }
            }
            
            MealDTO savedMeal;
            
            if (isEditing) {
                // Update existing meal
                MealDTO updatedMeal = new MealDTO(
                    existingMeal.getId(), // Keep existing ID
                    activeProfile.getId(),
                    existingMeal.getDate(), // Keep original date
                    mealType.toLowerCase(),
                    selectedIngredients,
                    quantities,
                    totalNutrients
                );
                
                savedMeal = mealLogFacade.editMeal(existingMeal.getId(), updatedMeal);
                
                // Check if meal was auto-deleted due to being empty
                if (savedMeal == null) {
                    // Update the UI immediately
                    loadMeals(); // This updates both journal and today's breakdown
                    
                    // Show message that meal was deleted
                    String deleteMessage = String.format(
                        "🗑️ %s meal deleted successfully!\n\n" +
                        "The meal was automatically removed because it had no ingredients.",
                        mealType.toUpperCase()
                    );
                    
                    JOptionPane.showMessageDialog(this, deleteMessage, "Meal Deleted", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            } else {
                // Create new meal
                MealDTO mealDTO = new MealDTO(
                    null, // ID will be generated by the service
                    activeProfile.getId(),
                    today,
                    mealType.toLowerCase(),
                    selectedIngredients,
                    quantities,
                    totalNutrients
                );
                
                savedMeal = mealLogFacade.addMeal(mealDTO);
            }
            
            // Update the UI immediately
            loadMeals(); // This updates both journal and today's breakdown
            
            // Show success message with nutrition info
            String actionText = isEditing ? "updated" : "added";
            String successMessage = String.format(
                "✅ %s meal %s successfully!\n\n" +
                "📊 Nutrition Information:\n" +
                "• Calories: %.0f\n" +
                "• Protein: %.1fg\n" +
                "• Carbs: %.1fg\n" +
                "• Fat: %.1fg\n" +
                "• Fiber: %.1fg\n\n" +
                "🍽️ Ingredients: %s",
                mealType.toUpperCase(),
                actionText,
                savedMeal.getNutrients().getCalories(),
                savedMeal.getNutrients().getProtein(),
                savedMeal.getNutrients().getCarbs(),
                savedMeal.getNutrients().getFat(),
                savedMeal.getNutrients().getFiber(),
                String.join(", ", selectedIngredients)
            );
            
            String titleText = isEditing ? "Meal Updated Successfully!" : "Meal Added Successfully!";
            JOptionPane.showMessageDialog(this, successMessage, titleText, JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            System.err.println("Error creating meal: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error creating meal: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleViewMealDetails() {
        String selectedMealText = mealJournalList.getSelectedValue();
        if (selectedMealText != null && selectedMeal != null) {
            showMealDetailsDialog(selectedMeal);
        }
    }
    
    private void showMealDetailsDialog(MealDTO meal) {
        StringBuilder details = new StringBuilder();
        details.append("Meal Details\n\n");
        details.append("Type: ").append(meal.getMealType().toUpperCase()).append("\n");
        details.append("Date: ").append(meal.getDate()).append("\n\n");
        details.append("Ingredients:\n");
        
        for (int i = 0; i < meal.getIngredients().size(); i++) {
            details.append("• ").append(meal.getIngredients().get(i));
            if (i < meal.getQuantities().size()) {
                details.append(" (").append(meal.getQuantities().get(i)).append("g)");
            }
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
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Meal Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleEditMeal() {
        if (editMealCallback != null) {
            editMealCallback.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "edit"));
        }
    }
    
    private void handleDeleteMeal() {
        if (deleteMealCallback != null) {
            deleteMealCallback.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "delete"));
        }
    }
    
    private void updateJournalButtons() {
        int selectedIndex = mealJournalList.getSelectedIndex();
        boolean hasSelection = selectedIndex >= 0;
        
        viewMealDetailsButton.setEnabled(hasSelection);
        deleteMealButton.setEnabled(hasSelection);
        
        if (hasSelection && selectedIndex < todaysMeals.size()) {
            // Get the meal from the currently displayed filtered list
            selectedMeal = todaysMeals.get(selectedIndex);
        }
    }
    
    // Public methods for controller interaction
    public void setMeals(List<MealDTO> meals) {
        this.allMeals = new ArrayList<>(meals);
        this.meals = new ArrayList<>(meals);
        
        // Initialize journal with today's meals by default
        LocalDate today = LocalDate.now();
        updateMealJournalForDate(today);
        updateTodaysBreakdown();
    }
    
    private void updateTodaysBreakdown() {
        try {
            if (activeProfile != null) {
                LocalDate today = LocalDate.now();
                NutrientInfo dailyTotals = mealLogFacade.getDailyTotals(activeProfile.getId(), today);
                
                if (dailyTotals != null) {
                    proteinLabel.setText("Protein: " + String.format("%.1f", dailyTotals.getProtein()) + "g");
                    fatLabel.setText("Fat: " + String.format("%.1f", dailyTotals.getFat()) + "g");
                    carbsLabel.setText("Carbs: " + String.format("%.1f", dailyTotals.getCarbs()) + "g");
                    fiberLabel.setText("Fiber: " + String.format("%.1f", dailyTotals.getFiber()) + "g");
                    caloriesLabel.setText("Calories: " + String.format("%.0f", dailyTotals.getCalories()));
                } else {
                    proteinLabel.setText("Protein: 0.0g");
                    fatLabel.setText("Fat: 0.0g");
                    carbsLabel.setText("Carbs: 0.0g");
                    fiberLabel.setText("Fiber: 0.0g");
                    caloriesLabel.setText("Calories: 0");
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating today's breakdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public MealDTO getCurrentMealData() {
        String dateStr = dateField.getText();
        LocalDate date = LocalDate.parse(dateStr);
        String mealType = (String) mealTypeCombo.getSelectedItem();
        
        return new MealDTO(
            null, // ID will be set by service
            currentProfileId,
            date,
            mealType,
            new ArrayList<>(currentIngredients),
            new ArrayList<>(currentQuantities),
            null // Nutrients will be calculated by service
        );
    }
    
    public UUID getSelectedMealId() {
        return selectedMeal != null ? selectedMeal.getId() : null;
    }
    
    public void setCurrentProfileId(UUID profileId) {
        this.currentProfileId = profileId;
    }
    
    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    public void clearForm() {
        currentIngredients.clear();
        currentQuantities.clear();
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        mealTypeCombo.setSelectedIndex(0);
    }
    
    public String getSelectedDate() {
        return dateField.getText();
    }
    
    // Callback setters
    public void setAddMealCallback(ActionListener callback) {
        this.addMealCallback = callback;
    }
    
    public void setEditMealCallback(ActionListener callback) {
        this.editMealCallback = callback;
    }
    
    public void setDeleteMealCallback(ActionListener callback) {
        this.deleteMealCallback = callback;
    }
    
    public void setRefreshMealsCallback(ActionListener callback) {
        this.refreshMealsCallback = callback;
    }
    
    public void setGetDaySummaryCallback(ActionListener callback) {
        this.getDaySummaryCallback = callback;
    }
    
    public void showNutritionSummary(String summary) {
        // Summary is now shown in the Today's breakdown panel
        // This method can be used for additional summary displays if needed
    }
    
    private void loadMeals() {
        try {
            if (activeProfile != null) {
                // Load all meals for the active profile
                meals = mealLogFacade.getMealsForProfile(activeProfile.getId());
                this.allMeals = new ArrayList<>(meals);
                
                // Initialize journal with today's meals by default
                LocalDate today = LocalDate.now();
                updateMealJournalForDate(today);
                updateTodaysBreakdown();
            }
        } catch (Exception e) {
            System.err.println("Error loading meals: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    
    /**
     * Filter meals by selected date (DD-8 requirement for date-based filtering)
     */
    private void filterMealsByDate() {
        try {
            String selectedDateStr = selectedDateField.getText().trim();
            LocalDate selectedDate = LocalDate.parse(selectedDateStr);
            
            // Update meal journal to show only meals from selected date
            updateMealJournalForDate(selectedDate);
            
            // Update today's breakdown for selected date (not just today)
            updateBreakdownForDate(selectedDate);
            
        } catch (Exception e) {
            System.err.println("Error parsing selected date: " + e.getMessage());
            // Reset to today if invalid date
            selectedDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
    
    /**
     * Update meal journal list for a specific date
     */
    private void updateMealJournalForDate(LocalDate date) {
        journalListModel.clear();
        
        if (meals == null) return;
        
        // Filter meals for the selected date
        List<MealDTO> mealsForDate = meals.stream()
            .filter(meal -> meal.getDate().equals(date))
            .sorted((m1, m2) -> {
                // Sort by meal type order: breakfast, lunch, dinner, snack
                String[] order = {"breakfast", "lunch", "dinner", "snack"};
                int index1 = java.util.Arrays.asList(order).indexOf(m1.getMealType().toLowerCase());
                int index2 = java.util.Arrays.asList(order).indexOf(m2.getMealType().toLowerCase());
                return Integer.compare(index1, index2);
            })
            .collect(java.util.stream.Collectors.toList());
        
        // Store filtered meals for meal selection
        this.todaysMeals = mealsForDate;
        
        for (MealDTO meal : mealsForDate) {
            double calories = meal.getNutrients() != null ? meal.getNutrients().getCalories() : 0.0;
            String displayText = String.format("%s (%.0f cal)", 
                meal.getMealType().toUpperCase(),
                calories);
            journalListModel.addElement(displayText);
        }
        
        // Update the status to show selected date
        if (mealsForDate.isEmpty()) {
            journalListModel.addElement("No meals logged for " + date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }
    }
    
    /**
     * Update nutrition breakdown for a specific date
     */
    private void updateBreakdownForDate(LocalDate date) {
        try {
            if (activeProfile != null) {
                NutrientInfo dailyTotals = mealLogFacade.getDailyTotals(activeProfile.getId(), date);
                
                if (dailyTotals != null) {
                    proteinLabel.setText("Protein: " + String.format("%.1f", dailyTotals.getProtein()) + "g");
                    fatLabel.setText("Fat: " + String.format("%.1f", dailyTotals.getFat()) + "g");
                    carbsLabel.setText("Carbs: " + String.format("%.1f", dailyTotals.getCarbs()) + "g");
                    fiberLabel.setText("Fiber: " + String.format("%.1f", dailyTotals.getFiber()) + "g");
                    caloriesLabel.setText("Calories: " + String.format("%.0f", dailyTotals.getCalories()));
                } else {
                    proteinLabel.setText("Protein: 0.0g");
                    fatLabel.setText("Fat: 0.0g");
                    carbsLabel.setText("Carbs: 0.0g");
                    fiberLabel.setText("Fiber: 0.0g");
                    caloriesLabel.setText("Calories: 0");
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating breakdown for date: " + e.getMessage());
        }
    }
    
    // ProfileChangeListener implementation (Observer pattern - DD-8)
    
    @Override
    public void onProfileActivated(ProfileDTO activeProfile) {
        // Update the active profile and refresh all data
        this.activeProfile = activeProfile;
        this.currentProfileId = activeProfile.getId();
        
        // Clear current data
        journalListModel.clear();
        clearForm();
        
        // Reset date selector to today
        selectedDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Reload meals for the new active profile
        loadMeals();
        
        // Update today's breakdown
        updateTodaysBreakdown();
        
        System.out.println("✅ MealLogPanel refreshed for new active profile: " + activeProfile.getName());
    }
    
    @Override
    public void onProfileDeactivated(ProfileDTO deactivatedProfile) {
        // Profile deactivated - clear data if it was our active profile
        if (activeProfile != null && activeProfile.getId().equals(deactivatedProfile.getId())) {
            journalListModel.clear();
            clearForm();
            updateTodaysBreakdown();
        }
    }
    
    @Override
    public void onProfileCreated(ProfileDTO newProfile) {
        // A new profile was created - no action needed in meal log panel
        System.out.println("New profile created: " + newProfile.getName());
    }
    
    @Override
    public void onProfileUpdated(ProfileDTO updatedProfile) {
        // Profile was updated - refresh if it's our active profile
        if (activeProfile != null && activeProfile.getId().equals(updatedProfile.getId())) {
            this.activeProfile = updatedProfile;
            System.out.println("Active profile updated: " + updatedProfile.getName());
        }
    }
    
    @Override
    public void onProfileDeleted(String deletedProfileId) {
        // Profile was deleted - clear data if it was our active profile
        if (activeProfile != null && activeProfile.getId().toString().equals(deletedProfileId)) {
            this.activeProfile = null;
            this.currentProfileId = null;
            journalListModel.clear();
            clearForm();
            updateTodaysBreakdown();
            System.out.println("Active profile was deleted - cleared meal log data");
        }
    }
} 