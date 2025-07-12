package ca.nutrisci.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwapResultDTO {
    private SwapDTO swap;
    private ArrayList<Map<String, Double>> nutrientChanges; // e.g., {"calories": -50, "protein": 5}
} 