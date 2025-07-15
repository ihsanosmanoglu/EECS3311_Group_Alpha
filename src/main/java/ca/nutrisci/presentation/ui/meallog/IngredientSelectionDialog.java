package ca.nutrisci.presentation.ui.meallog;

import ca.nutrisci.application.dto.IngredientDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.services.UnitConversionService;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IngredientSelectionDialog - Advanced ingredient selection with individual quantities and units
 * 
 * PURPOSE:
 * - Provides comprehensive ingredient selection interface with 5,700+ CNF foods
 * - Supports individual quantity and unit for each ingredient
 * - Double-click editing for easy quantity/unit modification
 * - Integrates with UnitConversionService for proper unit handling
 * 
 * FEATURES:
 * - Search across Canadian Nutrient File foods
 * - Food group filtering for easy browsing
 * - Individual ingredient quantity and unit management
 * - Double-click to edit ingredient quantity/unit
 * - Unit conversion support from CNF database
 * 
 * @author NutriSci Development Team
 * @version 3.0 (Refactored for individual ingredient units)
 * @since 1.0
 */
public class IngredientSelectionDialog extends JDialog {
    
    // Inner class to hold ingredient information with quantity and unit
    private static class EditableIngredientInfo {
        String name;
        double quantity;
        String unit;

        EditableIngredientInfo(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f %s)", name, quantity, unit);
        }
    }
    
    // Dialog components
    private JTextField searchField;
    private JComboBox<String> foodGroupCombo;
    private JList<String> availableList;
    private DefaultListModel<String> availableModel;
    private JList<EditableIngredientInfo> selectedList;
    private DefaultListModel<EditableIngredientInfo> selectedModel;
    private JButton createMealButton;
    private JButton cancelButton;
    
    // Core dependencies
    private final INutritionGateway nutritionGateway;
    private final UnitConversionService unitConversionService;
    
    // Dialog state
    private final String mealType;
    private final MealDTO existingMeal;
    private MealLogMediator.IngredientSelectionResult result;
    private boolean confirmed = false;
    
    /**
     * Constructor - Creates ingredient selection dialog
     */
    public IngredientSelectionDialog(Frame parent, String mealType, MealDTO existingMeal, 
                                   INutritionGateway nutritionGateway) {
        super(parent, createDialogTitle(mealType, existingMeal), true);
        
        this.mealType = mealType;
        this.existingMeal = existingMeal;
        this.nutritionGateway = nutritionGateway;
        this.unitConversionService = UnitConversionService.getInstance();
        
        // Initialize unit conversion service
        try {
            unitConversionService.initialize();
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize UnitConversionService: " + e.getMessage());
        }
        
        initializeComponents();
        layoutComponents();
        populateInitialData();
        attachEventListeners();
        
        // Configure dialog
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    /**
     * Create dialog title based on meal type and edit mode
     */
    private static String createDialogTitle(String mealType, MealDTO existingMeal) {
        String action = existingMeal != null ? "Edit" : "Add";
        String ingredientCount = existingMeal != null ? 
            " (Currently has " + existingMeal.getIngredients().size() + " ingredients)" : "";
        
        return action + " " + mealType.toUpperCase() + " Meal - Choose from 5,700+ Foods" + ingredientCount;
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Search components
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 25));
        
        // Food group filter - load from nutrition gateway if available
        foodGroupCombo = new JComboBox<>();
        foodGroupCombo.addItem("All Foods");
        
        // Try to load real food groups from nutrition gateway
        try {
            if (nutritionGateway != null) {
                List<String> foodGroups = nutritionGateway.getAllFoodGroups();
                for (String group : foodGroups) {
                    foodGroupCombo.addItem(group);
                }
            }
        } catch (Exception e) {
            // Fall back to predefined groups
            addDefaultFoodGroups();
        }
        
        if (foodGroupCombo.getItemCount() == 1) {
            addDefaultFoodGroups();
        }
        
        // Available ingredients list
        availableModel = new DefaultListModel<>();
        availableList = new JList<>(availableModel);
        availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Selected ingredients list with custom model
        selectedModel = new DefaultListModel<>();
        selectedList = new JList<>(selectedModel);
        selectedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Use custom cell renderer for better display
        selectedList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (value instanceof EditableIngredientInfo) {
                    EditableIngredientInfo info = (EditableIngredientInfo) value;
                    setText(String.format("<html><b>%s</b><br/><small>%.1f %s</small></html>", 
                           info.name, info.quantity, info.unit));
                }
                
                return this;
            }
        });
        
        // Action buttons
        String buttonText = existingMeal != null ? "✓ Update " + mealType.toUpperCase() + " Meal" : 
                                                   "✓ Create " + mealType.toUpperCase() + " Meal";
        createMealButton = new JButton(buttonText);
        createMealButton.setPreferredSize(new Dimension(200, 45));
        createMealButton.setFont(new Font("Arial", Font.BOLD, 14));
        createMealButton.setBackground(new Color(33, 150, 243));
        createMealButton.setForeground(Color.WHITE);
        createMealButton.setFocusPainted(false);
        createMealButton.setBorder(BorderFactory.createEtchedBorder());
        
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 45));
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEtchedBorder());
    }
    
    /**
     * Add default food groups as fallback
     */
    private void addDefaultFoodGroups() {
        String[] defaultGroups = {
            "Dairy and Egg Products", "Poultry Products", "Vegetables and Vegetable Products",
            "Fruits and fruit juices", "Beef Products", "Pork Products", "Finfish and Shellfish Products",
            "Cereals, Grains and Pasta", "Baked Products", "Nuts and Seeds", "Legumes and Legume Products",
            "Spices and Herbs", "Fats and Oils", "Beverages", "Sweets"
        };
        
        for (String group : defaultGroups) {
            foodGroupCombo.addItem(group);
        }
    }
    
    /**
     * Layout all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Control panel at top
        JPanel controlPanel = createControlPanel();
        dialogPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Selection panel in center
        JPanel selectionPanel = createSelectionPanel();
        dialogPanel.add(selectionPanel, BorderLayout.CENTER);
        
        // Button panel at bottom
        JPanel buttonPanel = createButtonPanel();
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(dialogPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create control panel with search and filter components
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Foods"));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Add instruction label
        JLabel instructionLabel = new JLabel("<html><small>💡 Double-click items to add/edit quantities and units</small></html>");
        instructionLabel.setForeground(new Color(100, 100, 100));
        searchPanel.add(instructionLabel, BorderLayout.SOUTH);
        
        // Food group filter
        JPanel groupPanel = new JPanel(new BorderLayout(5, 5));
        groupPanel.setBorder(BorderFactory.createTitledBorder("Filter by Food Group"));
        groupPanel.add(foodGroupCombo, BorderLayout.CENTER);
        
        controlPanel.add(searchPanel, BorderLayout.CENTER);
        controlPanel.add(groupPanel, BorderLayout.EAST);
        
        return controlPanel;
    }
    
    /**
     * Create selection panel with ingredient lists and transfer buttons
     */
    private JPanel createSelectionPanel() {
        JPanel selectionPanel = new JPanel(new BorderLayout(10, 10));
        
        // Left panel - Available ingredients
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        TitledBorder leftBorder = BorderFactory.createTitledBorder("Available Foods (Double-click to add)");
        leftPanel.setBorder(leftBorder);
        JScrollPane availableScroll = new JScrollPane(availableList);
        availableScroll.setPreferredSize(new Dimension(320, 300));
        leftPanel.add(availableScroll, BorderLayout.CENTER);
        
        // Middle panel - Transfer buttons
        JPanel middlePanel = createTransferButtonPanel();
        
        // Right panel - Selected ingredients
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        TitledBorder rightBorder = BorderFactory.createTitledBorder("Selected Ingredients (Double-click to edit)");
        rightPanel.setBorder(rightBorder);
        JScrollPane selectedScroll = new JScrollPane(selectedList);
        selectedScroll.setPreferredSize(new Dimension(320, 300));
        rightPanel.add(selectedScroll, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = createInfoPanel();
        rightPanel.add(infoPanel, BorderLayout.SOUTH);
        
        selectionPanel.add(leftPanel, BorderLayout.WEST);
        selectionPanel.add(middlePanel, BorderLayout.CENTER);
        selectionPanel.add(rightPanel, BorderLayout.EAST);
        
        return selectionPanel;
    }
    
    /**
     * Create transfer button panel
     */
    private JPanel createTransferButtonPanel() {
        JPanel middlePanel = new JPanel(new GridBagLayout());
        middlePanel.setPreferredSize(new Dimension(140, 300));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JButton addButton = createTransferButton("Add →", new Color(76, 175, 80));
        gbc.gridx = 0; gbc.gridy = 0;
        middlePanel.add(addButton, gbc);
        
        JButton removeButton = createTransferButton("← Remove", new Color(244, 67, 54));
        gbc.gridx = 0; gbc.gridy = 1;
        middlePanel.add(removeButton, gbc);
        
        JButton editButton = createTransferButton("✏️ Edit", new Color(33, 150, 243));
        gbc.gridx = 0; gbc.gridy = 2;
        middlePanel.add(editButton, gbc);
        
        JButton clearButton = createTransferButton("Clear All", new Color(255, 152, 0));
        gbc.gridx = 0; gbc.gridy = 3;
        middlePanel.add(clearButton, gbc);
        
        // Add button functionality
        addButton.addActionListener(e -> addSelectedIngredientsWithDialog());
        removeButton.addActionListener(e -> removeSelectedIngredients());
        editButton.addActionListener(e -> editSelectedIngredient());
        clearButton.addActionListener(e -> selectedModel.clear());
        
        return middlePanel;
    }
    
    /**
     * Create transfer button with consistent styling
     */
    private JButton createTransferButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 40));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }
    
    /**
     * Create info panel with instructions
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel info1 = new JLabel("<html><small>💡 Each ingredient has its own quantity & unit</small></html>");
        JLabel info2 = new JLabel("<html><small>🔧 Double-click to edit quantity/unit</small></html>");
        JLabel info3 = new JLabel("<html><small>📏 Units loaded from CNF database</small></html>");
        
        info1.setForeground(new Color(100, 100, 100));
        info2.setForeground(new Color(100, 100, 100));
        info3.setForeground(new Color(100, 100, 100));
        
        infoPanel.add(info1);
        infoPanel.add(info2);
        infoPanel.add(info3);
        
        return infoPanel;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(createMealButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }
    
    /**
     * Populate initial data
     */
    private void populateInitialData() {
        // Load all ingredients from nutrition gateway if available
        try {
            if (nutritionGateway != null) {
                List<String> allIngredients = nutritionGateway.getAllIngredients();
                for (String ingredient : allIngredients) {
                    availableModel.addElement(ingredient);
                }
                System.out.println("✅ Loaded " + allIngredients.size() + " ingredients from CNF database");
            }
        } catch (Exception e) {
            System.err.println("Could not load ingredients from nutrition gateway: " + e.getMessage());
            // Fall back to common ingredients
            loadCommonIngredients();
        }
        
        if (availableModel.isEmpty()) {
            loadCommonIngredients();
        }
        
        // Pre-populate with existing meal ingredients if editing
        if (existingMeal != null && existingMeal.getIngredients() != null) {
            for (IngredientDTO ingredient : existingMeal.getIngredients()) {
                EditableIngredientInfo info = new EditableIngredientInfo(
                    ingredient.getName(), 
                    ingredient.getQuantity(), 
                    ingredient.getUnit()
                );
                selectedModel.addElement(info);
            }
        }
    }
    
    /**
     * Load common ingredients as fallback
     */
    private void loadCommonIngredients() {
        String[] commonIngredients = getCommonIngredients(mealType);
        for (String ingredient : commonIngredients) {
            availableModel.addElement(ingredient);
        }
    }
    
    /**
     * Attach event listeners
     */
    private void attachEventListeners() {
        // Search functionality
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateIngredientList();
            }
        });
        
        foodGroupCombo.addActionListener(e -> updateIngredientList());
        
        // Double-click to add ingredients with quantity/unit dialog
        availableList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedIngredientsWithDialog();
                }
            }
        });
        
        // Double-click to edit ingredient quantity/unit
        selectedList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedIngredient();
                }
            }
        });
        
        // Button actions
        createMealButton.addActionListener(e -> handleCreateMeal());
        cancelButton.addActionListener(e -> handleCancel());
    }
    
    /**
     * Update ingredient list based on search and filter
     */
    private void updateIngredientList() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedGroup = (String) foodGroupCombo.getSelectedItem();
        availableModel.clear();
        
        try {
            if (nutritionGateway != null) {
                List<String> ingredients;
                
                if (!searchText.isEmpty()) {
                    // Search functionality
                    ingredients = nutritionGateway.searchIngredients(searchText);
                } else if (!"All Foods".equals(selectedGroup)) {
                    // Group filtering
                    ingredients = nutritionGateway.getIngredientsByGroup(selectedGroup);
                } else {
                    // Show all ingredients (limited for performance)
                    ingredients = nutritionGateway.getAllIngredients();
                    if (ingredients.size() > 100) {
                        ingredients = ingredients.subList(0, 100);
                    }
                }
                
                for (String ingredient : ingredients) {
                    availableModel.addElement(ingredient);
                }
                
                return;
            }
        } catch (Exception e) {
            System.err.println("Error updating ingredient list: " + e.getMessage());
        }
        
        // Fallback to local search
        updateIngredientListFallback(searchText, selectedGroup);
    }
    
    /**
     * Fallback method for updating ingredient list
     */
    private void updateIngredientListFallback(String searchText, String selectedGroup) {
        String[] ingredients = getCommonIngredients(mealType);
        
        for (String ingredient : ingredients) {
            boolean matchesSearch = searchText.isEmpty() || ingredient.toLowerCase().contains(searchText);
            boolean matchesGroup = "All Foods".equals(selectedGroup); // For simplicity, all match "All Foods"
            
            if (matchesSearch && matchesGroup) {
                availableModel.addElement(ingredient);
            }
        }
    }
    
    /**
     * Add selected ingredients with quantity and unit dialog
     */
    private void addSelectedIngredientsWithDialog() {
        List<String> selected = availableList.getSelectedValuesList();
        if (selected.isEmpty()) {
            return;
        }
        
        for (String ingredientName : selected) {
            // Check if already selected
            boolean alreadySelected = false;
            for (int i = 0; i < selectedModel.size(); i++) {
                if (selectedModel.getElementAt(i).name.equals(ingredientName)) {
                    alreadySelected = true;
                    break;
                }
            }
            
            if (!alreadySelected) {
                // Show quantity and unit selection dialog
                EditableIngredientInfo info = showIngredientEditDialog(ingredientName, 100.0, "g", false);
                if (info != null) {
                    selectedModel.addElement(info);
                }
            }
        }
        
        availableList.clearSelection();
    }
    
    /**
     * Add selected ingredients with default quantity
     */
    private void addSelectedIngredients() {
        List<String> selected = availableList.getSelectedValuesList();
        for (String ingredientName : selected) {
            // Check if already selected
            boolean alreadySelected = false;
            for (int i = 0; i < selectedModel.size(); i++) {
                if (selectedModel.getElementAt(i).name.equals(ingredientName)) {
                    alreadySelected = true;
                    break;
                }
            }
            
            if (!alreadySelected) {
                EditableIngredientInfo info = new EditableIngredientInfo(ingredientName, 100.0, "g");
                selectedModel.addElement(info);
            }
        }
        availableList.clearSelection();
    }
    
    /**
     * Remove selected ingredients
     */
    private void removeSelectedIngredients() {
        List<EditableIngredientInfo> selected = selectedList.getSelectedValuesList();
        for (EditableIngredientInfo info : selected) {
            selectedModel.removeElement(info);
        }
        selectedList.clearSelection();
    }
    
    /**
     * Edit selected ingredient quantity and unit
     */
    private void editSelectedIngredient() {
        EditableIngredientInfo selected = selectedList.getSelectedValue();
        if (selected != null) {
            EditableIngredientInfo updated = showIngredientEditDialog(
                selected.name, selected.quantity, selected.unit, true);
            if (updated != null) {
                int index = selectedModel.indexOf(selected);
                selectedModel.setElementAt(updated, index);
            }
        }
    }
    
    /**
     * Show ingredient edit dialog for quantity and unit
     */
    private EditableIngredientInfo showIngredientEditDialog(String ingredientName, 
            double currentQuantity, String currentUnit, boolean isEdit) {
        
        JDialog dialog = new JDialog(this, (isEdit ? "Edit" : "Add") + " Ingredient: " + ingredientName, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Ingredient name (readonly)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Ingredient:"), gbc);
        gbc.gridx = 1;
        JLabel nameLabel = new JLabel(ingredientName);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        panel.add(nameLabel, gbc);
        
        // Quantity input
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        JTextField quantityField = new JTextField(String.valueOf(currentQuantity), 10);
        panel.add(quantityField, gbc);
        
        // Unit selection
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 1;
        
        JComboBox<String> unitCombo = new JComboBox<>();
        
        // Try to get units from UnitConversionService
        try {
            int foodId = nutritionGateway.getFoodId(ingredientName);
            if (foodId > 0) {
                List<String> availableUnits = unitConversionService.getAvailableUnitsForFood(foodId);
                for (String unit : availableUnits) {
                    unitCombo.addItem(unit);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not get units for " + ingredientName + ": " + e.getMessage());
        }
        
        // Add default units if none found
        if (unitCombo.getItemCount() == 0) {
            String[] defaultUnits = {"g", "ml", "1 cup", "1 tbsp", "1 tsp", "1 slice", "1 piece", "1 serving"};
            for (String unit : defaultUnits) {
                unitCombo.addItem(unit);
            }
        }
        
        // Set current unit
        unitCombo.setSelectedItem(currentUnit);
        panel.add(unitCombo, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);
        
        // Result holder
        final EditableIngredientInfo[] result = {null};
        
        okButton.addActionListener(e -> {
            try {
                double quantity = Double.parseDouble(quantityField.getText().trim());
                String unit = (String) unitCombo.getSelectedItem();
                
                if (quantity > 0) {
                    result[0] = new EditableIngredientInfo(ingredientName, quantity, unit);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be positive", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number for quantity", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(panel);
        dialog.setVisible(true);
        
        return result[0];
    }
    
    /**
     * Handle create/update meal action
     */
    private void handleCreateMeal() {
        // Allow empty meals when editing (user can remove all ingredients)
        if (selectedModel.isEmpty() && existingMeal == null) {
            JOptionPane.showMessageDialog(this, "Please select at least one food item", 
                                        "No Foods Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Convert selected ingredients to IngredientDTO list
        List<IngredientDTO> ingredients = new ArrayList<>();
        for (int i = 0; i < selectedModel.size(); i++) {
            EditableIngredientInfo info = selectedModel.getElementAt(i);
            ingredients.add(new IngredientDTO(info.name, info.quantity, info.unit));
        }
        
        // Create result
        result = new MealLogMediator.IngredientSelectionResult(ingredients, true);
        confirmed = true;
        dispose();
    }
    
    /**
     * Handle cancel action
     */
    private void handleCancel() {
        result = new MealLogMediator.IngredientSelectionResult(new ArrayList<>(), false);
        confirmed = false;
        dispose();
    }
    
    /**
     * Get the dialog result
     */
    public MealLogMediator.IngredientSelectionResult getResult() {
        return result;
    }
    
    // Helper methods for ingredient data
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
} 