package ca.nutrisci.presentation.ui.meallog;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import javax.swing.*;
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
 * IngredientSelectionDialog - Component for ingredient selection in meal creation/editing
 * 
 * PURPOSE:
 * - Provides comprehensive ingredient selection interface with 5,700+ CNF foods
 * - Handles both new meal creation and existing meal editing
 * - Integrates with nutrition gateway for food lookup
 * - Supports search, filtering, and easy ingredient selection
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-1: Three-Tier Layered - Uses nutrition gateway for food data
 * - DD-9: Naming Conventions - IngredientSelectionDialog follows naming pattern
 * - Single Responsibility: Handles only ingredient selection UI
 * 
 * FEATURES:
 * - Search across 5,700+ Canadian Nutrient File foods
 * - Food group filtering for easy browsing
 * - Double-click to add/remove ingredients
 * - Quantity specification for all selected foods
 * - Pre-population for editing existing meals
 * 
 * @author NutriSci Development Team
 * @version 2.0
 * @since 1.0
 */
public class IngredientSelectionDialog extends JDialog {
    
    // Dialog components
    private JTextField searchField;
    private JComboBox<String> foodGroupCombo;
    private JList<String> availableList;
    private DefaultListModel<String> availableModel;
    private JList<String> selectedList;
    private DefaultListModel<String> selectedModel;
    private JTextField quantityField;
    private JButton createMealButton;
    private JButton cancelButton;
    
    // Core dependencies
    private final INutritionGateway nutritionGateway;
    
    // Dialog state
    private final String mealType;
    private final MealDTO existingMeal;
    private MealLogMediator.IngredientSelectionResult result;
    private boolean confirmed = false;
    
    /**
     * Constructor - Creates ingredient selection dialog
     * 
     * @param parent Parent frame
     * @param mealType Type of meal (breakfast, lunch, dinner, snack)
     * @param existingMeal Existing meal if editing, null if creating new
     * @param nutritionGateway Gateway for nutrition data access
     */
    public IngredientSelectionDialog(Frame parent, String mealType, MealDTO existingMeal, 
                                   INutritionGateway nutritionGateway) {
        super(parent, createDialogTitle(mealType, existingMeal), true);
        
        this.mealType = mealType;
        this.existingMeal = existingMeal;
        this.nutritionGateway = nutritionGateway;
        
        initializeComponents();
        layoutComponents();
        populateInitialData();
        attachEventListeners();
        
        // Configure dialog
        setSize(800, 600);
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
        
        // Food group filter
        foodGroupCombo = new JComboBox<>();
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
        
        // Available ingredients list
        availableModel = new DefaultListModel<>();
        availableList = new JList<>(availableModel);
        availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Selected ingredients list
        selectedModel = new DefaultListModel<>();
        selectedList = new JList<>(selectedModel);
        selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Quantity field
        String defaultQuantity = "100";
        if (existingMeal != null && existingMeal.getQuantities() != null && !existingMeal.getQuantities().isEmpty()) {
            defaultQuantity = String.valueOf(existingMeal.getQuantities().get(0).intValue());
        }
        quantityField = new JTextField(defaultQuantity);
        quantityField.setPreferredSize(new Dimension(80, 25));
        
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
        leftPanel.setBorder(BorderFactory.createTitledBorder("Available Foods"));
        JScrollPane availableScroll = new JScrollPane(availableList);
        availableScroll.setPreferredSize(new Dimension(280, 250));
        leftPanel.add(availableScroll, BorderLayout.CENTER);
        
        // Middle panel - Transfer buttons
        JPanel middlePanel = createTransferButtonPanel();
        
        // Right panel - Selected ingredients
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Selected Foods"));
        JScrollPane selectedScroll = new JScrollPane(selectedList);
        selectedScroll.setPreferredSize(new Dimension(280, 250));
        rightPanel.add(selectedScroll, BorderLayout.CENTER);
        
        // Quantity panel
        JPanel quantityPanel = createQuantityPanel();
        rightPanel.add(quantityPanel, BorderLayout.SOUTH);
        
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
        middlePanel.setPreferredSize(new Dimension(120, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JButton addButton = createTransferButton("Add →", new Color(76, 175, 80));
        gbc.gridx = 0; gbc.gridy = 0;
        middlePanel.add(addButton, gbc);
        
        JButton removeButton = createTransferButton("← Remove", new Color(244, 67, 54));
        gbc.gridx = 0; gbc.gridy = 1;
        middlePanel.add(removeButton, gbc);
        
        JButton clearButton = createTransferButton("Clear All", new Color(255, 152, 0));
        gbc.gridx = 0; gbc.gridy = 2;
        middlePanel.add(clearButton, gbc);
        
        // Add button functionality
        addButton.addActionListener(e -> addSelectedIngredients());
        removeButton.addActionListener(e -> removeSelectedIngredients());
        clearButton.addActionListener(e -> selectedModel.clear());
        
        return middlePanel;
    }
    
    /**
     * Create transfer button with consistent styling
     */
    private JButton createTransferButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 35));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }
    
    /**
     * Create quantity input panel
     */
    private JPanel createQuantityPanel() {
        JPanel quantityPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        quantityPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        quantityPanel.add(new JLabel("Quantity (grams):"));
        quantityPanel.add(quantityField);
        quantityPanel.add(new JLabel("Same quantity for all"));
        quantityPanel.add(new JLabel("selected foods"));
        quantityPanel.add(new JLabel("Tip: Double-click to add"));
        quantityPanel.add(new JLabel("foods quickly"));
        return quantityPanel;
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
        // Load common ingredients for the meal type
        String[] commonIngredients = getCommonIngredients(mealType);
        for (String ingredient : commonIngredients) {
            availableModel.addElement(ingredient);
        }
        
        // Pre-populate with existing meal ingredients if editing
        if (existingMeal != null && existingMeal.getIngredients() != null) {
            for (String ingredient : existingMeal.getIngredients()) {
                selectedModel.addElement(ingredient);
            }
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
        
        // Double-click to add ingredients
        availableList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedIngredients();
                }
            }
        });
        
        // Double-click to remove ingredients
        selectedList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    removeSelectedIngredients();
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
        
        // Use the meal type's common ingredients if no search or specific group
        if (searchText.isEmpty() && "All Foods".equals(selectedGroup)) {
            String[] commonIngredients = getCommonIngredients(mealType);
            for (String ingredient : commonIngredients) {
                availableModel.addElement(ingredient);
            }
            return;
        }
        
        // Get ingredients based on search/filter
        try {
            if (!searchText.isEmpty()) {
                addSearchResults(availableModel, searchText);
            } else if (!"All Foods".equals(selectedGroup)) {
                addFoodGroupItems(availableModel, selectedGroup);
            } else {
                addPopularFoods(availableModel);
            }
        } catch (Exception e) {
            // Fallback to common ingredients
            String[] commonIngredients = getCommonIngredients(mealType);
            for (String ingredient : commonIngredients) {
                if (searchText.isEmpty() || ingredient.toLowerCase().contains(searchText)) {
                    availableModel.addElement(ingredient);
                }
            }
        }
    }
    
    /**
     * Add selected ingredients from available list to selected list
     */
    private void addSelectedIngredients() {
        List<String> selected = availableList.getSelectedValuesList();
        for (String ingredient : selected) {
            if (!selectedModel.contains(ingredient)) {
                selectedModel.addElement(ingredient);
            }
        }
        availableList.clearSelection();
    }
    
    /**
     * Remove selected ingredients from selected list
     */
    private void removeSelectedIngredients() {
        List<String> selected = selectedList.getSelectedValuesList();
        for (String ingredient : selected) {
            selectedModel.removeElement(ingredient);
        }
        selectedList.clearSelection();
    }
    
    /**
     * Handle create/update meal action
     */
    private void handleCreateMeal() {
        // Allow empty meals when editing (user can remove all ingredients)
        // But require at least one ingredient when creating new meals
        if (selectedModel.isEmpty() && existingMeal == null) {
            JOptionPane.showMessageDialog(this, "Please select at least one food item", 
                                        "No Foods Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityField.getText());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive", 
                                            "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Convert selected ingredients to list
            List<String> selectedIngredients = new ArrayList<>();
            for (int i = 0; i < selectedModel.size(); i++) {
                selectedIngredients.add(selectedModel.getElementAt(i));
            }
            
            // Create quantities list (same quantity for all)
            List<Double> quantities = new ArrayList<>();
            for (int i = 0; i < selectedIngredients.size(); i++) {
                quantities.add(quantity);
            }
            
            // Create result
            result = new MealLogMediator.IngredientSelectionResult(selectedIngredients, quantities, true);
            confirmed = true;
            dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity", 
                                        "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handle cancel action
     */
    private void handleCancel() {
        result = new MealLogMediator.IngredientSelectionResult(new ArrayList<>(), new ArrayList<>(), false);
        confirmed = false;
        dispose();
    }
    
    /**
     * Get the dialog result
     */
    public MealLogMediator.IngredientSelectionResult getResult() {
        return result;
    }
    
    // Helper methods for ingredient data (extracted from original MealLogPanel)
    
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
    
    private void addSearchResults(DefaultListModel<String> model, String searchText) {
        try {
            if (nutritionGateway != null && nutritionGateway.isAvailable()) {
                // Use the real CNF database search
                List<String> searchResults = nutritionGateway.searchIngredients(searchText);
                
                // Add search results, limiting to 50 for better performance
                int count = 0;
                for (String ingredient : searchResults) {
                    if (count >= 50) break;
                    model.addElement(ingredient);
                    count++;
                }
                
                if (count > 0) return;
            }
        } catch (Exception e) {
            System.err.println("Error searching CNF database: " + e.getMessage());
        }
        
        // Fallback to simulated search
        addFallbackSearchResults(model, searchText);
    }
    
    private void addFallbackSearchResults(DefaultListModel<String> model, String searchText) {
        Map<String, String[]> searchDatabase = new HashMap<>();
        searchDatabase.put("chicken", new String[]{"chicken breast", "chicken thigh", "chicken wing", "chicken drumstick"});
        searchDatabase.put("beef", new String[]{"beef sirloin", "ground beef", "beef brisket", "beef ribs"});
        searchDatabase.put("fish", new String[]{"salmon", "tuna", "cod", "mackerel", "sardines"});
        
        for (Map.Entry<String, String[]> entry : searchDatabase.entrySet()) {
            if (entry.getKey().contains(searchText) || searchText.contains(entry.getKey())) {
                for (String food : entry.getValue()) {
                    if (food.toLowerCase().contains(searchText) && !model.contains(food)) {
                        model.addElement(food);
                        if (model.size() >= 50) return;
                    }
                }
            }
        }
    }
    
    private void addFoodGroupItems(DefaultListModel<String> model, String groupName) {
        // Use predefined food groups since the gateway doesn't support group filtering
        String[] foods = getFallbackGroupFoods(groupName);
        if (foods != null) {
            for (String food : foods) {
                model.addElement(food);
            }
        }
    }
    
    private String[] getFallbackGroupFoods(String groupName) {
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
        return groupFoods.get(groupName);
    }
    
    private void addPopularFoods(DefaultListModel<String> model) {
        String[] popularFoods = {
            "chicken breast", "ground beef", "salmon", "eggs", "milk", "cheese", "rice", "pasta",
            "broccoli", "carrots", "apple", "banana", "bread", "yogurt", "olive oil"
        };
        
        for (String food : popularFoods) {
            model.addElement(food);
        }
    }
} 