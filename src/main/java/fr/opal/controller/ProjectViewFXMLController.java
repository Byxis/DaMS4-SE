package fr.opal.controller;

import fr.opal.facade.ProjectFacade;
import fr.opal.facade.AuthFacade;
import fr.opal.type.Project;
import fr.opal.type.User;
import fr.opal.type.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for project view
 */
public class ProjectViewFXMLController {
    
    @FXML
    private TextField projectNameField;
    @FXML
    private TextArea projectDescriptionField;
    @FXML
    private TableView<Project> projectsTable;
    @FXML
    private Button createProjectButton;
    @FXML
    private Button colorButton;
    @FXML
    private TextField searchField;
    
    private ProjectFacade projectFacade;
    private AuthFacade authFacade;
    private java.util.function.BiConsumer<Integer, String> onProjectCreated; // Callback with project ID and color
    private String selectedColor = "#0000FF"; // Default color (Blue)
    
    private String[] colors = {
        "#000000", "#FFFFFF", "#FF0000", "#FFFF00",
        "#FFA500", "#00AA00", "#0000FF", "#800080"
    };
    
    private List<Project> allProjects = new java.util.ArrayList<>();

    public ProjectViewFXMLController() {
        this.projectFacade = ProjectFacade.getInstance();
        this.authFacade = AuthFacade.getInstance();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadProjects();
        
        // Setup search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProjects(newVal));
        }
        
        // Initialize color button with default color
        if (colorButton != null) {
            colorButton.setStyle("-fx-padding: 10 20; -fx-background-color: " + selectedColor + "; -fx-text-fill: white; -fx-font-weight: bold;");
            colorButton.setText("🎨 Color :");
        }
    }

    @FXML
    public void showColorPicker() {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        
        VBox colorBox = new VBox(8);
        colorBox.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        // Create 8 color rectangles in a grid (2x4)
        for (int row = 0; row < 2; row++) {
            HBox rowBox = new HBox(8);
            rowBox.setStyle("-fx-padding: 5;");
            
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                String color = colors[index];
                Rectangle rect = new Rectangle(40, 40);
                rect.setFill(Color.web(color));
                rect.setStyle("-fx-cursor: hand; -fx-stroke: black; -fx-stroke-width: 1;");
                rect.setOnMouseClicked(e -> selectColorFromPicker(color, popup));
                rowBox.getChildren().add(rect);
            }
            
            colorBox.getChildren().add(rowBox);
        }
        
        popup.getContent().add(colorBox);
        
        // Position popup below the button
        Bounds buttonBounds = colorButton.localToScreen(colorButton.getBoundsInLocal());
        popup.show(colorButton.getScene().getWindow(), buttonBounds.getCenterX() - 100, buttonBounds.getCenterY() + 40);
    }
    
    private void selectColorFromPicker(String color, Popup popup) {
        selectedColor = color;
        popup.hide();
        
        // Update button to show selected color
        if (colorButton != null) {
            colorButton.setStyle("-fx-padding: 10 20; -fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
            colorButton.setText("🎨 Color :");
        }
    }

    /**
     * Setup the projects table with columns
     */
    private void setupTable() {
        if (projectsTable != null) {
            projectsTable.getColumns().clear();
            projectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            // Name column
            javafx.scene.control.TableColumn<Project, String> nameColumn = new javafx.scene.control.TableColumn<>("Project Name");
            nameColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            nameColumn.setPrefWidth(200);
            
            // Description column
            javafx.scene.control.TableColumn<Project, String> descriptionColumn = new javafx.scene.control.TableColumn<>("Description");
            descriptionColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
            descriptionColumn.setPrefWidth(300);
            
            projectsTable.getColumns().addAll(nameColumn, descriptionColumn);
        }
    }

    /**
     * Load projects from database
     */
    private void loadProjects() {
        projectFacade.loadProjectsFromDatabase();
        List<Project> projects = projectFacade.getLoadedProjects();
        
        if (projects != null) {
            allProjects = new java.util.ArrayList<>(projects);
        }
        
        displayProjects(allProjects);
    }
    
    private void filterProjects(String searchText) {
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
        if (projectsTable != null) {
            ObservableList<Project> observableProjects = FXCollections.observableArrayList(projects);
            projectsTable.setItems(observableProjects);
        }
    }

    /**
     * Create a new project
     */
    @FXML
    private void createProject() {
        String projectName = projectNameField.getText().trim();
        String projectDescription = projectDescriptionField != null ? projectDescriptionField.getText().trim() : "";
        
        if (projectName.isEmpty()) {
            showAlert("Error", "Project name cannot be empty");
            return;
        }
        
        try {
            // Get current user from session
            Session currentSession = authFacade.getCurrentSession();
            if (currentSession == null) {
                showAlert("Error", "You must be logged in to create a project");
                return;
            }
            
            // Create user object from session
            User owner = new User(currentSession.getUserId(), currentSession.getUsername(), "");
            
            Project newProject = projectFacade.createProject(projectName, projectDescription, owner);
            
            projectNameField.clear();
            if (projectDescriptionField != null) {
                projectDescriptionField.clear();
            }
            loadProjects(); // Refresh the table
            
            showAlert("Success", "Project '" + projectName + "' created successfully!");
            
            // Notify listener that project was created with ID and selected color
            if (onProjectCreated != null && newProject != null) {
                onProjectCreated.accept(newProject.getProjectId(), selectedColor);
            }
            
            // Close dialog
            closeDialog();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to create project: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) projectNameField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Set callback when project is created
     */
    public void setOnProjectCreated(java.util.function.BiConsumer<Integer, String> callback) {
        this.onProjectCreated = callback;
    }
}
