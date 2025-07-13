package ca.nutrisci.application.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for Canada Food Guide alignment report
 */
public class CfgReportDTO {
    private final Map<String, Double> actualProportions;
    private final Map<String, Double> targetProportions;
    private final Map<String, Double> deviations;

    public CfgReportDTO() {
        this.actualProportions = new HashMap<>();
        this.targetProportions = new HashMap<>();
        this.deviations = new HashMap<>();
    }

    public void addFoodGroup(String group, double actual, double target) {
        actualProportions.put(group, actual);
        targetProportions.put(group, target);
        deviations.put(group, actual - target);
    }

    public Map<String, Double> getActualProportions() {
        return new HashMap<>(actualProportions);
    }

    public Map<String, Double> getTargetProportions() {
        return new HashMap<>(targetProportions);
    }

    public Map<String, Double> getDeviations() {
        return new HashMap<>(deviations);
    }
} 