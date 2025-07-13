# NutriSci: SwEATch to better! 🥗

A comprehensive nutrition tracking and food swap recommendation application that helps users understand their daily nutrient intake and improve it through smart food substitutions.

## 🏗️ Project Structure

This project follows **Clean Architecture** principles with clear separation of concerns:

```
src/main/java/ca/nutrisci/
├── presentation/           # UI Layer
│   ├── controllers/        # Screen Controllers
│   ├── mediator/          # Navigation Mediator
│   └── ui/                # Main Application
├── application/           # Business Logic Layer
│   ├── facades/           # Facade Pattern Implementations
│   ├── services/          # Business Services
│   └── dto/               # Data Transfer Objects
├── domain/                # Core Domain Layer
│   ├── entities/          # Domain Entities
│   └── strategies/        # Strategy Pattern for Swaps
└── infrastructure/        # External Concerns Layer
    ├── data/repositories/ # Repository Pattern
    └── external/adapters/ # Adapter Pattern for External APIs
```

## 🎯 Design Patterns Implemented

- **Facade Pattern**: Simplified interfaces for complex subsystems
- **Strategy Pattern**: Pluggable swap algorithms
- **Abstract Factory Pattern**: Repository creation
- **Observer Pattern**: Background calculations
- **Mediator Pattern**: UI navigation coordination
- **Adapter Pattern**: External data source integration

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+ (optional, can use file-based storage) (but will also work with others later in future)


### Building the Project
```bash
mvn clean compile
```

### Running the Application
```bash
mvn javafx:run  
```
Does not run yet, hopefully in future.

## 📊 Features

- **Profile Management**: Create and manage user profiles with personalized settings
- **Meal Logging**: Track daily food intake with automatic nutrient calculation
- **Smart Food Swaps**: AI-powered recommendations for healthier alternatives
- **Visualization**: Rich charts and graphs using JFreeChart
- **Canada Food Guide**: Alignment tracking with CFG recommendations
- **Background Processing**: Real-time calculation updates

## 🗄️ Data Sources

- **Canadian Nutrient File (CNF)**: Comprehensive nutrition database
- **Canada Food Guide**: Official dietary recommendations

## 🔍 Database Management

### What is H2?
**H2 is a simple SQL database** that stores your data in a single file:
- **File location**: `./data/nutrisci_db.mv.db`
- **Type**: Regular SQL database (like MySQL, but simpler)
- **Tables**: PROFILES, MEALS, CNF_FOODS, USER_PREFERENCES

### View Your Data (Simple Commands)

#### Super Simple Way (Recommended)
We've created a simple script for you:
```bash
# Make script executable (one time)
chmod +x view_db.sh

# View your data (stop your app first)
./view_db.sh tables      # See all tables
./view_db.sh profiles    # See your profiles  
./view_db.sh meals       # See your meals
./view_db.sh count       # Count your data

# Run any SQL you want
./view_db.sh "SELECT * FROM MEALS WHERE MEAL_TYPE='breakfast';"
```

#### Manual Commands (If you prefer)
If you don't want to use the script:

#### Step 1: Prepare H2 Tools (One Time Setup)
```bash
mvn dependency:copy-dependencies -DincludeArtifactIds=h2 -DoutputDirectory=target/dependency
```

#### Step 2: View Your Tables
```bash
# Stop your app first, then run these commands:

# See all tables
java -cp target/dependency/h2-2.2.224.jar org.h2.tools.Shell -url "jdbc:h2:file:./data/nutrisci_db" -user sa -sql "SHOW TABLES;"

# See your profiles
java -cp target/dependency/h2-2.2.224.jar org.h2.tools.Shell -url "jdbc:h2:file:./data/nutrisci_db" -user sa -sql "SELECT * FROM PROFILES;"

# See your meals
java -cp target/dependency/h2-2.2.224.jar org.h2.tools.Shell -url "jdbc:h2:file:./data/nutrisci_db" -user sa -sql "SELECT * FROM MEALS;"

# Count how much data you have
java -cp target/dependency/h2-2.2.224.jar org.h2.tools.Shell -url "jdbc:h2:file:./data/nutrisci_db" -user sa -sql "SELECT COUNT(*) FROM PROFILES;"
```

#### Quick Data Check
```bash
# See what's in your data folder
ls -la data/

# Check database file size
ls -lh data/nutrisci_db.mv.db
```

### If Commands Don't Work

#### Database is Locked?
```bash
# Your app is probably running - stop it first
ps aux | grep java
kill <process_number>
```

#### Start Fresh?
```bash
# Delete all data (WARNING: Deletes everything!)
rm -rf data/
# Your app will create a new empty database next time you run it
```

### Simple Backup
```bash
# Backup your data
cp -r data/ my_backup/
```

## 📝 Documentation

- [Architecture Overview](docs/architecture-overview.md)
- [API Documentation](docs/api/)
- [User Guide](docs/user-guide.md)

