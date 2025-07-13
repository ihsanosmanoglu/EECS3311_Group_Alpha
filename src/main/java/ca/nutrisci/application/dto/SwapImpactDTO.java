package ca.nutrisci.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data transfer object for representing the impact of food swaps on nutrient intake
 * by comparing two time periods.
 */
public class SwapImpactDTO {
    private String title;
    private String xLabel;
    private String yLabel;
    private String selectedNutrient;
    private String chartStyle;
    private List<String> nutrients;
    private Map<String, Number> beforeValues;
    private Map<String, Number> afterValues;
    private Map<LocalDate, Double> beforeData;
    private Map<LocalDate, Double> afterData;

    public SwapImpactDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXLabel() {
        return xLabel;
    }

    public void setXLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public String getYLabel() {
        return yLabel;
    }

    public void setYLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public String getSelectedNutrient() {
        return selectedNutrient;
    }

    public void setSelectedNutrient(String selectedNutrient) {
        this.selectedNutrient = selectedNutrient;
    }

    public String getChartStyle() {
        return chartStyle;
    }

    public void setChartStyle(String chartStyle) {
        this.chartStyle = chartStyle;
    }

    public List<String> getNutrients() {
        return nutrients;
    }

    public void setNutrients(List<String> nutrients) {
        this.nutrients = nutrients;
    }

    public Map<String, Number> getBeforeValues() {
        return beforeValues;
    }

    public void setBeforeValues(Map<String, Number> beforeValues) {
        this.beforeValues = beforeValues;
    }

    public Map<String, Number> getAfterValues() {
        return afterValues;
    }

    public void setAfterValues(Map<String, Number> afterValues) {
        this.afterValues = afterValues;
    }

    public Map<LocalDate, Double> getBeforeData() {
        return beforeData;
    }

    public void setBeforeData(Map<LocalDate, Double> beforeData) {
        this.beforeData = beforeData;
    }

    public Map<LocalDate, Double> getAfterData() {
        return afterData;
    }

    public void setAfterData(Map<LocalDate, Double> afterData) {
        this.afterData = afterData;
    }
} 