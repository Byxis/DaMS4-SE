package fr.opal.controller;

import fr.opal.type.*;
import fr.opal.service.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import fr.opal.service.EntryManager;
import fr.opal.service.AuthManager;
import fr.opal.service.IEntryImporter;
import fr.opal.service.IEntryExporter;
import fr.opal.service.EntryJsonImporter;
import fr.opal.service.EntryXmlImporter;
import fr.opal.service.EntryJsonExporter;
import fr.opal.service.EntryXmlExporter;
import fr.opal.facade.AuthFacade;
import fr.opal.facade.EntryFacade;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for entry management UI
 */
public class EntryController {
    // Navbar fields
    @FXML
    private Button userProfileBtn;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button homeBtn;
    @FXML
    private Button logoutBtn;

    @FXML
    private TextArea entryContent;
    @FXML
    private Label projectTitle;
    @FXML
    private Button importBtn;
    @FXML
    private Button exportBtn;
    @FXML
    private Button projectRootRedirBtn;
    @FXML
    private Button entryParentRedirBtn;
    @FXML
    private Button subDirBtn;

    // Additional UI components for entry management
    @FXML
    private ListView<Comment> commentsList;
    @FXML
    private TextArea commentInput;
    @FXML
    private Button addCommentBtn;
    @FXML
    private Button saveEntryBtn;
    @FXML
    private Button createEntryBtn;
    @FXML
    private Button deleteEntryBtn;
    @FXML
    private TextField entryTitleField;
    @FXML
    private ListView<UserPermission> usersPermissionsList;
    @FXML
    private TextField usernameField;
    @FXML
    private ComboBox<EPermission> permissionComboBox;
    @FXML
    private Button addPermissionBtn;

    private EntryManager entryManager;
    private AuthManager authManager;
    private AuthFacade authFacade;
    private SceneManager sceneManager;
    private EntryFacade facade;
    private User currentUser;
    private Session currentSession;
    private Profile currentProfile;

    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        // Initialize services
        authManager = AuthManager.getInstance();
        authFacade = AuthFacade.getInstance();
        sceneManager = SceneManager.getInstance();
        facade = EntryFacade.getInstance();
        
        // Initialize navbar with current session
        initializeNavbar();
        
        if (authManager.isAuthenticated()) {
            currentUser = authManager.getConnectedUser();
            entryManager = new EntryManager(currentUser);
            
            try {
                // Get the Facade to handle database initialization
                // (facade field is already initialized above)
                
                // Ensure placeholder entry structure exists in database (idempotent)
                // This creates it if it doesn't exist, but we DON'T use the returned ID for anything
                facade.ensurePlaceholderEntryExists("lez");
                
                // Load the root entry from DATABASE with Depth-1 context
                EntryContextDTO rootContext = facade.loadPlaceholderRootFromDatabase();
                
                if (rootContext != null) {
                    Entry rootEntry = rootContext.getTargetEntry();
                    // Set current entry to the database-loaded entry
                    entryManager.setCurrentEntry(rootEntry);
                    displayEntry(rootEntry);
                    
                    // Update navigation buttons based on context
                    entryParentRedirBtn.setDisable(rootContext.isRoot());
                    subDirBtn.setDisable(!rootContext.hasChildren());
                } else {
                    showErrorDialog("Error", "Failed to load placeholder entry from database");
                }
            } catch (Exception e) {
                showErrorDialog("Error", "Failed to initialize entry system: " + e.getMessage());
            }
            
            // Initialize permission ComboBox
            permissionComboBox.getItems().addAll(EPermission.READER, EPermission.COMMENTOR, EPermission.EDITOR);
            permissionComboBox.setValue(EPermission.READER);
            
            // Add hover listener to sub directories button
            subDirBtn.setOnMouseEntered(e -> showSubDirectoriesOnHover());
        }
    }

    /**
     * Initializes the navbar with user information
     */
    private void initializeNavbar() {
        currentSession = authFacade.getCurrentSession();
        if (currentSession != null) {
            usernameLabel.setText(currentSession.getUsername());
            currentProfile = authFacade.getProfile(currentSession.getUserId());
        }
    }

    /**
     * Opens profile dialog (similar to HomeController)
     */
    @FXML
    private void openProfileDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/opal/profile-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            TextField profileUsernameField = (TextField) dialogPane.lookup("#usernameField");
            TextField emailField = (TextField) dialogPane.lookup("#emailField");
            TextField firstNameField = (TextField) dialogPane.lookup("#firstNameField");
            TextField lastNameField = (TextField) dialogPane.lookup("#lastNameField");

            profileUsernameField.setText(currentSession.getUsername());
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles logout
     */
    @FXML
    private void logout() {
        if (currentSession != null) {
            authFacade.logout(currentSession.getId());
        }
        try {
            AuthController.clearMessageLabel();
            sceneManager.switchTo("/fr/opal/login-view.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates back to home
     */
    @FXML
    private void onNavigateHome() {
        try {
            sceneManager.switchTo("/fr/opal/home.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles showing subdirectories in a popup menu on hover
     */
    public void showSubDirectoriesOnHover() {
        Entry current = entryManager.getCurrentEntry();
        if (current == null) {
            return;
        }
        
        // Reload current entry with context to get fresh children data
        try {
            EntryContextDTO context = facade.loadEntry(current.getId());
            if (context == null || !context.hasChildren()) {
                return;
            }
            
            ContextMenu contextMenu = new ContextMenu();
            for (Entry child : context.getChildEntries()) {
                MenuItem menuItem = new MenuItem(child.getTitle());
                menuItem.setOnAction(e -> {
                    // Reload child from database with full Depth-1 context
                    loadAndDisplayEntry(child.getId());
                });
                contextMenu.getItems().add(menuItem);
            }
            
            contextMenu.show(subDirBtn, javafx.geometry.Side.BOTTOM, 0, 0);
        } catch (Exception e) {
            showErrorDialog("Error loading children", e.getMessage());
        }
    }

    /**
     * Handles showing subdirectories in a popup menu
     */
    @FXML
    public void onShowSubDirectories() {
        showSubDirectoriesOnHover();
    }

    /**
     * Loads an entry by ID with Depth-1 context and displays it
     * Provides validation context for navigation (parent and children)
     */
    private void loadAndDisplayEntry(int entryId) {
        try {
            EntryContextDTO context = facade.loadEntry(entryId);
            if (context != null) {
                Entry targetEntry = context.getTargetEntry();
                
                // Check if user has access to this entry
                if (!targetEntry.canUserAccess(currentUser)) {
                    showErrorDialog("Access Denied", "You do not have permission to view this entry.");
                    return;
                }
                
                // Display the entry with context loaded for navigation validation
                displayEntry(targetEntry, context);
            }
        } catch (Exception e) {
            showErrorDialog("Error loading entry", e.getMessage());
        }
    }

    /**
     * Displays an entry's content
     */
    private void displayEntry(Entry entry) {
        displayEntry(entry, null);
    }

    /**
     * Displays an entry's content with optional navigation context
     */
    private void displayEntry(Entry entry, EntryContextDTO context) {
        try {
            // Check if user has access to this entry using "deny by default" logic
            if (!entry.canUserAccess(currentUser)) {
                showErrorDialog("Access Denied", "You do not have permission to view this entry.");
                return;
            }
            
            entryManager.setCurrentEntry(entry);
            entryTitleField.setText(entry.getTitle());
            entryContent.setText(entry.getContent());
            projectTitle.setText(entry.getRootEntry().getTitle());
            displayComments(entry);
            displayUsersWithPermissions(entry);
            
            // Update navigation buttons based on context if provided, otherwise use in-memory state
            if (context != null) {
                entryParentRedirBtn.setDisable(context.getParentEntry() == null);
                subDirBtn.setDisable(!context.hasChildren());
                projectRootRedirBtn.setDisable(true); // Root navigation not yet implemented
            } else {
                updateNavigationButtons();
            }
            
            // Get effective permission for UI restrictions
            UserPermission userPerm = entry.getUserPermissionWithCascade(currentUser);
            EPermission permission = userPerm.getPermission();
            boolean isEditor = permission.canEdit();
            boolean isCommenter = permission.canComment();
            
            // Disable save button and lock content fields if not editor
            entryTitleField.setDisable(!isEditor);
            entryContent.setDisable(!isEditor);
            saveEntryBtn.setDisable(!isEditor);
            
            // Lock comment input and button for readers
            commentInput.setDisable(!isCommenter);
            addCommentBtn.setDisable(!isCommenter);
        } catch (Exception e) {
            showErrorDialog("Error displaying entry", e.getMessage());
        }
    }

    /**
     * Displays comments for an entry
     */
    private void displayComments(Entry entry) {
        commentsList.getItems().clear();
        commentsList.setCellFactory(param -> new ListCell<Comment>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) {
                    setText(null);
                    setWrapText(false);
                } else {
                    setText(comment.toString());
                    setWrapText(true);
                }
            }
        });
        List<Comment> comments = entry.getComments();
        for (Comment comment : comments) {
            commentsList.getItems().add(comment);
        }
    }

    /**
     * Displays users with permissions on the current entry
     */
    private void displayUsersWithPermissions(Entry entry) {
        if (usersPermissionsList == null) return;
        
        usersPermissionsList.getItems().clear();
        usersPermissionsList.setCellFactory(param -> new ListCell<UserPermission>() {
            @Override
            protected void updateItem(UserPermission userPerm, boolean empty) {
                super.updateItem(userPerm, empty);
                if (empty || userPerm == null) {
                    setText(null);
                } else {
                    // Only display if permission is not null (don't show explicit denials)
                    if (userPerm.getPermission() == null) {
                        setText(userPerm.getUser().getUsername() + " - NO ACCESS");
                    } else {
                        setText(userPerm.getUser().getUsername() + " - " + userPerm.getPermission().name());
                    }
                }
            }
        });
        
        List<UserPermission> permissions = entry.getUsersWithPermissions();
        for (UserPermission perm : permissions) {
            usersPermissionsList.getItems().add(perm);
        }
    }

    /**
     * Updates navigation button states
     */
    private void updateNavigationButtons() {
        Entry current = entryManager.getCurrentEntry();
        projectRootRedirBtn.setDisable(true); // Root navigation not yet implemented
        entryParentRedirBtn.setDisable(current == null || current.getParentEntry() == null);
    }

    /**
     * Handles saving the current entry data and permissions (Manual Save)
     * EDITOR only - COMMENTOR and READER users are blocked
     */
    @FXML
    public void onSaveEntry() {
        Entry current = entryManager.getCurrentEntry();
        if (current == null) {
            showErrorDialog("Error", "No entry selected");
            return;
        }
        
        try {
            String newTitle = entryTitleField.getText();
            String newContent = entryContent.getText();
            
            // Call the new updateEntry method with permission checks
            // This will throw PermissionException if user is not EDITOR
            entryManager.updateEntry(current.getId(), newTitle, newContent, null);
            
            showInfoDialog("Success", "Entry saved successfully");
            
            // Reload the entry to refresh UI with fresh data from database
            loadAndDisplayEntry(current.getId());
        } catch (EntryManager.PermissionException e) {
            showErrorDialog("Permission Denied", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error saving entry", e.getMessage());
        }
    }

    /**
     * Handles creating a new entry
     */
    @FXML
    public void onCreateEntry() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Entry");
        dialog.setHeaderText("Enter the title for the new entry");
        dialog.setContentText("Title:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Entry current = entryManager.getCurrentEntry();
                if (current != null) {
                    Entry newEntry = entryManager.createNewEntry(result.get(), "", current.getAuthor());
                    entryManager.attachChildToParent(current, newEntry);
                    displayEntry(current);
                    showInfoDialog("Success", "Entry created successfully");
                }
            } catch (Exception e) {
                showErrorDialog("Error creating entry", e.getMessage());
            }
        }
    }

    /**
     * Handles deleting the current entry
     */
    @FXML
    public void onDeleteEntry() {
        Entry current = entryManager.getCurrentEntry();
        if (current != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Entry");
            confirmation.setContentText("Are you sure you want to delete this entry?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    entryManager.removeEntry(current.getId());
                    Entry parent = current.getParentEntry();
                    if (parent != null) {
                        displayEntry(parent);
                    }
                    showInfoDialog("Success", "Entry deleted successfully");
                } catch (Exception e) {
                    showErrorDialog("Error deleting entry", e.getMessage());
                }
            }
        }
    }

    /**
     * Handles adding a comment (Auto-Save)
     * COMMENTOR or EDITOR - READER users are blocked
     */
    @FXML
    public void onAddComment() {
        Entry current = entryManager.getCurrentEntry();
        if (current == null) {
            showErrorDialog("Error", "No entry selected");
            return;
        }
        
        String commentText = commentInput.getText().trim();
        if (commentText.isEmpty()) {
            showErrorDialog("Error", "Comment cannot be empty");
            return;
        }
        
        try {
            // Create comment with current user as author
            Comment comment = new Comment(commentText, currentUser);
            
            // Call the new addComment method with permission checks (Auto-Save)
            // This will throw PermissionException if user is not COMMENTOR or EDITOR
            entryManager.addComment(current.getId(), comment);
            
            // Clear input and refresh display
            commentInput.clear();
            displayComments(current);
            showInfoDialog("Success", "Comment added successfully");
        } catch (EntryManager.PermissionException e) {
            showErrorDialog("Permission Denied", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error adding comment", e.getMessage());
        }
    }

    /**
     * Handles adding a permission to an entry
     */
    @FXML
    public void onAddPermission() {
        Entry current = entryManager.getCurrentEntry();
        String username = usernameField.getText();
        EPermission permission = permissionComboBox.getValue();
        
        if (current == null) {
            showErrorDialog("Error", "No entry selected");
            return;
        }
        
        if (username == null || username.trim().isEmpty()) {
            showErrorDialog("Error", "Please enter a username");
            return;
        }
        
        if (permission == null) {
            showErrorDialog("Error", "Please select a permission level");
            return;
        }
        
        try {
            entryManager.setUserPermissionByUsername(current, username, permission);
            usernameField.clear();
            permissionComboBox.setValue(EPermission.READER);
            displayUsersWithPermissions(current);
            showInfoDialog("Success", "Permission added for user: " + username);
        } catch (Exception e) {
            showErrorDialog("Error adding permission", e.getMessage());
        }
    }

    /**
     * Handles navigation to parent entry
     */
    @FXML
    public void onNavigateToParent() {
        try {
            EntryContextDTO parentContext = entryManager.navigateToParent();
            if (parentContext != null) {
                Entry parent = parentContext.getTargetEntry();
                displayEntry(parent);
                
                // Update navigation buttons based on new context
                entryParentRedirBtn.setDisable(parentContext.isRoot());
                subDirBtn.setDisable(!parentContext.hasChildren());
            }
        } catch (EntryManager.PermissionException e) {
            showErrorDialog("Permission Denied", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error navigating", e.getMessage());
        }
    }

    /**
     * Handles navigation to root entry
     * TODO: Implement when root entry navigation feature is added
     */
    @FXML
    public void onNavigateToRoot() {
        // Root navigation not yet implemented
    }

    /**
     * Handles import button click
     */
    @FXML
    public void onImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Entry");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                IEntryImporter importer;
                if (selectedFile.getName().endsWith(".json")) {
                    importer = new EntryJsonImporter();
                } else if (selectedFile.getName().endsWith(".xml")) {
                    importer = new EntryXmlImporter();
                } else {
                    throw new IllegalArgumentException("Unsupported file format");
                }

                Entry importedEntry = importer.importEntry(selectedFile.getAbsolutePath());
                if (importedEntry != null) {
                    entryManager.setCurrentEntry(importedEntry);
                    displayEntry(importedEntry);
                    showInfoDialog("Success", "Entry imported successfully");
                }
            } catch (Exception e) {
                showErrorDialog("Error importing entry", e.getMessage());
            }
        }
    }

    /**
     * Handles export button click
     */
    @FXML
    public void onExport() {
        Entry current = entryManager.getCurrentEntry();
        if (current == null) {
            showErrorDialog("Error", "No entry selected to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Entry");
        fileChooser.setInitialFileName(current.getTitle() + ".json");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            try {
                IEntryExporter exporter;
                if (selectedFile.getName().endsWith(".json")) {
                    exporter = new EntryJsonExporter();
                } else if (selectedFile.getName().endsWith(".xml")) {
                    exporter = new EntryXmlExporter();
                } else {
                    throw new IllegalArgumentException("Unsupported file format");
                }

                exporter.exportEntry(current, selectedFile.getAbsolutePath());
                showInfoDialog("Success", "Entry exported successfully to " + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                showErrorDialog("Error exporting entry", e.getMessage());
            }
        }
    }

    /**
     * Helper method to show error dialogs
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show info dialogs
     */
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
