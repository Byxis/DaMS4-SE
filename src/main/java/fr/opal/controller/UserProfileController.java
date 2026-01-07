package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.facade.FriendsFacade;
import fr.opal.type.Session;
import fr.opal.type.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller for viewing another user's profile.
 */
public class UserProfileController {

    @FXML
    private Label profileUsernameLabel;
    @FXML
    private Label friendsCountLabel;
    @FXML
    private Label followersCountLabel;
    @FXML
    private Button friendRequestBtn;
    @FXML
    private Button followBtn;
    @FXML
    private Button blockBtn;
    @FXML
    private Label statusLabel;

    private FriendsFacade friendsFacade;
    private AuthFacade authFacade;
    private Session currentSession;
    private User targetUser;

    public UserProfileController() {
        this.friendsFacade = FriendsFacade.getInstance();
        this.authFacade = AuthFacade.getInstance();
    }

    @FXML
    public void initialize() {
        currentSession = authFacade.getCurrentSession();
    }

    /**
     * Sets the target user to display.
     *
     * @param user The user to display
     */
    public void setTargetUser(User user) {
        this.targetUser = user;
        loadUserProfile();
        setupButtons();
    }

    /**
     * Loads and displays the user's profile information.
     */
    private void loadUserProfile() {
        profileUsernameLabel.setText(targetUser.getUsername());
        
        int friendCount = friendsFacade.getFriendCount(targetUser.getId());
        int followerCount = friendsFacade.getFollowerCount(targetUser.getId());
        
        friendsCountLabel.setText(String.valueOf(friendCount));
        followersCountLabel.setText(String.valueOf(followerCount));
    }

    /**
     * Sets up button states based on current relationships.
     */
    private void setupButtons() {
        int currentUserId = currentSession.getUserId();
        int targetUserId = targetUser.getId();
        
        boolean isBlocked = friendsFacade.isBlocked(currentUserId, targetUserId);
        boolean isFriend = friendsFacade.isFriend(currentUserId, targetUserId);
        boolean isFollowing = friendsFacade.isFollowing(currentUserId, targetUserId);
        boolean hasPendingRequest = friendsFacade.hasPendingFriendRequest(currentUserId, targetUserId);
        
        if (isBlocked) {
            disableAllButtons();
            statusLabel.setText("This user has blocked you");
            statusLabel.setVisible(true);
        } else {
            // Friend Request Button
            if (isFriend) {
                friendRequestBtn.setText("✓ Friends");
                friendRequestBtn.setDisable(true);
            } else if (hasPendingRequest) {
                friendRequestBtn.setText("Request Sent");
                friendRequestBtn.setDisable(true);
            } else {
                friendRequestBtn.setText("Friend Request");
                friendRequestBtn.setDisable(false);
                friendRequestBtn.setOnAction(e -> handleFriendRequest());
            }
            
            // Follow Button
            if (isFollowing) {
                followBtn.setText("Unfollow");
                followBtn.setOnAction(e -> handleUnfollow());
            } else {
                followBtn.setText("Follow");
                followBtn.setOnAction(e -> handleFollow());
            }
            
            // Block Button
            blockBtn.setOnAction(e -> handleBlock());
        }
    }

    /**
     * Handles sending a friend request.
     */
    private void handleFriendRequest() {
        boolean success = friendsFacade.sendFriendRequest(
            currentSession.getUserId(), 
            targetUser.getId()
        );
        
        if (success) {
            friendRequestBtn.setText("Request Sent");
            friendRequestBtn.setDisable(true);
            showSuccess("Friend request sent!");
        } else {
            showError("Failed to send friend request");
        }
    }

    /**
     * Handles following the user.
     */
    private void handleFollow() {
        boolean success = friendsFacade.follow(
            currentSession.getUserId(), 
            targetUser.getId()
        );
        
        if (success) {
            followBtn.setText("Unfollow");
            followBtn.setOnAction(e -> handleUnfollow());
            showSuccess("Now following " + targetUser.getUsername());
        } else {
            showError("Failed to follow user");
        }
    }

    /**
     * Handles unfollowing the user.
     */
    private void handleUnfollow() {
        boolean success = friendsFacade.unfollow(
            currentSession.getUserId(), 
            targetUser.getId()
        );
        
        if (success) {
            followBtn.setText("Follow");
            followBtn.setOnAction(e -> handleFollow());
            showSuccess("Unfollowed " + targetUser.getUsername());
        } else {
            showError("Failed to unfollow user");
        }
    }

    /**
     * Handles blocking the user.
     */
    private void handleBlock() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Block User");
        confirmAlert.setHeaderText("Are you sure you want to block " + targetUser.getUsername() + "?");
        confirmAlert.setContentText("This will remove any existing friendship and prevent future interactions.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = friendsFacade.block(
                    currentSession.getUserId(), 
                    targetUser.getId()
                );
                
                if (success) {
                    showSuccess("Blocked " + targetUser.getUsername());
                    closeWindow();
                } else {
                    showError("Failed to block user");
                }
            }
        });
    }

    /**
     * Disables all action buttons.
     */
    private void disableAllButtons() {
        friendRequestBtn.setDisable(true);
        followBtn.setDisable(true);
        blockBtn.setDisable(true);
    }

    /**
     * Shows a success message.
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setStyle("-fx-text-fill: #27AE60;");
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

    /**
     * Closes the profile window.
     */
    private void closeWindow() {
        Stage stage = (Stage) profileUsernameLabel.getScene().getWindow();
        stage.close();
    }
}
