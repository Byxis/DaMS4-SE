package fr.opal.controller;

import fr.opal.type.*;
import fr.opal.facade.AuthFacade;
import fr.opal.facade.EntryFacade;
import fr.opal.facade.SessionPropertiesFacade;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

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
    private ListView<Message> commentsList;
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

    private AuthFacade authFacade;
    private SessionPropertiesFacade sessionPropertiesFacade;
    private EntryFacade entryFacade;
    private User currentUser;
    private ContextMenu childrenContextMenu;

    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        // Initialize facades
        authFacade = AuthFacade.getInstance();
        sessionPropertiesFacade = SessionPropertiesFacade.getInstance();
        entryFacade = EntryFacade.getInstance();
        
        if (authFacade.isAuthenticated()) {
            currentUser = authFacade.getConnectedUser();
            
            // Load and apply session settings (theme/font size) using Platform.runLater
            sessionPropertiesFacade.loadSettings(currentUser.getId());
            javafx.application.Platform.runLater(() -> {
                try {
                    javafx.scene.Parent root = (javafx.scene.Parent) ((javafx.scene.control.Control) commentsList).getScene().getRoot();
                    if (root != null) {
                        sessionPropertiesFacade.applyTheme(root);
                    }
                } catch (Exception e) {
                    // Root may not be available yet
                }
            });
            
            try {
                // Load the root "Sample Project" entry from DATABASE via Facade
                EntryContextDTO rootContext = entryFacade.loadInitialProject("Sample Project");
                
                if (rootContext != null) {
                    displayEntry(rootContext.getTargetEntry(), rootContext);
                } else {
                    showErrorDialog("Error", "Sample Project entry not found - ensure database is initialized");
                }
            } catch (Exception e) {
                showErrorDialog("Error", "Failed to load entry system: " + e.getMessage());
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
        Entry current = entryFacade.getCurrentEntry();
        if (current == null) {
            return;
        }
        
        // Reload current entry with context to get fresh children data
        try {
            EntryContextDTO context = entryFacade.loadEntry(current.getId());
            if (context == null || !context.hasChildren()) {
                return;
            }
            
            childrenContextMenu = new ContextMenu();
            for (Entry child : context.getChildEntries()) {
                MenuItem menuItem = new MenuItem(child.getTitle());
                menuItem.setOnAction(e -> {
                    // Reload child from database with full Depth-1 context
                    loadAndDisplayEntry(child.getId());
                });
                childrenContextMenu.getItems().add(menuItem);
            }
            
            childrenContextMenu.show(subDirBtn, javafx.geometry.Side.BOTTOM, 0, 0);
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
            EntryContextDTO context = entryFacade.loadEntryWithAccessCheck(entryId, currentUser);
            if (context != null) {
                displayEntry(context.getTargetEntry(), context);
            }
        } catch (EntryFacade.AccessDeniedException e) {
            showErrorDialog("Access Denied", e.getMessage());
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
        // Collapse children menu when loading a new entry
        if (childrenContextMenu != null && childrenContextMenu.isShowing()) {
            childrenContextMenu.hide();
        }
        
        try {
            // Get UI state from facade (permission checks happen in facade/manager)
            EntryFacade.EntryUIState uiState = entryFacade.getEntryUIState(entry, currentUser);
            
            if (!uiState.canView()) {
                showErrorDialog("Access Denied", "You do not have permission to view this entry.");
                return;
            }
            
            entryFacade.setCurrentEntry(entry);
            entryTitleField.setText(entry.getTitle());
            entryContent.setText(entry.getContent());
            projectTitle.setText(entry.getRootEntry().getTitle());
            displayComments(entry);
            displayUsersWithPermissions(entry);
            
            // Update navigation buttons based on context if provided
            if (context != null) {
                entryParentRedirBtn.setDisable(context.getParentEntry() == null);
                subDirBtn.setDisable(!context.hasChildren());
                projectRootRedirBtn.setDisable(true); // Root navigation not yet implemented
            } else {
                updateNavigationButtons();
            }
            
            // Apply UI restrictions based on permissions from facade
            entryTitleField.setDisable(!uiState.canEdit());
            entryContent.setDisable(!uiState.canEdit());
            saveEntryBtn.setDisable(!uiState.canEdit());
            
            commentInput.setDisable(!uiState.canComment());
            addCommentBtn.setDisable(!uiState.canComment());
            
            usernameField.setDisable(!uiState.canEdit());
            permissionComboBox.setDisable(!uiState.canEdit());
            addPermissionBtn.setDisable(!uiState.canEdit());
        } catch (Exception e) {
            showErrorDialog("Error displaying entry", e.getMessage());
        }
    }

    /**
     * Displays comments (messages) for an entry from its unified channel
     */
    private void displayComments(Entry entry) {
        commentsList.getItems().clear();
        commentsList.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setWrapText(false);
                } else {
                    setText(message.toString());
                    setWrapText(true);
                }
            }
        });
        List<Message> messages = entry.getMessages();
        for (Message message : messages) {
            commentsList.getItems().add(message);
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
        Entry current = entryFacade.getCurrentEntry();
        projectRootRedirBtn.setDisable(true); // Root navigation not yet implemented
        entryParentRedirBtn.setDisable(current == null || current.getParentEntry() == null);
    }

    /**
     * Handles saving the current entry content (Manual Save)
     * Scope: Title and Content only
     * Permission: EDITOR only - COMMENTOR and READER users are blocked
     */
    @FXML
    public void onSaveEntry() {
        Entry current = entryFacade.getCurrentEntry();
        if (current == null) {
            showErrorDialog("Error", "No entry selected");
            return;
        }
        
        try {
            String newTitle = entryTitleField.getText();
            String newContent = entryContent.getText();
            
            // Delegate to facade (permission check happens in manager)
            entryFacade.updateEntryContent(current.getId(), newTitle, newContent, currentUser);
            
            showInfoDialog("Success", "Entry saved successfully");
            
            // Reload the entry to refresh UI with fresh data from database
            loadAndDisplayEntry(current.getId());
        } catch (EntryFacade.PermissionDeniedException e) {
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
                Entry current = entryFacade.getCurrentEntry();
                if (current != null) {
                    entryFacade.createChildEntry(current, result.get(), "", currentUser);
                    // Reload current entry to refresh display
                    loadAndDisplayEntry(current.getId());
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
        Entry current = entryFacade.getCurrentEntry();
        if (current != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Entry");
            confirmation.setContentText("Are you sure you want to delete this entry?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    Entry parent = current.getParentEntry();
                    entryFacade.deleteEntry(current.getId());
                    if (parent != null) {
                        loadAndDisplayEntry(parent.getId());
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
        Entry current = entryFacade.getCurrentEntry();
        if (current == null) {
            showErrorDialog("Error", "No entry selected");
            return;
        }
        
        String commentText = commentInput.getText();
        
        try {
            // Delegate all validation, message creation, and permission checks to facade
            entryFacade.addComment(current, currentUser, commentText);
            
            // Clear input and refresh display
            commentInput.clear();
            displayComments(current);
            showInfoDialog("Success", "Comment added successfully");
        } catch (EntryFacade.PermissionDeniedException e) {
            showErrorDialog("Permission Denied", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error adding comment", e.getMessage());
        }
    }

    /**
     * Handles adding a permission to an entry (Auto-Save)
     * Scope: Permissions only
     * Permission: EDITOR only - auto-saves immediately
     */
    @FXML
    public void onAddPermission() {
        Entry current = entryFacade.getCurrentEntry();
        String username = usernameField.getText();
        EPermission permission = permissionComboBox.getValue();
        
        if (current == null) {
            showErrorDialog("Error", "No entry selected");
            return;
        }
        
        try {
            // Delegate all validation and permission setting to facade
            entryFacade.setUserPermission(current, username, permission, currentUser);
            usernameField.clear();
            permissionComboBox.setValue(EPermission.READER);
            displayUsersWithPermissions(current);
            showInfoDialog("Success", "Permission added for user: " + username);
        } catch (EntryFacade.PermissionDeniedException e) {
            showErrorDialog("Permission Denied", e.getMessage());
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
            EntryContextDTO parentContext = entryFacade.navigateToParent(currentUser);
            if (parentContext != null) {
                Entry parent = parentContext.getTargetEntry();
                displayEntry(parent, parentContext);
            }
        } catch (EntryFacade.PermissionDeniedException e) {
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
                Entry importedEntry = entryFacade.importEntry(selectedFile);
                if (importedEntry != null) {
                    displayEntry(importedEntry);
                    showInfoDialog("Success", "Entry imported successfully");
                }
            } catch (IllegalArgumentException e) {
                showErrorDialog("Error", e.getMessage());
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
        Entry current = entryFacade.getCurrentEntry();
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
                entryFacade.exportEntry(current, selectedFile);
                showInfoDialog("Success", "Entry exported successfully to " + selectedFile.getAbsolutePath());
            } catch (IllegalArgumentException e) {
                showErrorDialog("Error", e.getMessage());
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
