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

    private EntryManager entryManager;
    private AuthManager authManager;
    private User currentUser;

    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        // Initialize with current authenticated user
        authManager = AuthManager.getInstance();
        if (authManager.isAuthenticated()) {
            currentUser = authManager.getConnectedUser();
            entryManager = new EntryManager(currentUser);
            
            // Initialize with placeholder entry if none exists
            Entry placeholderEntry = entryManager.getCurrentEntry();
            if (placeholderEntry == null) {
                try {
                    placeholderEntry = new Entry("Sample Project", "This is a sample project for testing the Entry Management System.", currentUser);
                    placeholderEntry.getPermissionManager().addUserPermission(
                        new UserPermission(currentUser, EPermission.EDITOR)
                    );

                    // Create nested child entries
                    Entry chapter1 = new Entry("Chapter 1: Introduction", "Introduction to the project and its purpose.", currentUser);
                    chapter1.getPermissionManager().addUserPermission(
                        new UserPermission(currentUser, EPermission.EDITOR)
                    );
                    placeholderEntry.addChildEntry(chapter1);
                    Entry section1_1 = new Entry("1.1 Overview", "This section provides an overview of the system architecture.", currentUser);
                    section1_1.getPermissionManager().addUserPermission(
                        new UserPermission(currentUser, EPermission.EDITOR)
                    );
                    chapter1.addChildEntry(section1_1);
                    
                    Entry section1_2 = new Entry("1.2 Key Features", "Key features and capabilities of the system.", currentUser);
                    section1_2.getPermissionManager().addUserPermission(
                        new UserPermission(currentUser, EPermission.EDITOR)
                    );
                    chapter1.addChildEntry(section1_2);
                    
                    // Entry with NO permissions
                    Entry chapter2 = new Entry("[NO ACCESS] Chapter 2: Implementation", "Implementation details and technical decisions.", currentUser);
                    chapter2.getPermissionManager().removeUserPermission(currentUser);
                    placeholderEntry.addChildEntry(chapter2);
                    
                    // Entry with READER only permissions
                    Entry section2_1 = new Entry("[READER ONLY] Architecture", "System architecture and design patterns.", currentUser);
                    section2_1.getPermissionManager().removeUserPermission(currentUser);
                    section2_1.getPermissionManager().addUserPermission(
                        new UserPermission(currentUser, EPermission.READER)
                    );
                    placeholderEntry.addChildEntry(section2_1);
                    
                    // Entry with COMMENTER only permissions
                    Entry section2_2 = new Entry("[COMMENTER ONLY] Database Schema", "Database design and relationships.", currentUser);
                    section2_2.getPermissionManager().removeUserPermission(currentUser);
                    section2_2.getPermissionManager().addUserPermission(
                        new UserPermission(currentUser, EPermission.COMMENTOR)
                    );
                    placeholderEntry.addChildEntry(section2_2);
                    
                    Entry chapter3 = new Entry("Chapter 3: Conclusion", "Final thoughts and future improvements.", currentUser);
                    chapter3.getPermissionManager().addUserPermission(
                        new UserPermission(currentUser, EPermission.EDITOR)
                    );
                    placeholderEntry.addChildEntry(chapter3);
                    
                    entryManager.setCurrentEntry(placeholderEntry);
                    displayEntry(placeholderEntry);
                } catch (Exception e) {
                    showErrorDialog("Error", "Failed to initialize placeholder entry: " + e.getMessage());
                }
            }
            
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
                displayEntry(child);
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
     * Displays an entry's content
     */
    private void displayEntry(Entry entry) {
        try {
            // Check if user has permission to view this entry
            UserPermission userPerm = entry.getPermissionManager().getUserPermission(currentUser);
            if (userPerm == null) {
                showErrorDialog("Access Denied", "You do not have permission to view this entry.");
                return;
            }
            
            entryManager.setCurrentEntry(entry);
            entryTitleField.setText(entry.getTitle());
            entryContent.setText(entry.getContent());
            projectTitle.setText(entry.getRootEntry().getTitle());
            displayComments(entry);
            updateNavigationButtons();
            
            // Apply permission-based UI restrictions
            EPermission permission = userPerm.getPermission();
            boolean isEditor = permission.canEdit();
            boolean isCommenter = permission.canComment();
            boolean isReader = permission == EPermission.READER;
            
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
                entryManager.saveEntry(current);
                showInfoDialog("Success", "Entry saved successfully");
            }
        } catch (EntryManager.PermissionException e) {
            showErrorDialog("Permission Denied", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error saving entry", e.getMessage());
        }
    }

    /**
     * Saves only the comments of an entry
     */
    private void saveComments(Entry entry) {
        try {
            entryManager.saveEntry(entry);
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
                Entry newEntry = entryManager.createEntry(result.get(), "");
                Entry current = entryManager.getCurrentEntry();
                if (current != null) {
                    entryManager.addChild(current, newEntry);
                    displayEntry(current);
                }
                showInfoDialog("Success", "Entry created successfully");
            } catch (EntryManager.PermissionException e) {
                showErrorDialog("Permission Denied", e.getMessage());
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
                    entryManager.deleteEntry(current);
                    Entry parent = current.getParentEntry();
                    if (parent != null) {
                        displayEntry(parent);
                    }
                    showInfoDialog("Success", "Entry deleted successfully");
                } catch (EntryManager.PermissionException e) {
                    showErrorDialog("Permission Denied", e.getMessage());
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
                entryManager.addComment(current, commentInput.getText());
                commentInput.clear();
                displayComments(current);
                showInfoDialog("Success", "Comment added successfully");
                
                // Try to save comments, suppress any errors
                saveComments(current);
            } catch (EntryManager.PermissionException e) {
                showErrorDialog("Permission Denied", e.getMessage());
            } catch (Exception e) {
                showErrorDialog("Error adding comment", e.getMessage());
            }
        }
    }

    /**
     * Handles navigation to parent entry
     */
    @FXML
    public void onNavigateToParent() {
        try {
            Entry parent = entryManager.navigateToParent();
            if (parent != null) {
                displayEntry(parent);
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
