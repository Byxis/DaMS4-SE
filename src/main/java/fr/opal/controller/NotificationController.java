package fr.opal.controller;

import fr.opal.facade.AuthFacade;
import fr.opal.facade.NotificationFacade;
import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.type.ENotifStatus;
import fr.opal.type.ENotifType;
import fr.opal.type.Notification;
import fr.opal.type.Session;
import fr.opal.util.ColorUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Controller for the notifications view
 * Handles display and management of user notifications
 */
public class NotificationController {

    @FXML
    private VBox rootContainer;
    @FXML
    private VBox notificationsContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private Label unreadCountLabel;
    @FXML
    private Button markAllReadBtn;
    @FXML
    private ScrollPane scrollPane;

    private NotificationFacade notificationFacade;
    private SessionPropertiesFacade sessionPropertiesFacade;
    private AuthFacade authFacade;
    private Session currentSession;
    private Runnable onNotificationChanged;

    /**
     * Default constructor
     */
    public NotificationController() {
        this.notificationFacade = NotificationFacade.getInstance();
        this.sessionPropertiesFacade = SessionPropertiesFacade.getInstance();
        this.authFacade = AuthFacade.getInstance();
    }

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        currentSession = authFacade.getCurrentSession();
        
        if (currentSession != null) {
            loadNotifications();
            updateUnreadCount();
        }
        
        // Apply theme
        if (rootContainer != null) {
            sessionPropertiesFacade.applyTheme(rootContainer);
        }
    }

    /**
     * Load and display notifications
     */
    private void loadNotifications() {
        if (notificationsContainer == null || currentSession == null) {
            return;
        }
        
        notificationsContainer.getChildren().clear();
        
        List<Notification> notifications = notificationFacade.listNotifications(currentSession.getUserId());
        
        if (notifications == null || notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications yet.");
            emptyLabel.getStyleClass().add("empty-message");
            notificationsContainer.getChildren().add(emptyLabel);
        } else {
            for (Notification notification : notifications) {
                VBox notificationCard = createNotificationCard(notification);
                notificationsContainer.getChildren().add(notificationCard);
            }
        }
    }

    /**
     * Create a notification card UI component
     *
     * @param notification the notification to display
     * @return the notification card VBox
     */
    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(8);
        card.getStyleClass().add("notification-card");
        
        // Add unread style if notification is unread
        if (notification.getStatus() == ENotifStatus.TO_READ) {
            card.getStyleClass().add("unread");
        } else if (notification.getStatus() == ENotifStatus.HIDDEN) {
            card.getStyleClass().add("hidden");
        }
        
        card.setPadding(new Insets(12));
        
        // Header with type icon and date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Type icon
        Label typeIcon = new Label(getTypeIcon(notification.getType()));
        typeIcon.getStyleClass().add("notification-icon");
        typeIcon.setStyle("-fx-font-size: 18;");
        
        // Type label
        Label typeLabel = new Label(getTypeLabel(notification.getType()));
        typeLabel.getStyleClass().add("notification-type");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(sdf.format(notification.getCreationDate()));
        dateLabel.getStyleClass().add("notification-date");
        
        header.getChildren().addAll(typeIcon, typeLabel, spacer, dateLabel);
        
        // Content
        Label contentLabel = new Label(notification.getContent());
        contentLabel.getStyleClass().add("notification-content");
        contentLabel.setWrapText(true);
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));
        
        // Mark as read button (only for unread)
        if (notification.getStatus() == ENotifStatus.TO_READ) {
            Button markReadBtn = new Button("✓ Mark as Read");
            markReadBtn.getStyleClass().add("action-button");
            markReadBtn.setOnAction(e -> markAsRead(notification.getId()));
            actions.getChildren().add(markReadBtn);
        }
        
        // Hide button (only for non-hidden)
        if (notification.getStatus() != ENotifStatus.HIDDEN) {
            Button hideBtn = new Button("👁 Hide");
            hideBtn.getStyleClass().add("action-button-secondary");
            hideBtn.setOnAction(e -> hideNotification(notification.getId()));
            actions.getChildren().add(hideBtn);
        }
        
        // Delete button
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("action-button-danger");
        deleteBtn.setOnAction(e -> deleteNotification(notification.getId()));
        actions.getChildren().add(deleteBtn);
        
        card.getChildren().addAll(header, contentLabel, actions);
        
        return card;
    }

    /**
     * Get icon for notification type
     *
     * @param type the notification type
     * @return the icon string
     */
    private String getTypeIcon(ENotifType type) {
        switch (type) {
            case PROJECT:
                return "📁";
            case SOCIAL:
                return "👥";
            case INVITATION:
                return "📬";
            case COMMENT:
                return "💬";
            case GENERAL:
            default:
                return "🔔";
        }
    }

    /**
     * Get label for notification type
     *
     * @param type the notification type
     * @return the label string
     */
    private String getTypeLabel(ENotifType type) {
        switch (type) {
            case PROJECT:
                return "Project";
            case SOCIAL:
                return "Social";
            case INVITATION:
                return "Invitation";
            case COMMENT:
                return "Comment";
            case GENERAL:
            default:
                return "General";
        }
    }

    /**
     * Mark a notification as read
     *
     * @param notificationId the notification ID
     */
    private void markAsRead(int notificationId) {
        try {
            notificationFacade.markAsRead(notificationId);
            loadNotifications();
            updateUnreadCount();
            notifyChange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hide a notification
     *
     * @param notificationId the notification ID
     */
    private void hideNotification(int notificationId) {
        try {
            notificationFacade.hideNotification(notificationId);
            loadNotifications();
            updateUnreadCount();
            notifyChange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a notification
     *
     * @param notificationId the notification ID
     */
    private void deleteNotification(int notificationId) {
        try {
            notificationFacade.deleteNotification(notificationId);
            loadNotifications();
            updateUnreadCount();
            notifyChange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mark all notifications as read
     */
    @FXML
    private void markAllAsRead() {
        if (currentSession != null) {
            try {
                notificationFacade.markAllAsRead(currentSession.getUserId());
                loadNotifications();
                updateUnreadCount();
                notifyChange();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update the unread count label
     */
    private void updateUnreadCount() {
        if (unreadCountLabel != null && currentSession != null) {
            int count = notificationFacade.getUnreadCount(currentSession.getUserId());
            if (count > 0) {
                unreadCountLabel.setText(count + " unread");
                unreadCountLabel.setVisible(true);
            } else {
                unreadCountLabel.setText("All read");
                unreadCountLabel.setVisible(true);
            }
        }
    }

    /**
     * Get unread notification count for current user
     *
     * @return the unread count
     */
    public int getUnreadCount() {
        if (currentSession != null) {
            return notificationFacade.getUnreadCount(currentSession.getUserId());
        }
        return 0;
    }

    /**
     * Refresh notifications display
     */
    public void refresh() {
        loadNotifications();
        updateUnreadCount();
    }

    /**
     * Set callback for when notifications change
     *
     * @param callback the callback runnable
     */
    public void setOnNotificationChanged(Runnable callback) {
        this.onNotificationChanged = callback;
    }

    /**
     * Notify that notifications have changed
     */
    private void notifyChange() {
        if (onNotificationChanged != null) {
            onNotificationChanged.run();
        }
    }
}
