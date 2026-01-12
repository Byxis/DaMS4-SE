package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.facade.FriendsFacade;
import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.type.Session;
import fr.opal.type.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

/**
 * Controller for the friend search view.
 */
public class FriendSearchController {

    @FXML
    private VBox rootPane;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private VBox searchResultsContainer;

    private FriendsFacade friendsFacade;
    private AuthFacade authFacade;
    private SessionPropertiesFacade sessionPropertiesFacade;
    private Session currentSession;

    public FriendSearchController() {
        this.friendsFacade = FriendsFacade.getInstance();
        this.authFacade = AuthFacade.getInstance();
        this.sessionPropertiesFacade = SessionPropertiesFacade.getInstance();
    }

    @FXML
    public void initialize() {
        currentSession = authFacade.getCurrentSession();
        
        // Load and apply user theme preferences
        if (currentSession != null) {
            sessionPropertiesFacade.loadSettings(currentSession.getUserId());
            javafx.application.Platform.runLater(() -> {
                if (rootPane != null) {
                    sessionPropertiesFacade.applyTheme(rootPane);
                }
            });
        }
        
        // Set up search on Enter key press
        searchField.setOnAction(e -> handleSearch());
    }

    /**
     * Handles the search action.
     */
    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            showError("Please enter a search term");
            return;
        }
        
        ArrayList<User> results = friendsFacade.searchUsers(query);
        displaySearchResults(results);
    }

    /**
     * Displays search results.
     */
    private void displaySearchResults(ArrayList<User> results) {
        searchResultsContainer.getChildren().clear();
        
        if (results.isEmpty()) {
            Label emptyLabel = new Label("No users found");
            emptyLabel.getStyleClass().add("muted-label");
            searchResultsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (User user : results) {
            // Don't show the current user in results
            if (user.getId() == currentSession.getUserId()) {
                continue;
            }
            
            HBox userItem = createUserItem(user);
            searchResultsContainer.getChildren().add(userItem);
        }
    }

    /**
     * Creates a UI item for a search result.
     */
    private HBox createUserItem(User user) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("search-result-item");
        
        // Username label
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.getStyleClass().add("title-label");
        
        // Friend/Follower counts
        int friendCount = friendsFacade.getFriendCount(user.getId());
        int followerCount = friendsFacade.getFollowerCount(user.getId());
        Label statsLabel = new Label("Friends: " + friendCount + "  Followers: " + followerCount);
        statsLabel.getStyleClass().add("subtitle-label");
        
        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(usernameLabel, statsLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        // Action buttons
        HBox buttonBox = new HBox(5);
        
        int currentUserId = currentSession.getUserId();
        int targetUserId = user.getId();
        
        // Check relationships
        boolean isBlocked = friendsFacade.isBlocked(currentUserId, targetUserId);
        boolean isFriend = friendsFacade.isFriend(currentUserId, targetUserId);
        boolean isFollowing = friendsFacade.isFollowing(currentUserId, targetUserId);
        boolean hasPendingRequest = friendsFacade.hasPendingFriendRequest(currentUserId, targetUserId);
        
        if (isBlocked) {
            // User is blocked, disable actions
            Label blockedLabel = new Label("Blocked");
            blockedLabel.getStyleClass().add("blocked-label");
            buttonBox.getChildren().add(blockedLabel);
        } else {
            // Friend Request button
            if (isFriend) {
                Label friendLabel = new Label("✓ Friends");
                friendLabel.getStyleClass().add("friend-label");
                buttonBox.getChildren().add(friendLabel);
            } else if (hasPendingRequest) {
                Label pendingLabel = new Label("Request Sent");
                pendingLabel.getStyleClass().add("pending-label");
                buttonBox.getChildren().add(pendingLabel);
            } else {
                Button friendRequestBtn = new Button("Friend Request");
                friendRequestBtn.getStyleClass().addAll("button", "primary-button");
                friendRequestBtn.setMinWidth(120);
                friendRequestBtn.setPrefWidth(120);
                friendRequestBtn.setOnAction(e -> handleFriendRequest(targetUserId));
                buttonBox.getChildren().add(friendRequestBtn);
            }
            
            // Follow button
            if (isFollowing) {
                Button unfollowBtn = new Button("Unfollow");
                unfollowBtn.getStyleClass().addAll("button");
                unfollowBtn.setMinWidth(100);
                unfollowBtn.setPrefWidth(100);
                unfollowBtn.setOnAction(e -> handleUnfollow(targetUserId));
                buttonBox.getChildren().add(unfollowBtn);
            } else {
                Button followBtn = new Button("Follow");
                followBtn.getStyleClass().addAll("button");
                followBtn.setMinWidth(100);
                followBtn.setPrefWidth(100);
                followBtn.setOnAction(e -> handleFollow(targetUserId));
                buttonBox.getChildren().add(followBtn);
            }
            
            // Block button
            Button blockBtn = new Button("Block");
            blockBtn.getStyleClass().addAll("button", "danger-button");
            blockBtn.setMinWidth(100);
            blockBtn.setPrefWidth(100);
            blockBtn.setOnAction(e -> handleBlock(targetUserId));
            buttonBox.getChildren().add(blockBtn);
        }
        
        item.getChildren().addAll(infoBox, buttonBox);
        return item;
    }

    /**
     * Handles sending a friend request.
     */
    private void handleFriendRequest(int targetUserId) {
        boolean success = friendsFacade.sendFriendRequest(currentSession.getUserId(), targetUserId);
        if (success) {
            handleSearch(); // Refresh results
        } else {
            showError("Failed to send friend request");
        }
    }

    /**
     * Handles following a user.
     */
    private void handleFollow(int targetUserId) {
        boolean success = friendsFacade.follow(currentSession.getUserId(), targetUserId);
        if (success) {
            handleSearch(); // Refresh results
        } else {
            showError("Failed to follow user");
        }
    }

    /**
     * Handles unfollowing a user.
     */
    private void handleUnfollow(int targetUserId) {
        boolean success = friendsFacade.unfollow(currentSession.getUserId(), targetUserId);
        if (success) {
            handleSearch(); // Refresh results
        } else {
            showError("Failed to unfollow user");
        }
    }

    /**
     * Handles blocking a user.
     */
    private void handleBlock(int targetUserId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Block User");
        confirmAlert.setHeaderText("Are you sure you want to block this user?");
        confirmAlert.setContentText("This will remove any existing friendship and prevent future interactions.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = friendsFacade.block(currentSession.getUserId(), targetUserId);
                if (success) {
                    handleSearch(); // Refresh results
                } else {
                    showError("Failed to block user");
                }
            }
        });
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
