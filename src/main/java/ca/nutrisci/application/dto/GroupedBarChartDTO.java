package ca.nutrisci.application.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for grouped bar chart visualization data.
 * Supports comparing actual vs recommended values for each category.
 */
public class GroupedBarChartDTO {
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private ChartDTO.ChartType chartType;
    private List<String> categories;
    private Map<String, Double> actuals;
    private Map<String, Double> recommendations;
    private boolean hasData;
    private String message;

    public GroupedBarChartDTO(String title, String xAxisLabel, String yAxisLabel) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.chartType = ChartDTO.ChartType.GROUPED_BAR;
        this.categories = new ArrayList<>();
        this.actuals = new LinkedHashMap<>();
        this.recommendations = new LinkedHashMap<>();
        this.hasData = true;
    }

    public void addCategory(String category, Double actual, Double recommended) {
        categories.add(category);
        actuals.put(category, actual);
        recommendations.put(category, recommended);
    }

    public String getTitle() {
        return title;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public ChartDTO.ChartType getChartType() {
        return chartType;
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories); // return a copy
    }

    public Map<String, Double> getActuals() {
        return new LinkedHashMap<>(actuals); // return a copy
    }

    public Map<String, Double> getRecommendations() {
        return new LinkedHashMap<>(recommendations); // return a copy
    }

    public boolean hasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static GroupedBarChartDTO createEmptyChart(String title) {
        GroupedBarChartDTO dto = new GroupedBarChartDTO(title, "", "");
        dto.setHasData(false);
        return dto;
    }
} 