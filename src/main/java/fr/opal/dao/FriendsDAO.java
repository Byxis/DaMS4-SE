package fr.opal.dao;

import fr.opal.type.User;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Abstract Data Access Object for managing friend relationships between users.
 */
public abstract class FriendsDAO {

    /**
     * Retrieves the list of friends for a given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing the user's friends
     * @throws SQLException if a database error occurs
     */
    public abstract ArrayList<User> getFriendList(int userId) throws SQLException;

    /**
     * Retrieves the list of users that the given user is following.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing followed users
     * @throws SQLException if a database error occurs
     */
    public abstract ArrayList<User> getFollowedUsers(int userId) throws SQLException;

    /**
     * Retrieves the list of users following the given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing followers
     * @throws SQLException if a database error occurs
     */
    public abstract ArrayList<User> getFollowersList(int userId) throws SQLException;

    /**
     * Retrieves the list of blocked users for a given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing blocked users
     * @throws SQLException if a database error occurs
     */
    public abstract ArrayList<User> getBlockedUsers(int userId) throws SQLException;

    /**
     * Retrieves pending friend requests for a given user.
     *
     * @param userId The ID of the user
     * @return ArrayList of User objects representing users who sent friend requests
     * @throws SQLException if a database error occurs
     */
    public abstract ArrayList<User> getPendingFriendRequests(int userId) throws SQLException;

    /**
     * Sends a friend request from one user to another.
     *
     * @param fromUserId The ID of the user sending the request
     * @param toUserId The ID of the user receiving the request
     * @throws SQLException if a database error occurs
     */
    public abstract void addFriend(int fromUserId, int toUserId) throws SQLException;

    /**
     * Removes a friend relationship between two users.
     *
     * @param userId The ID of the first user
     * @param friendId The ID of the second user
     * @throws SQLException if a database error occurs
     */
    public abstract void removeFriend(int userId, int friendId) throws SQLException;

    /**
     * Adds a follow relationship from one user to another (unidirectional).
     *
     * @param followerId The ID of the user who is following
     * @param followedId The ID of the user being followed
     * @throws SQLException if a database error occurs
     */
    public abstract void follow(int followerId, int followedId) throws SQLException;

    /**
     * Removes a follow relationship.
     *
     * @param followerId The ID of the user who is following
     * @param followedId The ID of the user being followed
     * @throws SQLException if a database error occurs
     */
    public abstract void unfollow(int followerId, int followedId) throws SQLException;

    /**
     * Blocks a user.
     *
     * @param userId The ID of the user doing the blocking
     * @param blockedUserId The ID of the user being blocked
     * @throws SQLException if a database error occurs
     */
    public abstract void block(int userId, int blockedUserId) throws SQLException;

    /**
     * Unblocks a user.
     *
     * @param userId The ID of the user doing the unblocking
     * @param blockedUserId The ID of the user being unblocked
     * @throws SQLException if a database error occurs
     */
    public abstract void unblock(int userId, int blockedUserId) throws SQLException;

    /**
     * Gets the count of friends for a user.
     *
     * @param userId The ID of the user
     * @return The number of friends
     * @throws SQLException if a database error occurs
     */
    public abstract int getFriendCount(int userId) throws SQLException;

    /**
     * Gets the count of followers for a user.
     *
     * @param userId The ID of the user
     * @return The number of followers
     * @throws SQLException if a database error occurs
     */
    public abstract int getFollowerCount(int userId) throws SQLException;

    /**
     * Searches for users by username.
     *
     * @param query The search query
     * @return ArrayList of User objects matching the query
     * @throws SQLException if a database error occurs
     */
    public abstract ArrayList<User> searchUsers(String query) throws SQLException;

    /**
     * Checks if a user has blocked another user.
     *
     * @param userId The ID of the user
     * @param otherUserId The ID of the other user
     * @return true if userId has blocked otherUserId
     * @throws SQLException if a database error occurs
     */
    public abstract boolean isBlocked(int userId, int otherUserId) throws SQLException;

    /**
     * Checks if a user is friends with another user.
     *
     * @param userId The ID of the first user
     * @param otherUserId The ID of the second user
     * @return true if they are friends
     * @throws SQLException if a database error occurs
     */
    public abstract boolean isFriend(int userId, int otherUserId) throws SQLException;

    /**
     * Checks if a user is following another user.
     *
     * @param followerId The ID of the potential follower
     * @param followedId The ID of the potential followed user
     * @return true if followerId is following followedId
     * @throws SQLException if a database error occurs
     */
    public abstract boolean isFollowing(int followerId, int followedId) throws SQLException;

    /**
     * Checks if there is a pending friend request from one user to another.
     *
     * @param fromUserId The ID of the user who sent the request
     * @param toUserId The ID of the user who received the request
     * @return true if there is a pending request
     * @throws SQLException if a database error occurs
     */
    public abstract boolean hasPendingFriendRequest(int fromUserId, int toUserId) throws SQLException;

    /**
     * Accepts a pending friend request.
     *
     * @param requesterId The ID of the user who sent the request
     * @param userId The ID of the user accepting the request
     * @throws SQLException if a database error occurs
     */
    public abstract void acceptFriendRequest(int requesterId, int userId) throws SQLException;
}
