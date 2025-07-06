# Architecture Overview

## 🏗️ Clean Architecture Implementation

The NutriSci application follows **Clean Architecture** principles with clear separation of concerns across four distinct layers:

## Layer Structure

### 1. Presentation Layer (`ca.nutrisci.presentation`)
**Responsibility**: User Interface and Navigation
- **Controllers**: Handle UI events and user interactions
- **Mediator**: Coordinates navigation between screens (Mediator Pattern)
- **Main Application**: Entry point and JavaFX application setup

### 2. Application Layer (`ca.nutrisci.application`)
**Responsibility**: Business Logic Orchestration
- **Facades**: Simplified interfaces to complex subsystems (Facade Pattern)
- **Services**: Core business logic and calculations
- **DTOs**: Data transfer between layers
- **Observers**: Background calculations (Observer Pattern)

### 3. Domain Layer (`ca.nutrisci.domain`)
**Responsibility**: Core Business Rules
- **Entities**: Core domain objects
- **Strategies**: Swap algorithms (Strategy Pattern)
- **Factories**: Domain object creation

### 4. Infrastructure Layer (`ca.nutrisci.infrastructure`)
**Responsibility**: External Concerns
- **Repositories**: Data access patterns (Repository + Abstract Factory)
- **Adapters**: External API integration (Adapter Pattern)
- **Configuration**: System configuration and setup

## 🎯 Design Patterns Used

### Facade Pattern
- **ProfileManagement**, **MealLogging**, **SwapEngine**, **Visualization**
- Simplifies complex subsystem interactions
- Single entry point for each domain area

### Strategy Pattern
- **SwapStrategy** with concrete implementations
- **ReduceCaloriesStrategy**, **IncreaseFiberStrategy**
- Runtime algorithm selection for food swaps

### Observer Pattern
- **MealLogService** as Subject
- **DailyTotalsCalculator**, **ChartCacheUpdater** as Observers
- Background calculations triggered by meal events

### Abstract Factory Pattern
- **IRepositoryFactory** with **FileRepoFactory**, **JdbcRepoFactory**
- Consistent families of repositories
- Easy switching between storage technologies

### Mediator Pattern
- **NavigationMediator** coordinates UI navigation
- Loose coupling between screen controllers
- Centralized navigation logic

### Adapter Pattern
- **INutritionGateway** with **ExternalAdapter**
- Integrates external nutrition data sources
- Consistent interface regardless of data source

## 🔄 Data Flow

```
User Input → Controller → Facade → Service → Repository/Gateway → External Source
```

### Example: Adding a Meal
1. **User** enters meal data in UI
2. **MealLogController** captures input
3. **MealLogging** facade validates and coordinates
4. **MealLogService** processes business logic
5. **INutritionGateway** fetches nutrient data
6. **MealLogRepo** persists meal data
7. **Observer** triggers background calculations

## 🎨 UI Architecture

### Screen Controllers
- Each screen has dedicated controller
- Controllers only know their specific facade
- No direct controller-to-controller communication

### Navigation Mediator
- Loads FXML screens
- Injects facade dependencies
- Manages screen transitions
- Maintains loose coupling

## 💾 Data Architecture

### Repository Pattern
- Consistent interface for data access
- Technology-agnostic business logic
- Easy testing with mock repositories

### Abstract Factory
- **Phase 1**: File-based storage (CSV)
- **Phase 2**: Database storage (JDBC)
- **Future**: NoSQL, Cloud storage, etc.

### External Integration
- Canadian Nutrient File (CNF) data
- Extensible for REST APIs
- Adapter pattern ensures consistent interface

## 🧪 Testing Strategy

### Unit Testing
- Mock facades for controller testing
- Mock repositories for service testing
- Strategy pattern testing for swap algorithms

### Integration Testing
- Repository implementations
- External adapter functionality
- End-to-end facade operations

### Acceptance Testing
- Complete user scenarios
- UI interaction flows
- Business rule validation

## 🚀 Benefits Achieved

### SOLID Principles
- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Easy to extend without modification
- **Liskov Substitution**: Interfaces enable substitutability
- **Interface Segregation**: Focused, specific interfaces
- **Dependency Inversion**: Depends on abstractions

### Maintainability
- Clear separation of concerns
- Minimal coupling between layers
- Easy to modify individual components

### Testability
- Mock-friendly interfaces
- Isolated business logic
- Clear dependency injection points

### Extensibility
- New swap strategies via Strategy pattern
- New storage types via Abstract Factory
- New external data sources via Adapter pattern
- New UI screens via Mediator pattern 