package fr.opal.facade;

import fr.opal.manager.FriendsManager;
import fr.opal.type.User;

import java.util.ArrayList;

/**
 * Facade class that provides a simplified interface for friend-related operations.
 */
public class FriendsFacade {

    private static FriendsFacade instance;
    private final FriendsManager friendsManager;

    private FriendsFacade() {
        this.friendsManager = FriendsManager.getInstance();
    }

    /**
     * Gets the singleton instance of FriendsFacade.
     *
     * @return The FriendsFacade instance
     */
    public static FriendsFacade getInstance() {
        if (instance == null) {
            instance = new FriendsFacade();
        }
        return instance;
    }

    /**
     * Initializes friend data for a user.
     *
     * @param userId The ID of the user
     */
    public void loadUserData(int userId) {
        friendsManager.loadUserData(userId);
    }

    /**
     * Gets the friend list for the current user.
     *
     * @return ArrayList of User objects
     */
    public ArrayList<User> getFriendList() {
        return friendsManager.getFriendList();
    }

    /**
     * Gets the followed users list.
     *
     * @return ArrayList of User objects
     */
    public ArrayList<User> getFollowedUsers() {
        return friendsManager.getFollowedUsers();
    }

    /**
     * Gets the blocked users list.
     *
     * @return ArrayList of User objects
     */
    public ArrayList<User> getBlockedUsers() {
        return friendsManager.getBlockedUsers();
    }

    /**
     * Gets pending friend requests.
     *
     * @return ArrayList of User objects
     */
    public ArrayList<User> getPendingFriendRequests() {
        return friendsManager.getPendingFriendRequests();
    }

    /**
     * Sends a friend request to another user.
     *
     * @param fromUserId The sender's user ID
     * @param toUserId The recipient's user ID
     * @return true if successful
     */
    public boolean sendFriendRequest(int fromUserId, int toUserId) {
        return friendsManager.addFriend(fromUserId, toUserId);
    }

    /**
     * Accepts a friend request.
     *
     * @param userId The ID of the user accepting
     * @param requesterId The ID of the requester
     * @return true if successful
     */
    public boolean acceptFriendRequest(int userId, int requesterId) {
        return friendsManager.acceptFriendRequest(userId, requesterId);
    }

    /**
     * Declines a friend request.
     *
     * @param userId The ID of the user declining
     * @param requesterId The ID of the requester
     * @return true if successful
     */
    public boolean declineFriendRequest(int userId, int requesterId) {
        return friendsManager.declineFriendRequest(userId, requesterId);
    }

    /**
     * Removes a friend.
     *
     * @param userId The user's ID
     * @param friendId The friend's ID
     * @return true if successful
     */
    public boolean removeFriend(int userId, int friendId) {
        return friendsManager.removeFriend(userId, friendId);
    }

    /**
     * Follows a user.
     *
     * @param followerId The follower's ID
     * @param followedId The followed user's ID
     * @return true if successful
     */
    public boolean follow(int followerId, int followedId) {
        return friendsManager.follow(followerId, followedId);
    }

    /**
     * Unfollows a user.
     *
     * @param followerId The follower's ID
     * @param followedId The followed user's ID
     * @return true if successful
     */
    public boolean unfollow(int followerId, int followedId) {
        return friendsManager.unfollow(followerId, followedId);
    }

    /**
     * Blocks a user.
     *
     * @param userId The user's ID
     * @param blockedUserId The blocked user's ID
     * @return true if successful
     */
    public boolean block(int userId, int blockedUserId) {
        return friendsManager.block(userId, blockedUserId);
    }

    /**
     * Unblocks a user.
     *
     * @param userId The user's ID
     * @param blockedUserId The blocked user's ID
     * @return true if successful
     */
    public boolean unblock(int userId, int blockedUserId) {
        return friendsManager.unblock(userId, blockedUserId);
    }

    /**
     * Gets the friend count for a user.
     *
     * @param userId The user's ID
     * @return The friend count
     */
    public int getFriendCount(int userId) {
        return friendsManager.getFriendCount(userId);
    }

    /**
     * Gets the follower count for a user.
     *
     * @param userId The user's ID
     * @return The follower count
     */
    public int getFollowerCount(int userId) {
        return friendsManager.getFollowerCount(userId);
    }

    /**
     * Searches for users by username.
     *
     * @param query The search query
     * @return ArrayList of matching users
     */
    public ArrayList<User> searchUsers(String query) {
        return friendsManager.searchUsers(query);
    }

    /**
     * Checks if a user is blocked.
     *
     * @param userId The user's ID
     * @param otherUserId The other user's ID
     * @return true if blocked
     */
    public boolean isBlocked(int userId, int otherUserId) {
        return friendsManager.isBlocked(userId, otherUserId);
    }

    /**
     * Checks if users are friends.
     *
     * @param userId The first user's ID
     * @param otherUserId The second user's ID
     * @return true if friends
     */
    public boolean isFriend(int userId, int otherUserId) {
        return friendsManager.isFriend(userId, otherUserId);
    }

    /**
     * Checks if a user is following another.
     *
     * @param followerId The follower's ID
     * @param followedId The followed user's ID
     * @return true if following
     */
    public boolean isFollowing(int followerId, int followedId) {
        return friendsManager.isFollowing(followerId, followedId);
    }

    /**
     * Checks if there's a pending friend request.
     *
     * @param fromUserId The sender's ID
     * @param toUserId The recipient's ID
     * @return true if pending
     */
    public boolean hasPendingFriendRequest(int fromUserId, int toUserId) {
        return friendsManager.hasPendingFriendRequest(fromUserId, toUserId);
    }

    /**
     * Gets the channel ID for DMs between two friends.
     *
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     * @return The channel ID for DMs, or 0 if friendship doesn't exist or isn't accepted
     */
    public int getChannelIdForFriendship(int user1Id, int user2Id) {
        return friendsManager.getChannelIdForFriendship(user1Id, user2Id);
    }
}
