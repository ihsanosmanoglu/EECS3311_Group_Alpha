package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.domain.entities.SwapHistory;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FileSwapHistoryRepo - File-based implementation for SwapHistoryRepo
 * Part of the Infrastructure Layer
 * This repository persists swap history data to a CSV file.
 */
public class FileSwapHistoryRepo implements SwapHistoryRepo {
    
    private final String filePath;
    private final Map<UUID, SwapHistory> swaps = new LinkedHashMap<>();
    private static final String[] HEADERS = {"id", "profileId", "originalFood", "replacementFood", "swapReason", "goalType", "impactScore", "createdAt"};

    public FileSwapHistoryRepo(String filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                saveAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                SwapHistory swap = new SwapHistory(
                    UUID.fromString(row[0]),
                    UUID.fromString(row[1]),
                    row[2],
                    row[3],
                    row[4],
                    row[5],
                    Double.parseDouble(row[6]),
                    LocalDateTime.parse(row[7])
                );
                swaps.put(swap.getId(), swap);
            }
        } catch (IOException | CsvException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private synchronized void saveAll() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(HEADERS);
            for (SwapHistory swap : swaps.values()) {
                writer.writeNext(new String[]{
                    swap.getId().toString(),
                    swap.getProfileId().toString(),
                    swap.getOriginalFood(),
                    swap.getReplacementFood(),
                    swap.getSwapReason(),
                    swap.getGoalType(),
                    String.valueOf(swap.getImpactScore()),
                    swap.getCreatedAt().toString()
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SwapHistory save(SwapHistory swapHistory) {
        if (swapHistory.getId() == null) {
            swapHistory.setId(UUID.randomUUID());
        }
        if (swapHistory.getCreatedAt() == null) {
            swapHistory.setCreatedAt(LocalDateTime.now());
        }
        swaps.put(swapHistory.getId(), swapHistory);
        saveAll();
        return swapHistory;
    }

    @Override
    public SwapHistory findById(UUID swapHistoryId) {
        return swaps.get(swapHistoryId);
    }

    @Override
    public void delete(UUID swapHistoryId) {
        swaps.remove(swapHistoryId);
        saveAll();
    }

    @Override
    public List<SwapHistory> findAll() {
        return new ArrayList<>(swaps.values());
    }

    @Override
    public SwapHistory update(SwapHistory swapHistory) {
        if (swapHistory.getId() == null || !swaps.containsKey(swapHistory.getId())) {
            throw new IllegalArgumentException("Swap history not found for update");
        }
        swaps.put(swapHistory.getId(), swapHistory);
        saveAll();
        return swapHistory;
    }

    @Override
    public void saveSwapHistory(SwapHistory swapHistory) {
        save(swapHistory);
    }

    @Override
    public void deleteSwapHistory(UUID swapHistoryId) {
        delete(swapHistoryId);
    }

    @Override
    public List<SwapHistory> listAllSwapHistories() {
        return findAll();
    }

    @Override
    public List<SwapHistory> findByProfileId(UUID profileId) {
        return swaps.values().stream()
                .filter(s -> s.getProfileId().equals(profileId))
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapHistory> findByProfileIdAndGoalType(UUID profileId, String goalType) {
        return swaps.values().stream()
                .filter(s -> s.getProfileId().equals(profileId) && s.getGoalType().equalsIgnoreCase(goalType))
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapHistory> findByGoalType(String goalType) {
        return swaps.values().stream()
                .filter(s -> s.getGoalType().equalsIgnoreCase(goalType))
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapHistory> findByOriginalFood(String originalFood) {
        return swaps.values().stream()
                .filter(s -> s.getOriginalFood().equalsIgnoreCase(originalFood))
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapHistory> findByReplacementFood(String replacementFood) {
        return swaps.values().stream()
                .filter(s -> s.getReplacementFood().equalsIgnoreCase(replacementFood))
                .collect(Collectors.toList());
    }

    @Override
    public int countSwapsForProfile(UUID profileId) {
        return (int) findByProfileId(profileId).size();
    }

    @Override
    public int countSwapsByGoalType(String goalType) {
        return (int) findByGoalType(goalType).size();
    }

    @Override
    public boolean existsByProfileIdAndGoalType(UUID profileId, String goalType) {
        return swaps.values().stream()
                .anyMatch(s -> s.getProfileId().equals(profileId) && s.getGoalType().equalsIgnoreCase(goalType));
    }

    @Override
    public double getAverageImpactScore(UUID profileId) {
        return findByProfileId(profileId).stream()
                .mapToDouble(SwapHistory::getImpactScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public List<SwapHistory> findTopSwapsByImpactScore(int limit) {
        return swaps.values().stream()
                .sorted(Comparator.comparingDouble(SwapHistory::getImpactScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapHistory> findRecentSwaps(UUID profileId, int limit) {
        return findByProfileId(profileId).stream()
                .sorted(Comparator.comparing(SwapHistory::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
} 