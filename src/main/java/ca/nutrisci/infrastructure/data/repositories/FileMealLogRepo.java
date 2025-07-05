package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.domain.entities.Meal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FileMealLogRepo - File-based implementation for MealLogRepo
 * Part of the Infrastructure Layer
 * This repository persists meal data to a CSV file, using JSON for complex fields.
 */
public class FileMealLogRepo implements MealLogRepo {
    
    private final String filePath;
    private final Map<UUID, Meal> meals = new LinkedHashMap<>();
    private final Gson gson = new Gson();
    private static final String[] HEADERS = {"id", "profileId", "date", "mealType", "ingredientsJson", "quantitiesJson", "nutrientsJson", "createdAt"};

    public FileMealLogRepo(String filePath) {
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
            Type listStringType = new TypeToken<List<String>>() {}.getType();
            Type listDoubleType = new TypeToken<List<Double>>() {}.getType();

            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                Meal meal = new Meal(
                    UUID.fromString(row[0]),
                    UUID.fromString(row[1]),
                    LocalDate.parse(row[2]),
                    row[3],
                    gson.fromJson(row[4], listStringType),
                    gson.fromJson(row[5], listDoubleType),
                    gson.fromJson(row[6], NutrientInfo.class),
                    LocalDateTime.parse(row[7])
                );
                meals.put(meal.getId(), meal);
            }
        } catch (IOException | CsvException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private synchronized void saveAll() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(HEADERS);
            for (Meal meal : meals.values()) {
                writer.writeNext(new String[]{
                    meal.getId().toString(),
                    meal.getProfileId().toString(),
                    meal.getDate().toString(),
                    meal.getMealType(),
                    gson.toJson(meal.getIngredients()),
                    gson.toJson(meal.getQuantities()),
                    gson.toJson(meal.getNutrients()),
                    meal.getCreatedAt().toString()
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Meal save(Meal meal) {
        if (meal.getId() == null) {
            meal.setId(UUID.randomUUID());
        }
        if (meal.getCreatedAt() == null) {
            meal.setCreatedAt(LocalDateTime.now());
        }
        meals.put(meal.getId(), meal);
        saveAll();
        return meal;
    }

    @Override
    public Meal findById(UUID mealId) {
        return meals.get(mealId);
    }

    @Override
    public void delete(UUID mealId) {
        meals.remove(mealId);
        saveAll();
    }

    @Override
    public List<Meal> findAll() {
        return new ArrayList<>(meals.values());
    }

    @Override
    public Meal update(Meal meal) {
        if (meal.getId() == null || !meals.containsKey(meal.getId())) {
            throw new IllegalArgumentException("Meal not found for update");
        }
        meals.put(meal.getId(), meal);
        saveAll();
        return meal;
    }

    @Override
    public void saveMeal(Meal meal) {
        save(meal);
    }

    @Override
    public void deleteMeal(UUID mealId) {
        delete(mealId);
    }

    @Override
    public List<Meal> listAllMeals() {
        return findAll();
    }

    @Override
    public List<Meal> findByProfileId(UUID profileId) {
        return meals.values().stream()
                .filter(m -> m.getProfileId().equals(profileId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Meal> findByProfileIdAndDate(UUID profileId, LocalDate date) {
        return meals.values().stream()
                .filter(m -> m.getProfileId().equals(profileId) && m.getDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Meal> findByProfileIdAndDateRange(UUID profileId, LocalDate startDate, LocalDate endDate) {
        return meals.values().stream()
                .filter(m -> m.getProfileId().equals(profileId) &&
                              !m.getDate().isBefore(startDate) && !m.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Meal> findByProfileIdAndMealType(UUID profileId, String mealType) {
        return meals.values().stream()
                .filter(m -> m.getProfileId().equals(profileId) && m.getMealType().equalsIgnoreCase(mealType))
                .collect(Collectors.toList());
    }

    @Override
    public List<Meal> findByDate(LocalDate date) {
        return meals.values().stream()
                .filter(m -> m.getDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Meal> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return meals.values().stream()
                .filter(m -> !m.getDate().isBefore(startDate) && !m.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Meal> findByMealType(String mealType) {
        return meals.values().stream()
                .filter(m -> m.getMealType().equalsIgnoreCase(mealType))
                .collect(Collectors.toList());
    }

    @Override
    public int countMealsForProfile(UUID profileId) {
        return (int) meals.values().stream()
                .filter(m -> m.getProfileId().equals(profileId))
                .count();
    }

    @Override
    public int countMealsForDate(UUID profileId, LocalDate date) {
        return (int) meals.values().stream()
                .filter(m -> m.getProfileId().equals(profileId) && m.getDate().equals(date))
                .count();
    }

    @Override
    public boolean existsByProfileIdAndDate(UUID profileId, LocalDate date) {
        return meals.values().stream()
                .anyMatch(m -> m.getProfileId().equals(profileId) && m.getDate().equals(date));
    }
} 