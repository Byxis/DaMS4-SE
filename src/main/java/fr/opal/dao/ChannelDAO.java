package fr.opal.dao;

import fr.opal.type.Channel;
import fr.opal.type.Message;
import java.util.List;

/**
 * Abstract DAO for Channel and Message persistence operations.
 * Part of the unified channel architecture where channels are generic
 * containers and context is derived from the owning entity (Entry or Friendship).
 */
public abstract class ChannelDAO {

    // ==================== Channel Operations ====================

    /**
     * Creates a new channel in the database
     * @return the generated channel ID
     */
    public abstract int createChannel();

    /**
     * Retrieves a channel by its ID
     */
    public abstract Channel getChannelById(int id);

    /**
     * Deletes a channel from the database (cascades to messages)
     */
    public abstract void deleteChannel(int id);

    // ==================== Message Operations ====================

    /**
     * Retrieves all messages for a channel, ordered by creation date ascending
     */
    public abstract List<Message> getMessagesForChannel(int channelId);

    /**
     * Retrieves a limited number of recent messages for a channel
     * @param channelId the channel ID
     * @param limit maximum number of messages to return
     * @return messages ordered by creation date descending (most recent first)
     */
    public abstract List<Message> getRecentMessages(int channelId, int limit);

    /**
     * Saves a new message to the database
     * @return the generated message ID
     */
    public abstract long saveMessage(Message message);

    /**
     * Deletes a message from the database
     */
    public abstract void deleteMessage(long messageId);

    /**
     * Updates the content of an existing message
     */
    public abstract void updateMessageContent(long messageId, String newContent);

    /**
     * Gets the total message count for a channel
     */
    public abstract int getMessageCount(int channelId);
}
