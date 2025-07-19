package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.presentation.controllers.ProfileController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * ProfilePanel - The UI screen for managing user profiles.
 * Part of the Presentation Layer - View
 */
public class ProfilePanel extends JPanel {

    private ProfileController controller;
    private JList<ProfileDTO> profileList;
    private DefaultListModel<ProfileDTO> listModel;
    private JButton createButton, setActiveButton, deleteButton;
    private JLabel activeProfileLabel;

    public ProfilePanel(ProfileController controller) {
        this.controller = controller;
        initComponents();
        loadProfiles();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Profile List Panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(new TitledBorder("All Profiles"));
        
        listModel = new DefaultListModel<>();
        profileList = new JList<>(listModel);
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setCellRenderer(new ProfileListCellRenderer());
        listPanel.add(new JScrollPane(profileList), BorderLayout.CENTER);
        
        // Active Profile Label
        activeProfileLabel = new JLabel("Active Profile: None");
        activeProfileLabel.setFont(new Font("Arial", Font.BOLD, 14));
        listPanel.add(activeProfileLabel, BorderLayout.NORTH);

        add(listPanel, BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        createButton = new JButton("Create New Profile");
        setActiveButton = new JButton("Set as Active");
        deleteButton = new JButton("Delete Selected");
        actionPanel.add(createButton);
        actionPanel.add(setActiveButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        // Wire up event listeners
        createButton.addActionListener(e -> showCreateProfileDialog());
        setActiveButton.addActionListener(e -> setActiveProfile());
        deleteButton.addActionListener(e -> deleteSelectedProfile());
    }

    private void loadProfiles() {
        listModel.clear();
        List<ProfileDTO> profiles = controller.getAllProfiles();
        profiles.forEach(listModel::addElement);
        updateActiveProfileLabel();
    }
    
    private void updateActiveProfileLabel() {
        ProfileDTO activeProfile = controller.getActiveProfile();
        if (activeProfile != null) {
            activeProfileLabel.setText("Active Profile: " + activeProfile.getName());
            activeProfileLabel.setForeground(new Color(0, 128, 0)); // Dark green
        } else {
            activeProfileLabel.setText("Active Profile: None");
            activeProfileLabel.setForeground(Color.RED);
        }
    }

    private void showCreateProfileDialog() {
        // Simple dialog for creating a profile
        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField heightField = new JTextField();
        JComboBox<String> sexComboBox = new JComboBox<>(new String[]{"Male", "Female"});
        JComboBox<String> unitsComboBox = new JComboBox<>(new String[]{"Metric (kg, cm)", "Imperial (lbs, in)"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Sex:"));
        panel.add(sexComboBox);
        panel.add(new JLabel("Weight:"));
        panel.add(weightField);
        panel.add(new JLabel("Height:"));
        panel.add(heightField);
        panel.add(new JLabel("Units:"));
        panel.add(unitsComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New Profile",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                int age = Integer.parseInt(ageField.getText());
                String sex = (String) sexComboBox.getSelectedItem();
                double weight = Double.parseDouble(weightField.getText());
                double height = Double.parseDouble(heightField.getText());
                String units = unitsComboBox.getSelectedIndex() == 0 ? "metric" : "imperial";

                ProfileDTO newProfile = controller.createProfile(name, age, sex.toLowerCase(), weight, height, units);
                if (newProfile != null) {
                    JOptionPane.showMessageDialog(this, "Profile created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadProfiles();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create profile.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for age, weight, or height.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setActiveProfile() {
        ProfileDTO selectedProfile = profileList.getSelectedValue();
        if (selectedProfile != null) {
            controller.setActiveProfile(selectedProfile.getId());
            loadProfiles(); // Reload to update active status display
        } else {
            JOptionPane.showMessageDialog(this, "Please select a profile to set as active.", "No Profile Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedProfile() {
        ProfileDTO selectedProfile = profileList.getSelectedValue();
        if (selectedProfile != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the profile for " + selectedProfile.getName() + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                controller.deleteProfile(selectedProfile.getId());
                loadProfiles();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a profile to delete.", "No Profile Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Custom cell renderer to display ProfileDTO objects nicely in the JList.
     */
    private static class ProfileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ProfileDTO) {
                ProfileDTO profile = (ProfileDTO) value;
                String activeStatus = profile.isActive() ? " (Active)" : "";
                setText(String.format("%s, Age: %d, Sex: %s%s",
                        profile.getName(), profile.getAge(), profile.getSex(), activeStatus));
                if (profile.isActive()) {
                    setForeground(new Color(0, 100, 0)); // Dark green for active
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            }
            return this;
        }
    }
} 