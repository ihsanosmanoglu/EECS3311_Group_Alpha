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
    public void save(SwapHistory swapHistory) {
        if (swapHistory.getId() == null) {
            swapHistory.setId(UUID.randomUUID());
        }
        if (swapHistory.getCreatedAt() == null) {
            swapHistory.setCreatedAt(LocalDateTime.now());
        }
        swaps.put(swapHistory.getId(), swapHistory);
        saveAll();
    }

    @Override
    public Optional<SwapHistory> findById(UUID swapHistoryId) {
        return Optional.ofNullable(swaps.get(swapHistoryId));
    }

    @Override
    public List<SwapHistory> findByProfileId(UUID profileId) {
        return swaps.values().stream()
                .filter(s -> s.getProfileId().equals(profileId))
                .collect(Collectors.toList());
    }

    @Override
    public List<SwapHistory> getSwapsByDateRange(UUID profileId, java.time.LocalDate from, java.time.LocalDate to) {
        return swaps.values().stream()
                .filter(s -> s.getProfileId().equals(profileId))
                .filter(s -> s.getDate() != null && !s.getDate().isBefore(from) && !s.getDate().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID swapHistoryId) {
        swaps.remove(swapHistoryId);
        saveAll();
    }
} 