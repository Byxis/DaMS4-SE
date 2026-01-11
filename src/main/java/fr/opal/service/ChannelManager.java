package fr.opal.service;

import fr.opal.dao.ChannelDAO;
import fr.opal.factory.AbstractDAOFactory;
import fr.opal.type.Channel;
import fr.opal.type.Message;
import fr.opal.type.User;

import java.util.List;

/**
 * Channel Manager Service
 * Contains channel and message-related business logic
 * Owns the complexity: message validation, channel operations
 * Delegates persistence to DAO
 * Part of the unified channel architecture
 */
public class ChannelManager {

    private ChannelDAO channelDAO;
    private User currentUser;

    /**
     * Constructor
     */
    public ChannelManager() {
        this.channelDAO = AbstractDAOFactory.getFactory().createChannelDAO();
        this.currentUser = null;
    }

    /**
     * Sets the current user for operations
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    // ==================== Channel Operations ====================

    /**
     * Creates a new channel
     * @return the channel ID
     */
    public int createChannel() {
        return channelDAO.createChannel();
    }

    /**
     * Gets a channel by ID
     */
    public Channel getChannel(int channelId) {
        return channelDAO.getChannelById(channelId);
    }

    /**
     * Deletes a channel and all its messages
     */
    public void deleteChannel(int channelId) {
        channelDAO.deleteChannel(channelId);
    }

    // ==================== Message Operations ====================

    /**
     * Gets all messages for a channel
     */
    public List<Message> getMessagesForChannel(int channelId) {
        validateChannelId(channelId);
        return channelDAO.getMessagesForChannel(channelId);
    }

    /**
     * Gets recent messages for a channel with a limit
     */
    public List<Message> getRecentMessages(int channelId, int limit) {
        validateChannelId(channelId);
        if (limit <= 0) {
            throw new IllegalArgumentException("Message limit must be positive");
        }
        return channelDAO.getRecentMessages(channelId, limit);
    }

    /**
     * Sends a message to a channel
     * Business logic: validates message content, creates message, persists
     * @throws MessageValidationException if message is invalid
     */
    public Message sendMessage(int channelId, User sender, String content) throws MessageValidationException {
        // Validate inputs
        validateChannelId(channelId);
        
        if (sender == null) {
            throw new MessageValidationException("Sender cannot be null");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new MessageValidationException("Message cannot be empty");
        }
        
        // Create and persist message
        Message message = new Message(channelId, sender, content.trim());
        long messageId = channelDAO.saveMessage(message);
        message.setId(messageId);
        
        return message;
    }

    /**
     * Sends a message with full validation including auth and channel checks
     * Business logic: validates all inputs before sending
     * @throws MessageValidationException if message content is invalid
     * @throws AuthenticationException if sender is not authenticated
     * @throws InvalidChannelException if channel is not valid
     */
    public Message sendMessageWithFullValidation(int channelId, User sender, String content) 
            throws MessageValidationException, AuthenticationException, InvalidChannelException {
        
        // Validate authentication
        if (sender == null) {
            throw new AuthenticationException("User not authenticated");
        }
        
        // Validate channel
        if (channelId <= 0) {
            throw new InvalidChannelException("Channel not selected");
        }
        
        // Validate content
        if (content == null || content.trim().isEmpty()) {
            throw new MessageValidationException("Message cannot be empty");
        }
        
        // Create and persist message
        Message message = new Message(channelId, sender, content.trim());
        long messageId = channelDAO.saveMessage(message);
        message.setId(messageId);
        
        return message;
    }

    /**
     * Deletes a message
     * Business logic: could add permission checks here
     */
    public void deleteMessage(long messageId) {
        channelDAO.deleteMessage(messageId);
    }

    /**
     * Updates message content
     * @throws MessageValidationException if new content is invalid
     */
    public void updateMessage(long messageId, String newContent) throws MessageValidationException {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new MessageValidationException("Message content cannot be empty");
        }
        channelDAO.updateMessageContent(messageId, newContent.trim());
    }

    /**
     * Gets the message count for a channel
     */
    public int getMessageCount(int channelId) {
        validateChannelId(channelId);
        return channelDAO.getMessageCount(channelId);
    }

    /**
     * Gets the message count for a channel with validation
     * @throws InvalidChannelException if channel is not valid
     */
    public int getMessageCountWithValidation(int channelId) throws InvalidChannelException {
        if (channelId <= 0) {
            throw new InvalidChannelException("Channel not selected");
        }
        return channelDAO.getMessageCount(channelId);
    }

    // ==================== Validation Helpers ====================

    /**
     * Validates that a channel ID is valid
     */
    private void validateChannelId(int channelId) {
        if (channelId <= 0) {
            throw new IllegalArgumentException("Invalid channel ID: " + channelId);
        }
    }

    // ==================== Exception Classes ====================

    /**
     * Exception for message validation errors
     */
    public static class MessageValidationException extends Exception {
        public MessageValidationException(String message) {
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
