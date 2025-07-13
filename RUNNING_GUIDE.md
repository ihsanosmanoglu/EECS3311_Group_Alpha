# NutriSci Meal Logging Application - Running Guide

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Terminal/Command Prompt access

## Quick Start

### 1. Clean and Compile
```bash
mvn clean compile
```

### 2. Run the Application
```bash
mvn exec:java -Dexec.mainClass="ca.nutrisci.presentation.ui.MainApplication" -q
```

### 3. If You Get JSON Parsing Errors
If you see errors like "MalformedJsonException: Unterminated string", clear the corrupted data:
```bash
rm -f data/app/meals.csv
rm -f data/app/profiles.csv
rm -f data/app/swaps.csv
```

Then run again:
```bash
mvn exec:java -Dexec.mainClass="ca.nutrisci.presentation.ui.MainApplication" -q
```

## Testing the Application

### 1. Create a Profile
- Click "Profile Management" tab
- Create a new profile with your information
- Set it as active

### 2. Test Meal Logging
- Click "Meal Logging" tab
- Try adding a breakfast meal:
  - Click "Add Breakfast"
  - Search for foods (e.g., "chicken", "rice", "apple")
  - Select ingredients and click "Add →"
  - Enter quantity (e.g., 100 grams)
  - Click "✓ Create BREAKFAST Meal"

### 3. Verify Features
- Check "Today's breakdown" panel for nutrition totals
- View "Meal Journal" for meal history
- Try different meal types (lunch, dinner, snack)
- Search for different foods using the search box

## Button Colors Fixed

The ingredient selection buttons now have proper colors:
- **Add →** - Green background, white text
- **← Remove** - Red background, white text  
- **Clear All** - Orange background, white text

## Database Features

The application now supports:
- **H2 Database** (default) - Embedded database for data persistence
- **MySQL, PostgreSQL, SQLite** - Switch by editing `src/main/resources/database/database.properties`

## Performance Improvements

- **Singleton Pattern** - CNF data loads only once (fixed duplicate loading)
- **Better Error Handling** - Graceful handling of corrupted CSV files
- **5,700+ Foods** - Full Canada Nutrient File database available

## Troubleshooting

### Issue: White buttons not visible
**Fixed** - Buttons now have proper colors and borders

### Issue: JSON parsing errors
**Fixed** - Better error handling, corrupted files are handled gracefully

### Issue: CNF data loading twice
**Fixed** - Singleton pattern prevents duplicate loading

### Issue: Application won't start
1. Check Java version: `java --version`
2. Check Maven version: `mvn --version`
3. Clear corrupted data files: `rm -f data/app/*.csv`
4. Try fresh compile: `mvn clean compile`

## Features to Test

1. **Meal Creation** - Add different meal types
2. **Ingredient Search** - Search from 5,700+ foods
3. **Food Groups** - Filter by food categories
4. **Nutrition Tracking** - Real-time nutrition totals
5. **Meal History** - View all logged meals
6. **Business Rules** - Try adding duplicate breakfast (should prevent)
7. **Database Persistence** - Meals save between app restarts

## Performance Notes

- First startup may take 30-60 seconds to load CNF data
- Subsequent startups are faster due to caching
- Database operations are optimized for performance

## Success Indicators

✅ Application starts without errors
✅ Buttons are visible with proper colors
✅ CNF data loads only once
✅ Meals can be created and saved
✅ Nutrition data is accurate
✅ No JSON parsing errors
✅ Database persists data between sessions

## Next Steps

Once the application is running successfully:
1. Create multiple profiles to test profile switching
2. Add meals over multiple days to test date filtering
3. Test the swap functionality for recipe suggestions
4. Try different database configurations if needed 