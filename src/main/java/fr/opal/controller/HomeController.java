package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.facade.ProjectFacade;
import fr.opal.service.SceneManager;
import fr.opal.type.Profile;
import fr.opal.type.Session;
import fr.opal.type.Project;
import fr.opal.type.ProjectInvitation;
import fr.opal.util.ColorUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Controller for the home screen
 */
public class HomeController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button userProfileBtn;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button settingsBtn;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button createProjectBtn;
    @FXML
    private Button invitationsBtn;
    @FXML
    private VBox projectsContainer;
    @FXML
    private TextField homeSearchField;

    private AuthFacade authFacade;
    private SessionPropertiesFacade sessionPropertiesFacade;
    private ProjectFacade projectFacade;
    private SceneManager sceneManager;
    private List<Project> allProjects = new java.util.ArrayList<>();
    private Session currentSession;
    private Profile currentProfile;
    private java.util.Map<Integer, String> projectColors = new java.util.HashMap<>(); // Store project colors

    public HomeController() {
        this.authFacade = AuthFacade.getInstance();
        this.sessionPropertiesFacade = SessionPropertiesFacade.getInstance();
        this.projectFacade = ProjectFacade.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    public void initialize() {
        currentSession = authFacade.getCurrentSession();
        if (currentSession != null) {
            usernameLabel.setText(currentSession.getUsername());
            currentProfile = authFacade.getProfile(currentSession.getUserId());
            
            // Load and apply user session settings
            sessionPropertiesFacade.loadSettings(currentSession.getUserId());
            
            // Apply theme after a short delay to ensure scene is ready
            javafx.application.Platform.runLater(this::applyCurrentTheme);
            
            String welcomeText = "Welcome, " + currentSession.getUsername() + "!";
            if (currentProfile != null && currentProfile.getDisplayName() != null && !currentProfile.getDisplayName().isEmpty()) {
                String firstName = currentProfile.getDisplayName();
                String lastName = currentProfile.getBio() != null ? currentProfile.getBio() : "";
                if (!lastName.isEmpty()) {
                    welcomeText = "Welcome, " + firstName + " " + lastName + "!";
                } else {
                    welcomeText = "Welcome, " + firstName + "!";
                }
            }
            welcomeLabel.setText(welcomeText);
        } else {
            redirectToLogin();
        }
        
        // Load and display projects (with error handling)
        try {
            loadAndDisplayProjects();
            updateInvitationCount();
        } catch (Exception e) {
            System.err.println("Error loading projects: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Setup search field listener
        if (homeSearchField != null) {
            homeSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayProjects(newVal));
        }
    }

    /**
     * Apply current theme to the root pane
     */
    private void applyCurrentTheme() {
        if (rootPane != null) {
            sessionPropertiesFacade.applyTheme(rootPane);
            
            // Style navbar buttons with accent color and appropriate text color
            String accentColor = sessionPropertiesFacade.getSettings().getAccentColor().getHexCode();
            String textColor = ColorUtil.getContrastTextColor(accentColor);
            
            String buttonStyle = "-fx-background-color: " + accentColor + "; " +
                                "-fx-text-fill: " + textColor + "; " +
                                "-fx-font-weight: bold;";
            
            if (userProfileBtn != null) {
                userProfileBtn.setStyle(buttonStyle);
            }
            if (settingsBtn != null) {
                settingsBtn.setStyle(buttonStyle);
            }
            if (logoutBtn != null) {
                logoutBtn.setStyle(buttonStyle);
            }
        }
    }

    /**
     * Opens a dialog to view and edit user profile
     */
    @FXML
    private void openProfileDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/profile-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Add stylesheet and apply theme
            dialogPane.getStylesheets().add(getClass().getResource("/fr/opal/style.css").toExternalForm());
            sessionPropertiesFacade.applyTheme(dialogPane);

            TextField usernameField = (TextField) dialogPane.lookup("#usernameField");
            TextField emailField = (TextField) dialogPane.lookup("#emailField");
            TextField firstNameField = (TextField) dialogPane.lookup("#firstNameField");
            TextField lastNameField = (TextField) dialogPane.lookup("#lastNameField");

            usernameField.setText(currentSession.getUsername());
            if (currentProfile != null) {
                emailField.setText(currentProfile.getContactInfo());
                firstNameField.setText(currentProfile.getDisplayName());
                lastNameField.setText(currentProfile.getBio());
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Opal - Profile");

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    saveProfileChanges(
                            firstNameField.getText(),
                            lastNameField.getText(),
                            emailField.getText()
                    );
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves profile changes
     */
    private void saveProfileChanges(String firstName, String lastName, String email) {
        try {
            if (currentSession != null) {
                Profile profile = new Profile(currentSession.getUserId(), firstName, lastName, email);
                authFacade.updateProfile(currentSession.getUserId(), profile);
                currentProfile = profile;
                refreshWelcomeMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the welcome message with updated profile data
     */
    private void refreshWelcomeMessage() {
        String welcomeText = "Welcome, " + currentSession.getUsername() + "!";
        if (currentProfile != null && currentProfile.getDisplayName() != null && !currentProfile.getDisplayName().isEmpty()) {
            String firstName = currentProfile.getDisplayName();
            String lastName = currentProfile.getBio() != null ? currentProfile.getBio() : "";
            if (!lastName.isEmpty()) {
                welcomeText = "Welcome, " + firstName + " " + lastName + "!";
            } else {
                welcomeText = "Welcome, " + firstName + "!";
            }
        }
        welcomeLabel.setText(welcomeText);
    }

    /**
     * Handles user logout
     */
    @FXML
    private void logout() {
        if (currentSession != null) {
            authFacade.logout(currentSession.getId());
            sessionPropertiesFacade.clearSettings();
        }
        redirectToLogin();
    }

    /**
     * Opens the session settings dialog
     */
    @FXML
    private void openSettings() {
        try {
            // FORCE reload settings from database before opening dialog
            if (currentSession != null) {
                sessionPropertiesFacade.loadSettings(currentSession.getUserId());
            }
            
            // Create controller BEFORE loading FXML
            SessionPropertiesController settingsController = new SessionPropertiesController();
            
            // Create FXMLLoader and set controller manually
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/session-properties-view.fxml"));
            loader.setController(settingsController);
            Parent settingsContent = loader.load();
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Appearance Settings");
            
            // Add stylesheet first
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/fr/opal/style.css").toExternalForm());
            
            // Apply theme to dialog first
            sessionPropertiesFacade.applyTheme(dialog.getDialogPane());
            
            // Set content after theme is applied
            dialog.getDialogPane().setContent(settingsContent);
            sessionPropertiesFacade.applyTheme(settingsContent);
            
            // Add button
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Function to update close button color
            Runnable updateCloseButtonColor = () -> {
                String accentColor = sessionPropertiesFacade.getSettings().getAccentColor().getHexCode();
                String textColor = ColorUtil.getContrastTextColor(accentColor);
                javafx.application.Platform.runLater(() -> {
                    // Find all buttons in the DialogPane (using CSS selector)
                    dialog.getDialogPane().lookupAll(".button").forEach(node -> {
                        if (node instanceof javafx.scene.control.Button) {
                            javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
                            // Check if this is not a color button (only style dialog buttons)
                            if (!btn.getStyleClass().contains("color-button")) {
                                btn.setStyle(
                                    "-fx-background-color: " + accentColor + "; " +
                                    "-fx-text-fill: " + textColor + "; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-padding: 8px 16px;"
                                );
                            }
                        }
                    });
                });
            };
            
            // Initialize the controller AFTER loading the FXML
            settingsController.initialize();
            
            // Update close button color initially
            updateCloseButtonColor.run();
            
            // Update close button color when settings change
            settingsController.setOnSettingsChanged(() -> {
                applyCurrentTheme();
                updateCloseButtonColor.run();
            });

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Redirects to login screen
     */
    private void redirectToLogin() {
        try {
            AuthController.clearMessageLabel();
            sceneManager.switchTo("/fr/opal/login-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load and display projects
     */
    private void loadAndDisplayProjects() {
        try {
            // First load all projects from database to populate cache
            projectFacade.loadProjectsFromDatabase();
            
            // Then filter by current user (owner or collaborator)
            List<Project> projects = projectFacade.getProjectsByOwner(currentSession.getUsername());
            List<Project> collaboratorProjects = projectFacade.getProjectsForCollaborator(currentSession.getUsername());
            
            // Merge both lists and remove duplicates
            projects.addAll(collaboratorProjects.stream()
                    .filter(p -> !projects.contains(p))
                    .toList());
            
            if (projects != null) {
                allProjects = new java.util.ArrayList<>(projects);
            }
            
            displayProjects(allProjects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void filterAndDisplayProjects(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayProjects(allProjects);
        } else {
            List<Project> filtered = allProjects.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
            displayProjects(filtered);
        }
    }
    
    private void displayProjects(List<Project> projects) {
        if (projectsContainer != null) {
            projectsContainer.getChildren().clear();
            
            if (projects != null && !projects.isEmpty()) {
                for (Project project : projects) {
                    HBox projectCard = createProjectCard(project);
                    projectsContainer.getChildren().add(projectCard);
                }
            } else {
                Label noProjectsLabel = new Label("No projects yet. Create one to get started!");
                noProjectsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");
                projectsContainer.getChildren().add(noProjectsLabel);
            }
        }
    }

    /**
     * Create a project card with name, color rectangle and delete button
     */
    private HBox createProjectCard(Project project) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-alignment: CENTER_LEFT;");
        
        // Color rectangle - use stored color or random
        Rectangle colorRect = new Rectangle(50, 50);
        String color = projectColors.getOrDefault(project.getProjectId(), getRandomHexColor());
        colorRect.setStyle("-fx-fill: " + color + ";");
        
        // VBox for project name and description
        javafx.scene.layout.VBox projectInfoBox = new javafx.scene.layout.VBox(3);
        
        // Project name
        Label projectName = new Label(project.getName());
        projectName.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        
        // Project description (small text)
        Label projectDescription = new Label(project.getDescription() != null ? project.getDescription() : "");
        projectDescription.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");
        projectDescription.setWrapText(true);
        projectDescription.setMaxWidth(250);
        
        projectInfoBox.getChildren().addAll(projectName, projectDescription);
        
        // Spacer to push buttons to the right
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Check if current user is the owner
        // Try getting owner from project object, or use ownerId to check
        String ownerUsername = null;
        if (project.getOwner() != null && project.getOwner().getUsername() != null) {
            ownerUsername = project.getOwner().getUsername();
        }
        boolean isOwner = ownerUsername != null && ownerUsername.equals(currentSession.getUsername());
        
        HBox buttonsBox = new HBox(10);
        
        // Only show Invite and Delete buttons if user is the owner
        if (isOwner) {
            // Invite button
            Button inviteBtn = new Button("👥 Invite");
            inviteBtn.setStyle("-fx-font-size: 12; -fx-padding: 5 10;");
            inviteBtn.setOnAction(e -> showInviteDialog(project));
            
            // Delete button
            Button deleteBtn = new Button("🗑️ Delete");
            deleteBtn.setStyle("-fx-font-size: 12; -fx-padding: 5 10;");
            deleteBtn.setOnAction(e -> {
                // Confirm deletion
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete the project '" + project.getName() + "' ?");
                
                if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
                    try {
                        projectFacade.deleteProject(project.getProjectId());
                        projectColors.remove(project.getProjectId());
                        loadAndDisplayProjects();
                    } catch (Exception ex) {
                        javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Error deleting project: " + ex.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            });
            
            buttonsBox.getChildren().addAll(inviteBtn, deleteBtn);
        } else {
            // Show "Shared with you" label for collaborators
            Label sharedLabel = new Label("(Shared with you)");
            sharedLabel.setStyle("-fx-font-size: 11; -fx-text-fill: gray;");
            buttonsBox.getChildren().add(sharedLabel);
        }
        
        card.getChildren().addAll(colorRect, projectInfoBox, spacer, buttonsBox);
        return card;
    }

    /**
     * Get a random hex color
     */
    private String getRandomHexColor() {
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"};
        return colors[(int) (Math.random() * colors.length)];
    }

    /**
     * Get a random color
     */
    private Color getRandomColor() {
        String hex = getRandomHexColor();
        return Color.web(hex);
    }

    /**
     * Opens the project management view to create a new project
     */
    @FXML
    private void createNewProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/project-view.fxml"));
            Parent projectView = loader.load();
            
            // Get the controller and set callback
            ProjectViewFXMLController controller = loader.getController();
            controller.setOnProjectCreated((projectId, color) -> {
                projectColors.put(projectId, color);
                loadAndDisplayProjects();
            });
            
            // Apply stylesheet and theme
            projectView.getStylesheets().add(getClass().getResource("/fr/opal/project-style.css").toExternalForm());
            projectView.getStylesheets().add(getClass().getResource("/fr/opal/style.css").toExternalForm());
            sessionPropertiesFacade.applyTheme(projectView);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Opal - Project Management");
            dialog.getDialogPane().setContent(projectView);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Show dialog to invite users to a project
     */
    private void showInviteDialog(Project project) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Invite User to Project");
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        Label headerLabel = new Label("Invite users to " + project.getName());
        headerLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username to invite...");
        
        // Message label - will show between field and buttons
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 12; -fx-wrap-text: true;");
        messageLabel.setVisible(false);
        
        // Send button - does NOT close the stage
        javafx.scene.control.Button sendButton = new javafx.scene.control.Button("Send");
        sendButton.setPrefWidth(80);
        sendButton.setStyle("-fx-font-size: 12;");
        
        // Close button - closes the stage
        javafx.scene.control.Button closeButton = new javafx.scene.control.Button("Close");
        closeButton.setPrefWidth(80);
        closeButton.setStyle("-fx-font-size: 12;");
        closeButton.setOnAction(e -> stage.close());
        
        sendButton.setOnAction(e -> {
            String invitedUsername = usernameField.getText().trim();
            
            if (invitedUsername.isEmpty()) {
                messageLabel.setText("Username cannot be empty");
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
                messageLabel.setVisible(true);
                return;
            }
            
            // Check if user exists
            if (!authFacade.userExists(invitedUsername)) {
                messageLabel.setText("\"" + invitedUsername + "\" doesn't exist");
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
                messageLabel.setVisible(true);
            } else {
                try {
                    // Send the invitation
                    projectFacade.inviteUser(project.getProjectId(), invitedUsername, currentSession.getUsername(), fr.opal.type.EPermission.CONTRIBUTOR);
                    
                    // Show success message and clear field
                    messageLabel.setText("✓ Invitation sent to " + invitedUsername);
                    messageLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
                    messageLabel.setVisible(true);
                    usernameField.clear();
                } catch (Exception ex) {
                    messageLabel.setText("Error sending invitation: " + ex.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
                    messageLabel.setVisible(true);
                    ex.printStackTrace();
                }
            }
        });
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");
        HBox inputBox = new HBox(10, usernameLabel, usernameField);
        inputBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox buttonBox = new HBox(10, sendButton, closeButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        content.getChildren().addAll(headerLabel, inputBox, messageLabel, buttonBox);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(content, 1000, 250);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Open invitations view
     */
    @FXML
    private void openInvitations() {
        try {
            // Get pending invitations for current user
            List<ProjectInvitation> pendingInvitations = projectFacade.getPendingInvitations(currentSession.getUsername());
            
            // Create a new stage for invitations
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Your Invitations");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 15;");
            
            Label headerLabel = new Label("Pending Invitations (" + pendingInvitations.size() + ")");
            headerLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
            
            VBox invitationsContainer = new VBox(10);
            
            if (pendingInvitations.isEmpty()) {
                Label noInvitationsLabel = new Label("No pending invitations");
                noInvitationsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: gray;");
                invitationsContainer.getChildren().add(noInvitationsLabel);
            } else {
                // For each invitation, create a card
                for (ProjectInvitation invitation : pendingInvitations) {
                    Project project = projectFacade.findProject(invitation.getProjectId());
                    
                    HBox invitationCard = new HBox(15);
                    invitationCard.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-color: #f9f9f9;");
                    
                    VBox projectInfo = new VBox(5);
                    Label projectNameLabel = new Label(project != null ? project.getName() : "Unknown Project");
                    projectNameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
                    Label inviterLabel = new Label("From: " + invitation.getInviterUsername());
                    inviterLabel.setStyle("-fx-font-size: 11; -fx-text-fill: gray;");
                    projectInfo.getChildren().addAll(projectNameLabel, inviterLabel);
                    
                    HBox buttonsBox = new HBox(10);
                    buttonsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    
                    javafx.scene.control.Button acceptBtn = new javafx.scene.control.Button("✓ Accept");
                    acceptBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #4CAF50; -fx-text-fill: white;");
                    
                    javafx.scene.control.Button declineBtn = new javafx.scene.control.Button("✗ Decline");
                    declineBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #f44336; -fx-text-fill: white;");
                    
                    acceptBtn.setOnAction(e -> {
                        try {
                            projectFacade.acceptInvitation(invitation.getInvitationId());
                            invitationsContainer.getChildren().remove(invitationCard);
                            
                            // Refresh project list
                            loadAndDisplayProjects();
                            
                            // Update button count
                            updateInvitationCount();
                            
                            if (invitationsContainer.getChildren().isEmpty()) {
                                invitationsContainer.getChildren().clear();
                                Label noInvitationsLabel = new Label("No pending invitations");
                                noInvitationsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: gray;");
                                invitationsContainer.getChildren().add(noInvitationsLabel);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                    
                    declineBtn.setOnAction(e -> {
                        try {
                            projectFacade.declineInvitation(invitation.getInvitationId());
                            invitationsContainer.getChildren().remove(invitationCard);
                            
                            // Update button count
                            updateInvitationCount();
                            
                            if (invitationsContainer.getChildren().isEmpty()) {
                                invitationsContainer.getChildren().clear();
                                Label noInvitationsLabel = new Label("No pending invitations");
                                noInvitationsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: gray;");
                                invitationsContainer.getChildren().add(noInvitationsLabel);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                    
                    buttonsBox.getChildren().addAll(acceptBtn, declineBtn);
                    invitationCard.getChildren().addAll(projectInfo, buttonsBox);
                    HBox.setHgrow(projectInfo, javafx.scene.layout.Priority.ALWAYS);
                    
                    invitationsContainer.getChildren().add(invitationCard);
                }
            }
            
            ScrollPane scrollPane = new ScrollPane(invitationsContainer);
            scrollPane.setFitToWidth(true);
            
            javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Close");
            closeBtn.setPrefWidth(100);
            closeBtn.setStyle("-fx-font-size: 12;");
            closeBtn.setOnAction(e -> stage.close());
            
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonBox.getChildren().add(closeBtn);
            
            content.getChildren().addAll(headerLabel, scrollPane, buttonBox);
            VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(content, 600, 400);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update invitation button count
     */
    private void updateInvitationCount() {
        try {
            List<ProjectInvitation> pendingInvitations = projectFacade.getPendingInvitations(currentSession.getUsername());
            invitationsBtn.setText("📬 Invitations (" + pendingInvitations.size() + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
