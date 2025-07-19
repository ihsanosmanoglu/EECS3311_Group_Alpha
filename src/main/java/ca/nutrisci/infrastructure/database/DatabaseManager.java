package ca.nutrisci.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * DatabaseManager - Simple database connection management
 * Supports H2, MySQL, PostgreSQL, SQLite databases
 * Simplified for university-level project (KISS principle)
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Properties dbConfig;
    private String databaseType;
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClass;
    
    private DatabaseManager() {
        loadConfiguration();
        initializeDatabase();
    }
    
    /**
     * Get singleton instance
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load database configuration - simplified
     */
    private void loadConfiguration() {
        dbConfig = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database/database.properties")) {
            if (input != null) {
                dbConfig.load(input);
                System.out.println("✅ Database configuration loaded");
            } else {
                System.out.println("⚠️  Using default H2 configuration");
                setDefaultConfiguration();
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading config: " + e.getMessage());
            setDefaultConfiguration();
        }
        
        // Set connection parameters
        databaseType = dbConfig.getProperty("database.type", "h2").toLowerCase();
        jdbcUrl = dbConfig.getProperty(databaseType + ".url");
        username = dbConfig.getProperty(databaseType + ".username", "");
        password = dbConfig.getProperty(databaseType + ".password", "");
        driverClass = dbConfig.getProperty(databaseType + ".driver");
        
        System.out.println("🔧 Database: " + databaseType.toUpperCase());
    }
    
    /**
     * Set default H2 configuration
     */
    private void setDefaultConfiguration() {
        databaseType = "h2";
        jdbcUrl = "jdbc:h2:file:./data/nutrisci_db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
        username = "sa";
        password = "";
        driverClass = "org.h2.Driver";
    }
    
    /**
     * Initialize database - simplified
     */
    private void initializeDatabase() {
        try {
            // Create data directory
            Files.createDirectories(Paths.get("data"));
            
            // Load JDBC driver
            Class.forName(driverClass);
            
            // Create tables
            createTables();
            
            System.out.println("✅ Database initialized");
            
        } catch (Exception e) {
            System.err.println("❌ Database init error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create tables - simplified approach
     */
    private void createTables() {
        // Force manual table creation for now to ensure tables are created
        System.out.println("📄 Creating tables manually (forced)");
        createTablesManually();
        return;
        
        /*
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("database/schema.sql")) {
            if (schemaStream == null) {
                System.out.println("📄 Creating tables manually");
                createTablesManually();
                return;
            }
            
            String schemaSQL = new String(schemaStream.readAllBytes());
            String[] statements = schemaSQL.split(";");
            
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty() && sql.toUpperCase().startsWith("CREATE")) {
                        try {
                            stmt.execute(sql);
                            System.out.println("✅ Created table/index");
                        } catch (SQLException e) {
                            // Ignore if table already exists
                            if (!e.getMessage().contains("already exists")) {
                                System.err.println("⚠️  SQL warning: " + e.getMessage());
                            }
                        }
                    }
                }
                
                System.out.println("✅ Database schema ready");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Schema error: " + e.getMessage());
            createTablesManually();
        }
        */
    }
    
    /**
     * Create tables manually if schema file fails
     */
    private void createTablesManually() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Profiles table
            stmt.execute("CREATE TABLE IF NOT EXISTS profiles (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "age INTEGER NOT NULL, " +
                "sex VARCHAR(10) NOT NULL, " +
                "weight DOUBLE NOT NULL, " +
                "height DOUBLE NOT NULL, " +
                "is_active BOOLEAN DEFAULT FALSE, " +
                "units VARCHAR(10) DEFAULT 'metric', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Meals table  
            stmt.execute("CREATE TABLE IF NOT EXISTS meals (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "profile_id VARCHAR(36) NOT NULL, " +
                "date DATE NOT NULL, " +
                "meal_type VARCHAR(20) NOT NULL, " +
                "ingredients_json TEXT, " +
                "ingredients TEXT, " +
                "quantities TEXT, " +
                "calories DOUBLE DEFAULT 0, " +
                "protein DOUBLE DEFAULT 0, " +
                "carbs DOUBLE DEFAULT 0, " +
                "fat DOUBLE DEFAULT 0, " +
                "fiber DOUBLE DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Add ingredients_json column to existing meals table if it doesn't exist
            try {
                stmt.execute("ALTER TABLE meals ADD COLUMN ingredients_json TEXT");
                System.out.println("✅ Added ingredients_json column to existing meals table");
            } catch (SQLException e) {
                // Column probably already exists, ignore
                if (!e.getMessage().toLowerCase().contains("already exists") && 
                    !e.getMessage().toLowerCase().contains("duplicate column")) {
                    System.err.println("⚠️ Could not add ingredients_json column: " + e.getMessage());
                }
            }
            
            System.out.println("✅ Manual table creation completed");
            
        } catch (SQLException e) {
            System.err.println("❌ Manual table creation failed: " + e.getMessage());
        }
    }
    
    /**
     * Get a new database connection - simplified (no pooling)
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
    
    /**
     * Get database type
     */
    public String getDatabaseType() {
        return databaseType;
    }
    
    /**
     * Check if database is available
     */
    public boolean isAvailable() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
} 