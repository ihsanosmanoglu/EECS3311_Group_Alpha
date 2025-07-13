package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.infrastructure.database.DatabaseManager;

/**
 * JdbcRepoFactory - Concrete factory for JDBC-based repositories
 * Part of the Infrastructure Layer - Abstract Factory Pattern
 * Uses database for persistent storage across any SQL database
 */
public class JdbcRepoFactory implements IRepositoryFactory {
    
    private final DatabaseManager dbManager;
    private ProfileRepo profileRepo;
    private MealLogRepo mealLogRepo;
    private SwapHistoryRepo swapHistoryRepo;
    
    public JdbcRepoFactory() {
        this.dbManager = DatabaseManager.getInstance();
        System.out.println("🗃️ JdbcRepoFactory initialized with " + dbManager.getDatabaseType().toUpperCase() + " database");
    }
    
    @Override
    public ProfileRepo getProfileRepository() {
        if (profileRepo == null) {
            profileRepo = new JdbcProfileRepo();
            System.out.println("📊 JDBC ProfileRepo created");
        }
        return profileRepo;
    }
    
    @Override
    public MealLogRepo getMealLogRepository() {
        if (mealLogRepo == null) {
            mealLogRepo = new JdbcMealLogRepo();
            System.out.println("🍽️ JDBC MealLogRepo created");
        }
        return mealLogRepo;
    }
    
    @Override
    public SwapHistoryRepo getSwapHistoryRepository() {
        if (swapHistoryRepo == null) {
            // For now, fall back to file-based implementation
            // TODO: Implement JDBC SwapHistoryRepo when needed
            swapHistoryRepo = new FileSwapHistoryRepo("data/app");
            System.out.println("🔄 File-based SwapHistoryRepo created (JDBC implementation pending)");
        }
        return swapHistoryRepo;
    }
    
    /**
     * Get database connection status
     */
    public String getDatabaseStatus() {
        if (dbManager.isAvailable()) {
            return String.format("✅ Database Connected: %s", 
                               dbManager.getDatabaseType().toUpperCase());
        } else {
            return "❌ Database Not Available";
        }
    }
    
    /**
     * Check if database is available
     */
    public boolean isAvailable() {
        return dbManager.isAvailable();
    }
} 