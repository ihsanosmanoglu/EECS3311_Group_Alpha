package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.domain.entities.Meal;
import ca.nutrisci.infrastructure.database.DatabaseManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.lang.reflect.Type;

/**
 * JdbcMealLogRepo - JDBC implementation of MealLogRepo
 * Works with any SQL database (MySQL, PostgreSQL, H2, SQLite)
 * Uses JSON serialization for ingredients and quantities
 * Part of the Infrastructure Layer - Repository Pattern
 */
public class JdbcMealLogRepo implements MealLogRepo {
    
    private final DatabaseManager dbManager;
    private final Gson gson;
    private final Type stringListType;
    private final Type doubleListType;
    private final Type ingredientListType;
    
    public JdbcMealLogRepo() {
        this.dbManager = DatabaseManager.getInstance();
        this.gson = new Gson();
        this.stringListType = new TypeToken<List<String>>(){}.getType();
        this.doubleListType = new TypeToken<List<Double>>(){}.getType();
        this.ingredientListType = new TypeToken<List<ca.nutrisci.application.dto.IngredientDTO>>(){}.getType();
    }
    
    public MealDTO save(MealDTO meal) {
        String sql = "INSERT INTO meals (id, profile_id, date, meal_type, ingredients_json, ingredients, quantities, calories, protein, carbs, fat, fiber) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            UUID id = (meal.getId() == null) ? UUID.randomUUID() : meal.getId();
            
            stmt.setString(1, id.toString());
            stmt.setString(2, meal.getProfileId().toString());
            stmt.setDate(3, Date.valueOf(meal.getDate()));
            stmt.setString(4, meal.getMealType());
            
            // Store full IngredientDTO list as JSON (with units)
            stmt.setString(5, gson.toJson(meal.getIngredients()));
            
            // Store backward compatibility data
            stmt.setString(6, gson.toJson(meal.getIngredientNames()));
            stmt.setString(7, gson.toJson(meal.getQuantities()));
            
            NutrientInfo nutrients = meal.getNutrients();
            if (nutrients != null) {
                stmt.setDouble(8, nutrients.getCalories());
                stmt.setDouble(9, nutrients.getProtein());
                stmt.setDouble(10, nutrients.getCarbs());
                stmt.setDouble(11, nutrients.getFat());
                stmt.setDouble(12, nutrients.getFiber());
            } else {
                stmt.setDouble(8, 0.0);
                stmt.setDouble(9, 0.0);
                stmt.setDouble(10, 0.0);
                stmt.setDouble(11, 0.0);
                stmt.setDouble(12, 0.0);
            }
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Meal saved to database: " + meal.getMealType() + " (" + id + ")");
                // Return the meal with original IngredientDTO objects (preserving units)
                return new MealDTO(id, meal.getProfileId(), meal.getDate(), meal.getMealType(),
                                 meal.getIngredients(), meal.getNutrients());
            } else {
                throw new RuntimeException("Failed to save meal to database");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error saving meal: " + e.getMessage());
            throw new RuntimeException("Database error while saving meal", e);
        }
    }
    
    public MealDTO update(UUID mealId, MealDTO updatedMeal) {
        String sql = "UPDATE meals SET meal_type = ?, ingredients_json = ?, ingredients = ?, quantities = ?, calories = ?, protein = ?, carbs = ?, fat = ?, fiber = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, updatedMeal.getMealType());
            
            // Store full IngredientDTO list as JSON (with units)
            stmt.setString(2, gson.toJson(updatedMeal.getIngredients()));
            
            // Store backward compatibility data
            stmt.setString(3, gson.toJson(updatedMeal.getIngredientNames()));
            stmt.setString(4, gson.toJson(updatedMeal.getQuantities()));
            
            NutrientInfo nutrients = updatedMeal.getNutrients();
            if (nutrients != null) {
                stmt.setDouble(5, nutrients.getCalories());
                stmt.setDouble(6, nutrients.getProtein());
                stmt.setDouble(7, nutrients.getCarbs());
                stmt.setDouble(8, nutrients.getFat());
                stmt.setDouble(9, nutrients.getFiber());
            } else {
                stmt.setDouble(5, 0.0);
                stmt.setDouble(6, 0.0);
                stmt.setDouble(7, 0.0);
                stmt.setDouble(8, 0.0);
                stmt.setDouble(9, 0.0);
            }
            
            stmt.setString(10, mealId.toString());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Meal updated in database: " + mealId);
                // Return the meal with original IngredientDTO objects (preserving units)
                return new MealDTO(mealId, updatedMeal.getProfileId(), updatedMeal.getDate(),
                                 updatedMeal.getMealType(), updatedMeal.getIngredients(), updatedMeal.getNutrients());
            } else {
                throw new RuntimeException("Meal not found for update: " + mealId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating meal: " + e.getMessage());
            throw new RuntimeException("Database error while updating meal", e);
        }
    }
    
    public void delete(UUID mealId) {
        String sql = "DELETE FROM meals WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, mealId.toString());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Meal deleted from database: " + mealId);
            } else {
                throw new RuntimeException("Meal not found for deletion: " + mealId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting meal: " + e.getMessage());
            throw new RuntimeException("Database error while deleting meal", e);
        }
    }
    
    public MealDTO findById(UUID mealId) {
        String sql = "SELECT * FROM meals WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, mealId.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMealDTO(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding meal by ID: " + e.getMessage());
            throw new RuntimeException("Database error while finding meal", e);
        }
        
        return null;
    }
    
    public List<MealDTO> findByProfileId(UUID profileId) {
        String sql = "SELECT * FROM meals WHERE profile_id = ? ORDER BY date DESC, created_at DESC";
        List<MealDTO> meals = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profileId.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    meals.add(mapResultSetToMealDTO(rs));
                }
            }
            
            System.out.println("📊 Loaded " + meals.size() + " meals for profile: " + profileId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding meals by profile ID: " + e.getMessage());
            throw new RuntimeException("Database error while finding meals", e);
        }
        
        return meals;
    }
    
    public List<MealDTO> findByProfileIdAndDate(UUID profileId, LocalDate date) {
        String sql = "SELECT * FROM meals WHERE profile_id = ? AND date = ? ORDER BY created_at";
        List<MealDTO> meals = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profileId.toString());
            stmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    meals.add(mapResultSetToMealDTO(rs));
                }
            }
            
            System.out.println("📊 Loaded " + meals.size() + " meals for profile " + profileId + " on " + date);
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding meals by profile and date: " + e.getMessage());
            throw new RuntimeException("Database error while finding meals", e);
        }
        
        return meals;
    }
    
    public List<MealDTO> findByProfileIdAndDateRange(UUID profileId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM meals WHERE profile_id = ? AND date BETWEEN ? AND ? ORDER BY date DESC, created_at DESC";
        List<MealDTO> meals = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profileId.toString());
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    meals.add(mapResultSetToMealDTO(rs));
                }
            }
            
            System.out.println("📊 Loaded " + meals.size() + " meals for profile " + profileId + " from " + startDate + " to " + endDate);
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding meals by date range: " + e.getMessage());
            throw new RuntimeException("Database error while finding meals", e);
        }
        
        return meals;
    }
    
    public boolean mealTypeExistsForDate(UUID profileId, LocalDate date, String mealType) {
        String sql = "SELECT 1 FROM meals WHERE profile_id = ? AND date = ? AND meal_type = ? LIMIT 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profileId.toString());
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, mealType.toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking meal type existence: " + e.getMessage());
            return false;
        }
    }
    
    public List<MealDTO> findAll() {
        String sql = "SELECT * FROM meals ORDER BY date DESC, created_at DESC";
        List<MealDTO> meals = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                meals.add(mapResultSetToMealDTO(rs));
            }
            
            System.out.println("📊 Loaded " + meals.size() + " total meals from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding all meals: " + e.getMessage());
            throw new RuntimeException("Database error while finding all meals", e);
        }
        
        return meals;
    }
    
    /**
     * Map ResultSet to MealDTO
     */
    private MealDTO mapResultSetToMealDTO(ResultSet rs) throws SQLException {
        try {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID profileId = UUID.fromString(rs.getString("profile_id"));
            LocalDate date = rs.getDate("date").toLocalDate();
            String mealType = rs.getString("meal_type");
            
            // Create nutrition info
            NutrientInfo nutrients = new NutrientInfo(
                rs.getDouble("calories"),
                rs.getDouble("protein"),
                rs.getDouble("carbs"),
                rs.getDouble("fat"),
                rs.getDouble("fiber")
            );
            
            // Try to load from new ingredients_json column first
            String ingredientsJson = rs.getString("ingredients_json");
            if (ingredientsJson != null && !ingredientsJson.trim().isEmpty()) {
                try {
                    List<ca.nutrisci.application.dto.IngredientDTO> ingredientList = gson.fromJson(ingredientsJson, ingredientListType);
                    return new MealDTO(id, profileId, date, mealType, ingredientList, nutrients);
                } catch (Exception e) {
                    System.err.println("⚠️ Error parsing ingredients_json, falling back to legacy format: " + e.getMessage());
                }
            }
            
            // Fall back to legacy format (ingredients + quantities arrays)
            List<String> ingredients = gson.fromJson(rs.getString("ingredients"), stringListType);
            List<Double> quantities = gson.fromJson(rs.getString("quantities"), doubleListType);
            return new MealDTO(id, profileId, date, mealType, ingredients, quantities, nutrients);
            
        } catch (Exception e) {
            System.err.println("❌ Error mapping meal from database: " + e.getMessage());
            throw new SQLException("Error deserializing meal data", e);
        }
    }
    
    /**
     * Get repository status
     */
    public String getStatus() {
        try {
            int mealCount = findAll().size();
            return String.format("JdbcMealLogRepo - Meals: %d, Database: %s", 
                               mealCount, dbManager.getDatabaseType().toUpperCase());
        } catch (Exception e) {
            return "JdbcMealLogRepo - Error: " + e.getMessage();
        }
    }
    
    // Required interface methods
    @Override
    public List<MealDTO> getMealLogHistory(UUID profileId) {
        return findByProfileId(profileId);
    }
    
    @Override
    public List<MealDTO> getMealsByTimeInterval(UUID profileId, LocalDate startDate, LocalDate endDate) {
        return findByProfileIdAndDateRange(profileId, startDate, endDate);
    }
    
    @Override
    public List<MealDTO> getMealsByDate(UUID profileId, LocalDate date) {
        return findByProfileIdAndDate(profileId, date);
    }
    
    @Override
    public MealDTO getSingleMealById(UUID mealId) {
        return findById(mealId);
    }
    
    @Override
    public MealDTO addMeal(MealDTO meal) {
        return save(meal);
    }
    
    @Override
    public MealDTO editMeal(UUID mealId, MealDTO meal) {
        return update(mealId, meal);
    }
    
    @Override
    public void deleteMeal(UUID mealId) {
        delete(mealId);
    }
    
    @Override
    public boolean mealExists(UUID mealId) {
        return findById(mealId) != null;
    }
    
    @Override
    public List<MealDTO> getMealsByTypeAndDate(UUID profileId, LocalDate date, String mealType) {
        String sql = "SELECT * FROM meals WHERE profile_id = ? AND date = ? AND meal_type = ? ORDER BY created_at";
        List<MealDTO> meals = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profileId.toString());
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, mealType.toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    meals.add(mapResultSetToMealDTO(rs));
                }
            }
            
            System.out.println("📊 Loaded " + meals.size() + " " + mealType + " meals for profile " + profileId + " on " + date);
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding meals by type and date: " + e.getMessage());
            throw new RuntimeException("Database error while finding meals by type and date", e);
        }
        
        return meals;
    }
} 