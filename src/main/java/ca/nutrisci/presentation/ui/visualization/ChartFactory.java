package ca.nutrisci.presentation.ui.visualization;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.SwapImpactDTO;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.awt.Color;
import java.awt.Font;

/**
 * Factory for creating JFreeChart instances from ChartDTO
 */
public class ChartFactory {
    // Existing colors
    private static final Color ACTUAL_COLOR = new Color(65, 105, 225);    // Royal Blue
    private static final Color RECOMMENDED_COLOR = new Color(50, 205, 50); // Lime Green
    private static final Color BEFORE_COLOR = new Color(65, 105, 225);    // Royal Blue
    private static final Color AFTER_COLOR = new Color(50, 205, 50);      // Lime Green

    // New nutrient colors
    private static final Color PROTEIN_COLOR = new Color(51, 122, 183);   // Blue
    private static final Color CARBS_COLOR = new Color(217, 83, 79);      // Red
    private static final Color FAT_COLOR = new Color(92, 184, 92);        // Green
    private static final Color FIBER_COLOR = new Color(240, 173, 78);     // Orange
    private static final Color CALORIES_COLOR = new Color(153, 51, 153);  // Purple
    private static final Color OTHER_COLOR = new Color(204, 204, 204);    // Light Gray

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);

    public static JFreeChart createChart(ChartDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ChartDTO cannot be null");
        }

        if (!dto.hasData()) {
            return createEmptyChart(dto.getTitle(), dto.getChartType());
        }

        switch (dto.getChartType()) {
            case PIE:
                return createPieChart(dto);
            case BAR:
                return createBarChart(dto);
            default:
                throw new IllegalArgumentException("Unsupported chart type: " + dto.getChartType());
        }
    }

    /**
     * Creates a grouped bar chart comparing actual vs recommended values
     */
    public static JFreeChart createGroupedBarChart(GroupedBarChartDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("GroupedBarChartDTO cannot be null");
        }

        if (!dto.hasData()) {
            return createEmptyChart(dto.getTitle(), ChartDTO.ChartType.GROUPED_BAR);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add actual values
        dto.getCategories().forEach(category -> 
            dataset.addValue(
                dto.getActuals().getOrDefault(category, 0.0),
                "Actual",
                category
            )
        );

        // Add recommended values
        dto.getCategories().forEach(category -> 
            dataset.addValue(
                dto.getRecommendations().getOrDefault(category, 0.0),
                "Recommended",
                category
            )
        );

        // Create the chart
        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            dto.getTitle(),
            dto.getXAxisLabel(),
            dto.getYAxisLabel(),
            dataset,
            PlotOrientation.VERTICAL,
            true,   // include legend
            true,   // include tooltips
            false   // no URLs
        );

        // Customize the chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(TITLE_FONT);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineVisible(false);

        // Customize the axes
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(LABEL_FONT);
        domainAxis.setTickLabelFont(LABEL_FONT);
        domainAxis.setCategoryMargin(0.2);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(LABEL_FONT);
        rangeAxis.setTickLabelFont(LABEL_FONT);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Customize the bars with new colors
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PROTEIN_COLOR);    // Actual values in blue
        renderer.setSeriesPaint(1, CALORIES_COLOR);   // Recommended values in purple
        renderer.setDrawBarOutline(true);
        renderer.setItemMargin(0.1);  // Space between bars in a group
        renderer.setShadowVisible(false);

        return chart;
    }

    private static JFreeChart createPieChart(ChartDTO dto) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dto.getDataPoints().forEach(dataset::setValue);
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart(
            dto.getTitle(),
            dataset,
            true,  // include legend
            true,  // include tooltips
            false  // no URLs
        );

        // Apply consistent styling
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(TITLE_FONT);
        
        // Customize section colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Protein", PROTEIN_COLOR);
        plot.setSectionPaint("Carbohydrates", CARBS_COLOR);
        plot.setSectionPaint("Fat", FAT_COLOR);
        plot.setSectionPaint("Fiber", FIBER_COLOR);
        plot.setSectionPaint("Calories", CALORIES_COLOR);
        plot.setSectionPaint("Other", OTHER_COLOR);
        
        return chart;
    }

    private static JFreeChart createBarChart(ChartDTO dto) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dto.getDataPoints().forEach((label, value) -> 
            dataset.addValue(value, dto.getYAxisLabel(), label)
        );
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            dto.getTitle(),
            dto.getXAxisLabel(),
            dto.getYAxisLabel(),
            dataset,
            PlotOrientation.VERTICAL,
            true,   // include legend
            true,   // include tooltips
            false   // no URLs
        );

        // Apply consistent styling
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(TITLE_FONT);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        return chart;
    }

    private static JFreeChart createEmptyChart(String title, ChartDTO.ChartType type) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "No data", "");
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            title,
            "",
            "",
            dataset,
            PlotOrientation.VERTICAL,
            false,  // no legend
            false,  // no tooltips
            false   // no URLs
        );

        // Apply consistent styling
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(TITLE_FONT);
        
        return chart;
    }

    /**
     * Creates a chart showing the impact of food swaps
     */
    public static JFreeChart createSwapImpactChart(SwapImpactDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("SwapImpactDTO cannot be null");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add before data points
        dto.getBeforeData().forEach((date, value) -> 
            dataset.addValue(value, "Before", date.toString())
        );

        // Add after data points
        dto.getAfterData().forEach((date, value) -> 
            dataset.addValue(value, "After", date.toString())
        );

        // Create chart based on style
        JFreeChart chart;
        if ("Line Chart".equalsIgnoreCase(dto.getChartStyle())) {
            chart = org.jfree.chart.ChartFactory.createLineChart(
                dto.getTitle(),
                dto.getXLabel(),
                dto.getYLabel(),
                dataset,
                PlotOrientation.VERTICAL,
                true,  // legend
                true,  // tooltips
                false  // urls
            );
        } else {
            // Default to bar chart
            chart = org.jfree.chart.ChartFactory.createBarChart(
                dto.getTitle(),
                dto.getXLabel(),
                dto.getYLabel(),
                dataset,
                PlotOrientation.VERTICAL,
                true,  // legend
                true,  // tooltips
                false  // urls
            );
        }

        // Customize the chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        // Set colors
        plot.getRenderer().setSeriesPaint(0, BEFORE_COLOR);
        plot.getRenderer().setSeriesPaint(1, AFTER_COLOR);

        // Customize fonts
        chart.getTitle().setFont(TITLE_FONT);
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(LABEL_FONT);
        domainAxis.setLabelFont(LABEL_FONT);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(LABEL_FONT);
        rangeAxis.setLabelFont(LABEL_FONT);

        return chart;
    }
} 