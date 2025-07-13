package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.domain.entities.Profile;
import ca.nutrisci.infrastructure.database.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JdbcProfileRepo - JDBC implementation of ProfileRepo
 * Works with any SQL database (MySQL, PostgreSQL, H2, SQLite)
 * Part of the Infrastructure Layer - Repository Pattern
 */
public class JdbcProfileRepo implements ProfileRepo {
    
    private final DatabaseManager dbManager;
    
    public JdbcProfileRepo() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @Override
    public Profile save(Profile profile) {
        String sql = "INSERT INTO profiles (id, name, age, sex, weight, height, is_active, units) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            UUID id = (profile.getId() == null) ? UUID.randomUUID() : profile.getId();
            
            stmt.setString(1, id.toString());
            stmt.setString(2, profile.getName());
            stmt.setInt(3, profile.getAge());
            stmt.setString(4, profile.getSex());
            stmt.setDouble(5, profile.getWeight());
            stmt.setDouble(6, profile.getHeight());
            stmt.setBoolean(7, profile.isActive());
            stmt.setString(8, profile.getUnits());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Profile saved to database: " + profile.getName());
                return new Profile(id, profile.getName(), profile.getAge(), profile.getSex(),
                                 profile.getWeight(), profile.getHeight(), profile.isActive(), 
                                 profile.getUnits(), LocalDateTime.now());
            } else {
                throw new RuntimeException("Failed to save profile to database");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error saving profile: " + e.getMessage());
            throw new RuntimeException("Database error while saving profile", e);
        }
    }
    
    @Override
    public Profile update(Profile profile) {
        String sql = "UPDATE profiles SET name = ?, age = ?, sex = ?, weight = ?, height = ?, is_active = ?, units = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profile.getName());
            stmt.setInt(2, profile.getAge());
            stmt.setString(3, profile.getSex());
            stmt.setDouble(4, profile.getWeight());
            stmt.setDouble(5, profile.getHeight());
            stmt.setBoolean(6, profile.isActive());
            stmt.setString(7, profile.getUnits());
            stmt.setString(8, profile.getId().toString());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Profile updated in database: " + profile.getName());
                return profile;
            } else {
                throw new RuntimeException("Profile not found for update: " + profile.getId());
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating profile: " + e.getMessage());
            throw new RuntimeException("Database error while updating profile", e);
        }
    }
    
    @Override
    public Profile findById(UUID id) {
        String sql = "SELECT * FROM profiles WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProfile(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding profile by ID: " + e.getMessage());
            throw new RuntimeException("Database error while finding profile", e);
        }
        
        return null;
    }
    
    @Override
    public List<Profile> findAll() {
        String sql = "SELECT * FROM profiles ORDER BY name";
        List<Profile> profiles = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                profiles.add(mapResultSetToProfile(rs));
            }
            
            System.out.println("📊 Loaded " + profiles.size() + " profiles from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding all profiles: " + e.getMessage());
            throw new RuntimeException("Database error while finding profiles", e);
        }
        
        return profiles;
    }
    
    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM profiles WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id.toString());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Profile deleted from database: " + id);
            } else {
                throw new RuntimeException("Profile not found for deletion: " + id);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting profile: " + e.getMessage());
            throw new RuntimeException("Database error while deleting profile", e);
        }
    }
    
    @Override
    public Profile findActiveProfile() {
        String sql = "SELECT * FROM profiles WHERE is_active = true LIMIT 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                Profile activeProfile = mapResultSetToProfile(rs);
                System.out.println("👤 Active profile found: " + activeProfile.getName());
                return activeProfile;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding active profile: " + e.getMessage());
            throw new RuntimeException("Database error while finding active profile", e);
        }
        
        System.out.println("👤 No active profile found");
        return null;
    }
    
    // Legacy method implementations
    @Override
    public void saveProfile(Profile profile) {
        save(profile);
    }
    
    @Override
    public void deleteProfile(UUID profileId) {
        delete(profileId);
    }
    
    @Override
    public List<Profile> listAllProfiles() {
        return findAll();
    }
    
    @Override
    public List<Profile> findByName(String name) {
        String sql = "SELECT * FROM profiles WHERE name LIKE ? ORDER BY name";
        List<Profile> profiles = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profiles.add(mapResultSetToProfile(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding profiles by name: " + e.getMessage());
            throw new RuntimeException("Database error while finding profiles by name", e);
        }
        
        return profiles;
    }
    
    @Override
    public boolean existsById(UUID profileId) {
        String sql = "SELECT 1 FROM profiles WHERE id = ? LIMIT 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profileId.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking profile existence: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void updateProfileSettings(UUID profileId, String units) {
        String sql = "UPDATE profiles SET units = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, units);
            stmt.setString(2, profileId.toString());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("✅ Profile settings updated: " + profileId);
            } else {
                throw new RuntimeException("Profile not found for settings update: " + profileId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating profile settings: " + e.getMessage());
            throw new RuntimeException("Database error while updating profile settings", e);
        }
    }
    
    @Override
    public void activateProfile(UUID profileId) {
        Connection conn = null;
        
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // First, deactivate all profiles using the same connection
            String deactivateSQL = "UPDATE profiles SET is_active = false";
            try (PreparedStatement deactivateStmt = conn.prepareStatement(deactivateSQL)) {
                deactivateStmt.executeUpdate();
                System.out.println("✅ All profiles deactivated");
            }
            
            // Then, activate the specified profile
            String activateSQL = "UPDATE profiles SET is_active = true WHERE id = ?";
            try (PreparedStatement activateStmt = conn.prepareStatement(activateSQL)) {
                activateStmt.setString(1, profileId.toString());
                
                int rows = activateStmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    throw new RuntimeException("Profile not found: " + profileId);
                }
            }
            
            conn.commit();
            System.out.println("✅ Active profile set: " + profileId);
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            System.err.println("❌ Error setting active profile: " + e.getMessage());
            throw new RuntimeException("Database error while setting active profile", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    System.err.println("❌ Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    public void deactivateAllProfiles() {
        String sql = "UPDATE profiles SET is_active = false";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.executeUpdate();
            System.out.println("✅ All profiles deactivated");
            
        } catch (SQLException e) {
            System.err.println("❌ Error deactivating all profiles: " + e.getMessage());
            throw new RuntimeException("Database error while deactivating profiles", e);
        }
    }
    
    /**
     * Map ResultSet to Profile entity
     */
    private Profile mapResultSetToProfile(ResultSet rs) throws SQLException {
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTimestamp != null) ? createdTimestamp.toLocalDateTime() : LocalDateTime.now();
        
        return new Profile(
            UUID.fromString(rs.getString("id")),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("sex"),
            rs.getDouble("weight"),
            rs.getDouble("height"),
            rs.getBoolean("is_active"),
            rs.getString("units"),
            createdAt
        );
    }
    
    /**
     * Get repository status
     */
    public String getStatus() {
        try {
            int profileCount = findAll().size();
            Profile activeProfile = findActiveProfile();
            String activeName = (activeProfile != null) ? activeProfile.getName() : "None";
            
            return String.format("JdbcProfileRepo - Profiles: %d, Active: %s, Database: %s", 
                               profileCount, activeName, dbManager.getDatabaseType().toUpperCase());
        } catch (Exception e) {
            return "JdbcProfileRepo - Error: " + e.getMessage();
        }
    }
} 