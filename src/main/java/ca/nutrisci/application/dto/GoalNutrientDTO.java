package ca.nutrisci.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalNutrientDTO {
    private String nutrientName;
    private double amount;
    private String unit;
} 