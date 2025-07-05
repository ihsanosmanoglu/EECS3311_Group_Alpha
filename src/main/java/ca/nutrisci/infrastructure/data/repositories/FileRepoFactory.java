package ca.nutrisci.infrastructure.data.repositories;

/**
 * FileRepoFactory - Concrete factory for creating file-based repositories
 * Part of the Infrastructure Layer - Abstract Factory Pattern
 * This implementation provides repositories that persist data to CSV files.
 */
public class FileRepoFactory implements IRepositoryFactory {
    
    private final String dataPath;
    private ProfileRepo profileRepo;
    private MealLogRepo mealLogRepo;
    private SwapHistoryRepo swapHistoryRepo;

    /**
     * Constructor for FileRepoFactory.
     * @param dataPath The base directory path where data files are stored.
     */
    public FileRepoFactory(String dataPath) {
        this.dataPath = dataPath;
    }

    /**
     * Lazily initializes and returns a singleton instance of FileProfileRepo.
     * @return A ProfileRepo implementation that works with files.
     */
    @Override
    public ProfileRepo getProfileRepository() {
        if (profileRepo == null) {
            profileRepo = new FileProfileRepo(dataPath + "/profiles.csv");
        }
        return profileRepo;
    }

    /**
     * Lazily initializes and returns a singleton instance of FileMealLogRepo.
     * @return A MealLogRepo implementation that works with files.
     */
    @Override
    public MealLogRepo getMealLogRepository() {
        if (mealLogRepo == null) {
            mealLogRepo = new FileMealLogRepo(dataPath + "/meals.csv");
        }
        return mealLogRepo;
    }

    /**
     * Lazily initializes and returns a singleton instance of FileSwapHistoryRepo.
     * @return A SwapHistoryRepo implementation that works with files.
     */
    @Override
    public SwapHistoryRepo getSwapHistoryRepository() {
        if (swapHistoryRepo == null) {
            swapHistoryRepo = new FileSwapHistoryRepo(dataPath + "/swaps.csv");
        }
        return swapHistoryRepo;
    }
} 