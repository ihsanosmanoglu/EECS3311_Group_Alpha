package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.domain.entities.Profile;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FileProfileRepo - File-based implementation for ProfileRepo
 * Part of the Infrastructure Layer
 * This repository persists profile data to a CSV file.
 */
public class FileProfileRepo implements ProfileRepo {
    
    private final String filePath;
    private final Map<UUID, Profile> profiles = new LinkedHashMap<>();
    private static final String[] HEADERS = {"id", "name", "age", "sex", "weight", "height", "isActive", "units", "createdAt"};

    public FileProfileRepo(String filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            // If the file doesn't exist, create it with headers
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                saveAll(); // This will write the headers
            } catch (IOException e) {
                e.printStackTrace(); // Simple error handling for student project
            }
            return;
        }

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            // Skip header row
            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                Profile profile = new Profile(
                    UUID.fromString(row[0]),
                    row[1],
                    Integer.parseInt(row[2]),
                    row[3],
                    Double.parseDouble(row[4]),
                    Double.parseDouble(row[5]),
                    Boolean.parseBoolean(row[6]),
                    row[7],
                    LocalDateTime.parse(row[8])
                );
                profiles.put(profile.getId(), profile);
            }
        } catch (IOException | CsvException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private synchronized void saveAll() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(HEADERS);
            for (Profile profile : profiles.values()) {
                writer.writeNext(new String[]{
                    profile.getId().toString(),
                    profile.getName(),
                    String.valueOf(profile.getAge()),
                    profile.getSex(),
                    String.valueOf(profile.getWeight()),
                    String.valueOf(profile.getHeight()),
                    String.valueOf(profile.isActive()),
                    profile.getUnits(),
                    profile.getCreatedAt().toString()
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Profile save(Profile profile) {
        if (profile.getId() == null) {
            profile.setId(UUID.randomUUID());
        }
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }
        profiles.put(profile.getId(), profile);
        saveAll();
        return profile;
    }

    @Override
    public Profile findById(UUID profileId) {
        return profiles.get(profileId);
    }

    @Override
    public void delete(UUID profileId) {
        profiles.remove(profileId);
        saveAll();
    }

    @Override
    public List<Profile> findAll() {
        return new ArrayList<>(profiles.values());
    }

    @Override
    public Profile update(Profile profile) {
        if (profile.getId() == null || !profiles.containsKey(profile.getId())) {
            throw new IllegalArgumentException("Profile not found for update");
        }
        profiles.put(profile.getId(), profile);
        saveAll();
        return profile;
    }

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
    public Profile findActiveProfile() {
        return profiles.values().stream()
                .filter(Profile::isActive)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Profile> findByName(String name) {
        return profiles.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID profileId) {
        return profiles.containsKey(profileId);
    }

    @Override
    public void updateProfileSettings(UUID profileId, String units) {
        Profile profile = findById(profileId);
        if (profile != null) {
            profile.setUnits(units);
            update(profile);
        }
    }

    @Override
    public void activateProfile(UUID profileId) {
        Profile profile = findById(profileId);
        if (profile != null) {
            profile.setActive(true);
            update(profile);
        }
    }

    @Override
    public void deactivateAllProfiles() {
        profiles.values().forEach(p -> p.setActive(false));
        saveAll();
    }
} 