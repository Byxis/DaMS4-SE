package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.facade.FriendsFacade;
import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.service.SceneManager;
import fr.opal.service.AuthManager;
import fr.opal.type.Session;
import fr.opal.type.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Controller for the friend list view.
 */
public class FriendListController {

    @FXML
    private VBox rootPane;
    @FXML
    private VBox friendsContainer;
    @FXML
    private VBox followersContainer;
    @FXML
    private VBox pendingRequestsContainer;
    @FXML
    private Label friendsCountLabel;
    @FXML
    private Label followersCountLabel;
    @FXML
    private Label pendingCountLabel;

    private FriendsFacade friendsFacade;
    private AuthFacade authFacade;
    private AuthManager authManager;
    private SessionPropertiesFacade sessionPropertiesFacade;
    private SceneManager sceneManager;
    private Session currentSession;
    private User currentUser;

    public FriendListController() {
        this.friendsFacade = FriendsFacade.getInstance();
        this.authFacade = AuthFacade.getInstance();
        this.authManager = AuthManager.getInstance();
        this.sessionPropertiesFacade = SessionPropertiesFacade.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }

    @FXML
    public void initialize() {
        currentSession = authFacade.getCurrentSession();
        currentUser = authManager.getConnectedUser();
        if (currentSession != null) {
            // Load and apply user theme preferences
            sessionPropertiesFacade.loadSettings(currentSession.getUserId());
            javafx.application.Platform.runLater(() -> {
                if (rootPane != null) {
                    sessionPropertiesFacade.applyTheme(rootPane);
                }
            });
            
            loadFriendsList();
        }
    }

    /**
     * Loads all friend-related data.
     */
    private void loadFriendsList() {
        int userId = currentSession.getUserId();
        friendsFacade.loadUserData(userId);
        
        displayFriends();
        displayFollowers();
        displayPendingRequests();
        
        updateCounts();
    }

    /**
     * Displays the friends list.
     */
    private void displayFriends() {
        friendsContainer.getChildren().clear();
        ArrayList<User> friends = friendsFacade.getFriendList();
        
        for (User friend : friends) {
            HBox friendItem = createFriendItem(friend);
            friendsContainer.getChildren().add(friendItem);
        }
        
        if (friends.isEmpty()) {
            Label emptyLabel = new Label("No friends yet");
            emptyLabel.getStyleClass().add("muted-label");
            friendsContainer.getChildren().add(emptyLabel);
        }
    }

    /**
     * Displays the followers list.
     */
    private void displayFollowers() {
        followersContainer.getChildren().clear();
        ArrayList<User> followers = friendsFacade.getFollowedUsers();
        
        for (User follower : followers) {
            HBox followerItem = createFollowerItem(follower);
            followersContainer.getChildren().add(followerItem);
        }
        
        if (followers.isEmpty()) {
            Label emptyLabel = new Label("Not following anyone yet");
            emptyLabel.getStyleClass().add("muted-label");
            followersContainer.getChildren().add(emptyLabel);
        }
    }

    /**
     * Displays pending friend requests.
     */
    private void displayPendingRequests() {
        pendingRequestsContainer.getChildren().clear();
        ArrayList<User> requests = friendsFacade.getPendingFriendRequests();
        
        for (User requester : requests) {
            HBox requestItem = createRequestItem(requester);
            pendingRequestsContainer.getChildren().add(requestItem);
        }
        
        if (requests.isEmpty()) {
            Label emptyLabel = new Label("No pending requests");
            emptyLabel.getStyleClass().add("muted-label");
            pendingRequestsContainer.getChildren().add(emptyLabel);
        }
    }

    /**
     * Creates a UI item for a friend.
     */
    private HBox createFriendItem(User friend) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("friend-item");
        
        Label usernameLabel = new Label(friend.getUsername());
        usernameLabel.getStyleClass().add("title-label");
        HBox.setHgrow(usernameLabel, Priority.ALWAYS);
        
        Button messageBtn = new Button("Message");
        messageBtn.getStyleClass().addAll("button", "primary-button");
        messageBtn.setMinWidth(100);
        messageBtn.setPrefWidth(100);
        messageBtn.setOnAction(e -> handleOpenDM(friend));
        
        Button unfriendBtn = new Button("Unfriend");
        unfriendBtn.getStyleClass().addAll("button", "danger-button");
        unfriendBtn.setMinWidth(100);
        unfriendBtn.setPrefWidth(100);
        unfriendBtn.setOnAction(e -> handleUnfriend(friend.getId()));
        
        item.getChildren().addAll(usernameLabel, messageBtn, unfriendBtn);
        return item;
    }

    /**
     * Creates a UI item for a followed user.
     */
    private HBox createFollowerItem(User follower) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("friend-item");
        
        Label usernameLabel = new Label(follower.getUsername());
        usernameLabel.getStyleClass().add("title-label");
        HBox.setHgrow(usernameLabel, Priority.ALWAYS);
        
        Button unfollowBtn = new Button("Unfollow");
        unfollowBtn.getStyleClass().addAll("button");
        unfollowBtn.setMinWidth(100);
        unfollowBtn.setPrefWidth(100);
        unfollowBtn.setOnAction(e -> handleUnfollow(follower.getId()));
        
        item.getChildren().addAll(usernameLabel, unfollowBtn);
        return item;
    }

    /**
     * Creates a UI item for a friend request.
     */
    private HBox createRequestItem(User requester) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10));
        item.getStyleClass().add("friend-item");
        
        Label usernameLabel = new Label(requester.getUsername());
        usernameLabel.getStyleClass().add("title-label");
        HBox.setHgrow(usernameLabel, Priority.ALWAYS);
        
        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().addAll("button", "primary-button");
        acceptBtn.setMinWidth(100);
        acceptBtn.setPrefWidth(100);
        acceptBtn.setOnAction(e -> handleAcceptRequest(requester.getId()));
        
        Button declineBtn = new Button("Decline");
        declineBtn.getStyleClass().addAll("button", "danger-button");
        declineBtn.setMinWidth(100);
        declineBtn.setPrefWidth(100);
        declineBtn.setOnAction(e -> handleDeclineRequest(requester.getId()));
        
        item.getChildren().addAll(usernameLabel, acceptBtn, declineBtn);
        return item;
    }

    /**
     * Handles unfriending a user.
     */
    private void handleUnfriend(int friendId) {
        boolean success = friendsFacade.removeFriend(currentSession.getUserId(), friendId);
        if (success) {
            loadFriendsList();
        } else {
            showError("Failed to remove friend");
        }
    }

    /**
     * Handles unfollowing a user.
     */
    private void handleUnfollow(int followedId) {
        boolean success = friendsFacade.unfollow(currentSession.getUserId(), followedId);
        if (success) {
            loadFriendsList();
        } else {
            showError("Failed to unfollow user");
        }
    }

    /**
     * Handles accepting a friend request.
     */
    private void handleAcceptRequest(int requesterId) {
        boolean success = friendsFacade.acceptFriendRequest(currentSession.getUserId(), requesterId);
        if (success) {
            loadFriendsList();
        } else {
            showError("Failed to accept friend request");
        }
    }

    /**
     * Handles declining a friend request.
     */
    private void handleDeclineRequest(int requesterId) {
        boolean success = friendsFacade.declineFriendRequest(currentSession.getUserId(), requesterId);
        if (success) {
            loadFriendsList();
        } else {
            showError("Failed to decline friend request");
        }
    }

    /**
     * Updates the count labels.
     */
    private void updateCounts() {
        int userId = currentSession.getUserId();
        int friendCount = friendsFacade.getFriendCount(userId);
        int pendingCount = friendsFacade.getPendingFriendRequests().size();
        
        friendsCountLabel.setText("Friends (" + friendCount + ")");
        followersCountLabel.setText("Following (" + friendsFacade.getFollowedUsers().size() + ")");
        pendingCountLabel.setText("Pending Requests (" + pendingCount + ")");
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
     * Handles blocking a user.
     */
    @FXML
    public void handleBlock(int userId) {
        boolean success = friendsFacade.block(currentSession.getUserId(), userId);
        if (success) {
            loadFriendsList();
        } else {
            showError("Failed to block user");
        }
    }

    /**
     * Handles unblocking a user.
     */
    @FXML
    public void handleUnblock(int userId) {
        boolean success = friendsFacade.unblock(currentSession.getUserId(), userId);
        if (success) {
            loadFriendsList();
        } else {
            showError("Failed to unblock user");
        }
    }

    /**
     * Handles opening a DM channel with a friend
     */
    private void handleOpenDM(User friend) {
        try {
            // Get the channel ID for this friendship
            int channelId = friendsFacade.getChannelIdForFriendship(currentUser.getId(), friend.getId());
            
            if (channelId <= 0) {
                showError("Cannot open DM: Channel not found or friendship not accepted");
                return;
            }
            
            // Open TextChannelController as a new window and get its controller
            Object controller = sceneManager.openNewWindowWithController(
                "/fr/opal/textchannel-view.fxml",
                "DM with " + friend.getUsername(),
                600,
                500
            );
            
            // Initialize the controller with channel and friend info
            if (controller instanceof fr.opal.controller.TextChannelController) {
                ((fr.opal.controller.TextChannelController) controller).setChannelAndFriend(channelId, friend);
            }
        } catch (IOException e) {
            showError("Error opening DM: " + e.getMessage());
        }
    }
}
