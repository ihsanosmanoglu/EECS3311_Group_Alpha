package ca.nutrisci.presentation.ui.visualization;

import ca.nutrisci.presentation.controllers.VisualizationController;
import com.toedter.calendar.JDateChooser;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * VisualizationPanel is responsible for displaying charts and visual analytics in the application.
 */
public class VisualizationPanel extends JPanel {
    private JPanel chartContainer;
    private JLabel placeholderLabel;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JComboBox<String> chartTypeCombo;
    private JComboBox<String> nutrientCombo;
    private JComboBox<String> chartStyleCombo;
    private JPanel nutrientPanel;
    private JPanel chartStylePanel;
    private VisualizationController controller;
    private JPanel feedbackPanel = new JPanel();

    // Chart type options
    private static final String TOP_NUTRIENTS = "Top Nutrients";
    private static final String CFG_ALIGNMENT = "CFG Alignment";
    private static final String FOOD_SWAP_IMPACT = "Food Swap Impact";

    // Nutrient options
    private static final String[] NUTRIENTS = {
        "Protein", "Carbohydrates", "Fat", "Fiber", "Sugar", "Calories"
    };

    // Chart style options
    private static final String[] CHART_STYLES = {
        "Bar Chart", "Line Chart"
    };

    public VisualizationPanel() {
        setLayout(new BorderLayout());
        
        // Initialize components
        chartContainer = new JPanel(new BorderLayout());
        placeholderLabel = new JLabel("[Visualization Panel: Chart will appear here]");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Create main control panel with vertical BoxLayout
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        
        // Date Range Panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Date Range", 
            TitledBorder.LEFT, TitledBorder.TOP));
        
        JLabel startLabel = new JLabel("Start Date:");
        startDateChooser = new JDateChooser();
        startDateChooser.setDate(Date.from(LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        JLabel endLabel = new JLabel("End Date:");
        endDateChooser = new JDateChooser();
        endDateChooser.setDate(new Date());
        
        datePanel.add(startLabel);
        datePanel.add(startDateChooser);
        datePanel.add(endLabel);
        datePanel.add(endDateChooser);
        
        // Chart Type Panel
        JPanel chartTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chartTypePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Chart Options", 
            TitledBorder.LEFT, TitledBorder.TOP));
        
        JLabel chartTypeLabel = new JLabel("Chart Type:");
        chartTypeCombo = new JComboBox<>(new String[]{TOP_NUTRIENTS, CFG_ALIGNMENT, FOOD_SWAP_IMPACT});
        chartTypeCombo.addActionListener(e -> updateVisiblePanels());
        
        chartTypePanel.add(chartTypeLabel);
        chartTypePanel.add(chartTypeCombo);
        
        // Nutrient Selection Panel (initially hidden)
        nutrientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel nutrientLabel = new JLabel("Nutrient:");
        nutrientCombo = new JComboBox<>(NUTRIENTS);
        nutrientPanel.add(nutrientLabel);
        nutrientPanel.add(nutrientCombo);
        nutrientPanel.setVisible(false);
        
        // Chart Style Panel (initially hidden)
        chartStylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel styleLabel = new JLabel("Chart Style:");
        chartStyleCombo = new JComboBox<>(CHART_STYLES);
        chartStylePanel.add(styleLabel);
        chartStylePanel.add(chartStyleCombo);
        chartStylePanel.setVisible(false);
        
        chartTypePanel.add(nutrientPanel);
        chartTypePanel.add(chartStylePanel);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton generateButton = new JButton("Generate Chart");
        generateButton.addActionListener(e -> generateChart());
        buttonPanel.add(generateButton);
        
        // Add all panels to control panel
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(datePanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(chartTypePanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(buttonPanel);
        controlPanel.add(Box.createVerticalStrut(5));
        
        // Add components to main panel
        add(controlPanel, BorderLayout.NORTH);
        add(chartContainer, BorderLayout.CENTER);
        add(feedbackPanel, BorderLayout.SOUTH);
        
        // Add placeholder initially
        chartContainer.add(placeholderLabel, BorderLayout.CENTER);
    }

    private void updateVisiblePanels() {
        boolean isSwapImpact = FOOD_SWAP_IMPACT.equals(chartTypeCombo.getSelectedItem());
        nutrientPanel.setVisible(isSwapImpact);
        chartStylePanel.setVisible(isSwapImpact);
        revalidate();
        repaint();
    }

    private void generateChart() {
        if (controller == null) {
            return;
        }
        // Convert dates to LocalDate
        LocalDate startDate = startDateChooser.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = endDateChooser.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        String selectedType = (String) chartTypeCombo.getSelectedItem();
        UUID profileId = controller.getCurrentProfileId();
        System.out.println("[DEBUG] generateChart: startDate=" + startDate + ", endDate=" + endDate + ", chartType=" + selectedType + ", profileId=" + profileId);
        // Validate date range
        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this,
                    "Start date cannot be after end date",
                    "Invalid Date Range",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        switch (selectedType) {
            case TOP_NUTRIENTS:
                controller.loadDailyIntakeChart(startDate, endDate);
                break;
            case CFG_ALIGNMENT:
                controller.loadCfgAlignmentChart(startDate, endDate);
                break;
            case FOOD_SWAP_IMPACT:
                String nutrient = (String) nutrientCombo.getSelectedItem();
                String chartStyle = (String) chartStyleCombo.getSelectedItem();
                controller.loadSwapImpactChart(startDate, endDate, nutrient, chartStyle, profileId);
                break;
        }
    }

    public void setController(VisualizationController controller) {
        this.controller = controller;
    }

    public void updateChart(JFreeChart chart, Object chartDTO) {
        chartContainer.removeAll();
        feedbackPanel.removeAll();

        boolean showMessage = false;
        String message = null;
        if (chartDTO != null) {
            try {
                boolean hasData = (boolean) chartDTO.getClass().getMethod("hasData").invoke(chartDTO);
                Object msgObj = chartDTO.getClass().getMethod("getMessage").invoke(chartDTO);
                if (!hasData && msgObj != null) {
                    message = msgObj.toString();
                    showMessage = true;
                }
            } catch (Exception e) {
                // ignore
            }
        }

        if (chart != null && !showMessage) {
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 400));
            chartPanel.setMouseWheelEnabled(true);
            chartPanel.setMouseZoomable(true);
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            // Contextual feedback for CFG Alignment chart
            if (chartDTO != null && chartDTO.getClass().getSimpleName().equals("GroupedBarChartDTO")) {
                try {
                    java.util.List<String> categories = (java.util.List<String>) chartDTO.getClass().getMethod("getCategories").invoke(chartDTO);
                    java.util.Map<String, Double> actuals = (java.util.Map<String, Double>) chartDTO.getClass().getMethod("getActuals").invoke(chartDTO);
                    java.util.Map<String, Double> recommendations = (java.util.Map<String, Double>) chartDTO.getClass().getMethod("getRecommendations").invoke(chartDTO);
                    boolean anyWithin50 = false;
                    int within10 = 0;
                    JPanel feedbackList = new JPanel();
                    feedbackList.setLayout(new BoxLayout(feedbackList, BoxLayout.Y_AXIS));
                    for (String group : categories) {
                        double actual = actuals.getOrDefault(group, 0.0);
                        double rec = recommendations.getOrDefault(group, 0.0);
                        String icon = "";
                        double percent = rec > 0 ? (actual / rec) * 100 : 0;
                        if (rec > 0 && Math.abs(percent - 100) <= 10) { icon = "✅"; within10++; anyWithin50 = true; }
                        else if (rec > 0 && percent < 70) { icon = "⚠️"; }
                        else if (rec > 0 && percent > 300) { icon = "❌"; }
                        else if (rec > 0 && Math.abs(percent - 100) <= 50) { anyWithin50 = true; }
                        String label = String.format("%s %s: %.0f%% of recommended", icon, group, percent);
                        JLabel feedbackLabel = new JLabel(label);
                        feedbackList.add(feedbackLabel);
                    }
                    feedbackPanel.add(feedbackList);
                    if (!anyWithin50) {
                        JLabel warn = new JLabel("Your current intake is far from CFG recommendations. Consider adjusting your meals.");
                        warn.setForeground(java.awt.Color.RED);
                        feedbackPanel.add(warn);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        } else if (showMessage && message != null) {
            JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
            messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
            chartContainer.add(messageLabel, BorderLayout.CENTER);
        } else {
            chartContainer.add(placeholderLabel, BorderLayout.CENTER);
        }

        chartContainer.revalidate();
        chartContainer.repaint();
        feedbackPanel.revalidate();
        feedbackPanel.repaint();
    }
} 