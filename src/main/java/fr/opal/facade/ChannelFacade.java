package fr.opal.facade;

import fr.opal.service.ChannelManager;
import fr.opal.type.Channel;
import fr.opal.type.Message;
import fr.opal.type.User;

import java.util.List;

/**
 * Facade for channel and messaging operations
 * Strict interface between Controller (UI) and Manager (Business) layers
 * Hides complexity of underlying Managers from the Controller
 * Controllers must ONLY interact with this facade - never directly with Managers or DAOs
 */
public class ChannelFacade {

    private static ChannelFacade instance;
    private ChannelManager manager;

    /**
     * Private constructor for singleton pattern
     */
    private ChannelFacade() {
        this.manager = new ChannelManager();
    }

    /**
     * Gets the singleton instance
     */
    public static ChannelFacade getInstance() {
        if (instance == null) {
            instance = new ChannelFacade();
        }
        return instance;
    }

    // ==================== Authentication Delegation ====================

    /**
     * Gets the currently authenticated user
     * Delegates to AuthFacade
     */
    public User getAuthenticatedUser() {
        return AuthFacade.getInstance().getConnectedUser();
    }

    /**
     * Checks if a user is authenticated
     */
    public boolean isUserAuthenticated() {
        return AuthFacade.getInstance().isAuthenticated();
    }

    // ==================== Channel Operations ====================

    /**
     * Creates a new channel
     * @return the channel ID
     */
    public int createChannel() {
        return manager.createChannel();
    }

    /**
     * Gets a channel by ID
     */
    public Channel getChannel(int channelId) {
        return manager.getChannel(channelId);
    }

    /**
     * Deletes a channel
     */
    public void deleteChannel(int channelId) {
        manager.deleteChannel(channelId);
    }

    // ==================== Message Operations ====================

    /**
     * Gets all messages for a channel
     * @throws InvalidChannelException if channel is not valid
     */
    public List<Message> getMessages(int channelId) throws InvalidChannelException {
        try {
            return manager.getMessagesForChannel(channelId);
        } catch (IllegalArgumentException e) {
            throw new InvalidChannelException(e.getMessage());
        }
    }

    /**
     * Gets recent messages for a channel
     * @throws InvalidChannelException if channel is not valid
     */
    public List<Message> getRecentMessages(int channelId, int limit) throws InvalidChannelException {
        try {
            return manager.getRecentMessages(channelId, limit);
        } catch (IllegalArgumentException e) {
            throw new InvalidChannelException(e.getMessage());
        }
    }

    /**
     * Sends a message to a channel
     * Delegates all validation and business logic to the manager
     * @throws MessageException if message cannot be sent
     * @throws AuthenticationException if user is not authenticated
     * @throws InvalidChannelException if channel is not valid
     */
    public Message sendMessage(int channelId, User sender, String content) 
            throws MessageException, AuthenticationException, InvalidChannelException {
        try {
            return manager.sendMessageWithFullValidation(channelId, sender, content);
        } catch (ChannelManager.MessageValidationException e) {
            throw new MessageException(e.getMessage());
        } catch (ChannelManager.AuthenticationException e) {
            throw new AuthenticationException(e.getMessage());
        } catch (ChannelManager.InvalidChannelException e) {
            throw new InvalidChannelException(e.getMessage());
        }
    }

    /**
     * Deletes a message
     */
    public void deleteMessage(long messageId) {
        manager.deleteMessage(messageId);
    }

    /**
     * Updates a message's content
     * @throws MessageException if update fails
     */
    public void updateMessage(long messageId, String newContent) throws MessageException {
        try {
            manager.updateMessage(messageId, newContent);
        } catch (ChannelManager.MessageValidationException e) {
            throw new MessageException(e.getMessage());
        }
    }

    /**
     * Gets the message count for a channel
     * Delegates validation to manager
     */
    public int getMessageCount(int channelId) throws InvalidChannelException {
        try {
            return manager.getMessageCountWithValidation(channelId);
        } catch (ChannelManager.InvalidChannelException e) {
            throw new InvalidChannelException(e.getMessage());
        }
    }

    // ==================== Exception Classes ====================

    /**
     * Exception for message-related errors
     */
    public static class MessageException extends Exception {
        public MessageException(String message) {
            super(message);
        }
    }

    /**
     * Exception for authentication errors
     */
    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    /**
     * Exception for invalid channel errors
     */
    public static class InvalidChannelException extends Exception {
        public InvalidChannelException(String message) {
            super(message);
        }
    }
}
