package fr.opal.dao;

import fr.opal.type.User;
import java.util.ArrayList;

/**
 * Interface definition for managing friend relationships between users.
 */
public abstract class FriendsDAO {

    /**
     * Retrieves the list of friends for a given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing the user's friends
     */
    public abstract ArrayList<User> getFriendList(int userId);

    /**
     * Retrieves the list of users that the given user is following.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing followed users
     */
    public abstract ArrayList<User> getFollowedUsers(int userId);

    /**
     * Retrieves the list of users following the given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing followers
     */
    public abstract ArrayList<User> getFollowersList(int userId);

    /**
     * Retrieves the list of blocked users for a given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing blocked users
     */
    public abstract ArrayList<User> getBlockedUsers(int userId);

    /**
     * Retrieves pending friend requests for a given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing users who sent friend requests
     */
    public abstract ArrayList<User> getPendingFriendRequests(int userId);

    /**
     * Sends a friend request from one user to another.
     *
     * @param fromUserId The ID of the user sending the request
     * @param toUserId The ID of the user receiving the request
     */
    public abstract void addFriend(int fromUserId, int toUserId);

    /**
     * Removes a friend relationship between two users.
     *
     * @param userId The ID of the first user
     * @param friendId The ID of the second user
     */
    public abstract void removeFriend(int userId, int friendId);

    /**
     * Adds a follow relationship from one user to another (unidirectional).
     *
     * @param followerId The ID of the user who is following
     * @param followedId The ID of the user being followed
     */
    public abstract void follow(int followerId, int followedId);

    /**
     * Removes a follow relationship.
     *
     * @param followerId The ID of the user who is following
     * @param followedId The ID of the user being followed
     */
    public abstract void unfollow(int followerId, int followedId);

    /**
     * Blocks a user.
     *
     * @param userId The ID of the user doing the blocking
     * @param blockedUserId The ID of the user being blocked
     */
    public abstract void block(int userId, int blockedUserId);

    /**
     * Unblocks a user.
     *
     * @param userId The ID of the user doing the unblocking
     * @param blockedUserId The ID of the user being unblocked
     */
    public abstract void unblock(int userId, int blockedUserId);

    /**
     * Gets the count of friends for a user.
     *
     * @param userId The ID of the user
     * @return The number of friends
     */
    public abstract int getFriendCount(int userId);

    /**
     * Gets the count of followers for a user.
     *
     * @param userId The ID of the user
     * @return The number of followers
     */
    public abstract int getFollowerCount(int userId);

    /**
     * Searches for users by username.
     *
     * @param query The search query
     * @return ArrayList of User objects matching the query
     */
    public abstract ArrayList<User> searchUsers(String query);

    /**
     * Checks if a user has blocked another user.
     *
     * @param userId The ID of the user
     * @param otherUserId The ID of the other user
     * @return true if userId has blocked otherUserId
     */
    public abstract boolean isBlocked(int userId, int otherUserId);

    /**
     * Checks if a user is friends with another user.
     *
     * @param userId The ID of the first user
     * @param otherUserId The ID of the second user
     * @return true if they are friends
     */
    public abstract boolean isFriend(int userId, int otherUserId);

    /**
     * Checks if a user is following another user.
     *
     * @param followerId The ID of the potential follower
     * @param followedId The ID of the potential followed user
     * @return true if followerId is following followedId
     */
    public abstract boolean isFollowing(int followerId, int followedId);

    /**
     * Checks if there is a pending friend request from one user to another.
     *
     * @param fromUserId The ID of the user who sent the request
     * @param toUserId The ID of the user who received the request
     * @return true if there is a pending request
     */
    public abstract boolean hasPendingFriendRequest(int fromUserId, int toUserId);

    /**
     * Accepts a pending friend request.
     *
     * @param requesterId The ID of the user who sent the request
     * @param userId The ID of the user accepting the request
     */
    public abstract void acceptFriendRequest(int requesterId, int userId);
}
