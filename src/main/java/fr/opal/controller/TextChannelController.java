package fr.opal.controller;

import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.facade.ChannelFacade;
import fr.opal.type.User;
import fr.opal.type.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

/**
 * Controller for text channel messaging UI
 * Strictly uses Facades only - no direct access to Managers or DAOs
 */
public class TextChannelController {
    @FXML
    private VBox rootPane;
    
    @FXML
    private Label channelTitle;
    
    @FXML
    private Label channelDescription;
    
    @FXML
    private ListView<Message> messagesList;
    
    @FXML
    private TextArea messageInput;
    
    @FXML
    private Label currentAuthorLabel;
    
    @FXML
    private Button sendMessageBtn;

    private SessionPropertiesFacade sessionPropertiesFacade;
    private ChannelFacade channelFacade;
    private User currentUser;
    private int currentChannelId;

    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        sessionPropertiesFacade = SessionPropertiesFacade.getInstance();
        channelFacade = ChannelFacade.getInstance();
        
        // Get current user from facade
        currentUser = channelFacade.getAuthenticatedUser();
        
        // Load and apply session settings (theme/font size)
        if (currentUser != null) {
            sessionPropertiesFacade.loadSettings(currentUser.getId());
            Platform.runLater(() -> {
                if (rootPane != null) {
                    sessionPropertiesFacade.applyTheme(rootPane);
                }
            });
            
            currentAuthorLabel.setText(currentUser.getUsername());
            
            // Initialize message list cell factory
            messagesList.setCellFactory(param -> new ListCell<Message>() {
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
        }
    }

    /**
     * Sets the channel ID and friend info, then loads messages
     */
    public void setChannelAndFriend(int channelId, User friend) {
        this.currentChannelId = channelId;
        
        // Update the title to show who we're chatting with
        if (friend != null) {
            channelTitle.setText("DM with " + friend.getUsername());
            channelDescription.setText("Private conversation with " + friend.getUsername());
        }
        
        loadChannelMessages();
    }

    /**
     * Load messages for the current channel
     */
    private void loadChannelMessages() {
        if (currentChannelId <= 0) {
            return; // Channel not yet set
        }
        
        try {
            messagesList.getItems().clear();
            List<Message> messages = channelFacade.getMessages(currentChannelId);
            messagesList.getItems().addAll(messages);
            
            // Scroll to latest message
            if (!messages.isEmpty()) {
                messagesList.scrollTo(messages.size() - 1);
            }
        } catch (ChannelFacade.InvalidChannelException e) {
            showErrorDialog("Error loading messages", e.getMessage());
        }
    }

    /**
     * Handles sending a message (Auto-Save)
     */
    @FXML
    public void onSendMessage() {
        String messageText = messageInput.getText().trim();
        
        try {
            // Delegate all validation and message creation to facade
            channelFacade.sendMessage(currentChannelId, currentUser, messageText);
            
            // Clear input and refresh display
            messageInput.clear();
            loadChannelMessages();
        } catch (ChannelFacade.AuthenticationException e) {
            showErrorDialog("Error", e.getMessage());
        } catch (ChannelFacade.InvalidChannelException e) {
            showErrorDialog("Error", e.getMessage());
        } catch (ChannelFacade.MessageException e) {
            showErrorDialog("Error", e.getMessage());
        }
    }

    @FXML
    public void clearInput() {
        messageInput.clear();
    }

    /**
     * Closes the text channel view
     */
    @FXML
    public void closeChannel() {
        try {
            // Close the current window by getting the stage from sendMessageBtn (guaranteed to be initialized)
            Stage stage = (Stage) sendMessageBtn.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows an error dialog
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
