package fr.opal.controller;

import fr.opal.type.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import fr.opal.service.EntryManager;
import fr.opal.service.AuthManager;
import fr.opal.service.IEntryImporter;
import fr.opal.service.IEntryExporter;
import fr.opal.service.EntryJsonImporter;
import fr.opal.service.EntryXmlImporter;
import fr.opal.service.EntryJsonExporter;
import fr.opal.service.EntryXmlExporter;
import fr.opal.facade.EntryFacade;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Controller for entry management UI
 */
public class EntryController {
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
    private EntryFacade facade;
    private User currentUser;

    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        // Initialize with current authenticated user
        authManager = AuthManager.getInstance();
        facade = EntryFacade.getInstance();
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
     * Handles showing subdirectories in a popup menu on hover
     */
    public void showSubDirectoriesOnHover() {
        Entry current = entryManager.getCurrentEntry();
        if (current == null || current.getChildEntries().isEmpty()) {
            return;
        }
        
        ContextMenu contextMenu = new ContextMenu();
        for (Entry child : current.getChildEntries()) {
            MenuItem menuItem = new MenuItem(child.getTitle());
            menuItem.setOnAction(e -> {
                // Reload child from database with full Depth-1 context
                loadAndDisplayEntry(child.getId());
            });
            contextMenu.getItems().add(menuItem);
        }
        
        contextMenu.show(subDirBtn, javafx.geometry.Side.BOTTOM, 0, 0);
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
                displayEntry(targetEntry);
                
                // Parent is now available for navigation validation
                Entry parent = context.getParentEntry();
                if (parent == null) {
                    entryParentRedirBtn.setDisable(true);
                } else {
                    entryParentRedirBtn.setDisable(false);
                }
                
                // Children are now available
                if (!context.hasChildren()) {
                    subDirBtn.setDisable(true);
                } else {
                    subDirBtn.setDisable(false);
                }
            }
        } catch (Exception e) {
            showErrorDialog("Error loading entry", e.getMessage());
        }
    }

    /**
     * Displays an entry's content
     */
    private void displayEntry(Entry entry) {
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
            updateNavigationButtons();
            
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
        projectRootRedirBtn.setDisable(current == null || current.getRootEntry() == current);
        entryParentRedirBtn.setDisable(current == null || current.getParentEntry() == null);
    }

    /**
     * Handles saving the current entry
     */
    @FXML
    public void onSaveEntry() {
        try {
            Entry current = entryManager.getCurrentEntry();
            if (current != null) {
                current.setTitle(entryTitleField.getText());
                current.setContent(entryContent.getText());
                saveComments(current);
                entryManager.persistEntry(current);
                showInfoDialog("Success", "Entry saved successfully");
            }
        } catch (Exception e) {
            showErrorDialog("Error saving entry", e.getMessage());
        }
    }

    /**
     * Saves only the comments of an entry
     */
    private void saveComments(Entry entry) {
        try {
            entryManager.persistEntry(entry);
        } catch (Exception e) {
            showErrorDialog("Error saving comments", e.getMessage());
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
     * Handles adding a comment
     */
    @FXML
    public void onAddComment() {
        Entry current = entryManager.getCurrentEntry();
        if (current != null && !commentInput.getText().isEmpty()) {
            try {
                Comment comment = new Comment(commentInput.getText(), current.getAuthor());
                entryManager.persistCommentToEntry(current, comment);
                commentInput.clear();
                displayComments(current);
                showInfoDialog("Success", "Comment added successfully");
            } catch (Exception e) {
                showErrorDialog("Error adding comment", e.getMessage());
            }
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
     */
    @FXML
    public void onNavigateToRoot() {
        try {
            Entry current = entryManager.getCurrentEntry();
            if (current != null) {
                Entry root = current.getRootEntry();
                displayEntry(root);
            }
        } catch (Exception e) {
            showErrorDialog("Error navigating", e.getMessage());
        }
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
