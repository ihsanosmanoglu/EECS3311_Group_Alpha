# Canadian Nutrient File (CNF) Data

This directory contains the CSV files from the Canadian Nutrient File database.

## Files Expected:

- `CONVERSION_FACTOR.csv` - Conversion factors for different measurements
- `FOOD_GROUP.csv` - Food group classifications  
- `FOOD_NAME.csv` - Food item names and identifiers
- `FOOD_SOURCE.csv` - Sources of food data
- `MEASURE_NAME.csv` - Measurement unit definitions
- `NUTRIENT_AMOUNT.csv` - Nutrient content data (main data file)
- `NUTRIENT_NAME.csv` - Nutrient type definitions
- `NUTRIENT_SOURCE.csv` - Sources of nutrient data
- `REFUSE_AMOUNT.csv` - Non-edible portion data
- `REFUSE_NAME.csv` - Refuse type definitions
- `YIELD_AMOUNT.csv` - Cooking yield factors
- `YIELD_NAME.csv` - Yield type definitions

## Data Source:

Health Canada - Canadian Nutrient File (CNF) 2015
https://www.canada.ca/en/health-canada/services/food-nutrition/healthy-eating/nutrient-data.html

## Usage:

These files are loaded by the `CNFDataAdapter` class to provide nutrition information for the application.

## Note:

The actual CSV files should be placed in this directory. They are not included in the repository due to size and licensing considerations. 