package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.domain.entities.Meal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * MealLogRepo - Repository interface for meal log data access
 * Part of the Infrastructure Layer
 */
public interface MealLogRepo {
    
    // Basic CRUD operations
    Meal save(Meal meal);
    Meal findById(UUID mealId);
    void delete(UUID mealId);
    List<Meal> findAll();
    Meal update(Meal meal);
    
    // Legacy method names for backward compatibility
    void saveMeal(Meal meal);
    void deleteMeal(UUID mealId);
    List<Meal> listAllMeals();
    
    // Meal-specific queries
    List<Meal> findByProfileId(UUID profileId);
    List<Meal> findByProfileIdAndDate(UUID profileId, LocalDate date);
    List<Meal> findByProfileIdAndDateRange(UUID profileId, LocalDate startDate, LocalDate endDate);
    List<Meal> findByProfileIdAndMealType(UUID profileId, String mealType);
    List<Meal> findByDate(LocalDate date);
    List<Meal> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Meal> findByMealType(String mealType);
    
    // Aggregate queries
    int countMealsForProfile(UUID profileId);
    int countMealsForDate(UUID profileId, LocalDate date);
    boolean existsByProfileIdAndDate(UUID profileId, LocalDate date);
} 