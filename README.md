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
- MySQL 8.0+ (optional, can use file-based storage)

### Building the Project
```bash
mvn clean compile
```

### Running the Application
```bash
mvn javafx:run
```

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
- **Extensible**: Ready for additional API integrations

## 🧪 Testing

```bash
mvn test
```

## 📝 Documentation

- [Architecture Overview](docs/architecture-overview.md)
- [API Documentation](docs/api/)
- [User Guide](docs/user-guide.md)

## 🤝 Contributing

This is an academic project. Please follow the established architecture patterns and SOLID principles.

## 📄 License

Educational use only. 