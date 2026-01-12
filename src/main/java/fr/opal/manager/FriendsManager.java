package fr.opal.manager;

import fr.opal.dao.FriendsDAO;
import fr.opal.factory.AbstractDAOFactory;
import fr.opal.type.User;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Manager class for handling friend-related business logic.
 */
public class FriendsManager {

    private static FriendsManager instance;
    private final FriendsDAO friendsDAO;
    private ArrayList<User> friendList;
    private ArrayList<User> followedUsers;
    private ArrayList<User> blockedUsers;
    private ArrayList<User> pendingFriendRequests;
    private static final Logger LOGGER = Logger.getLogger(FriendsManager.class.getName());

    private FriendsManager() {
        this.friendsDAO = AbstractDAOFactory.getFactory().createFriendsDAO();
        this.friendList = new ArrayList<>();
        this.followedUsers = new ArrayList<>();
        this.blockedUsers = new ArrayList<>();
        this.pendingFriendRequests = new ArrayList<>();
    }

    /**
     * Gets the singleton instance of FriendsManager.
     *
     * @return The FriendsManager instance
     */
    public static FriendsManager getInstance() {
        if (instance == null) {
            instance = new FriendsManager();
        }
        return instance;
    }

    /**
     * Loads all friend-related data for a user.
     *
     * @param userId The ID of the user
     */
    public void loadUserData(int userId) {
        friendList = friendsDAO.getFriendList(userId);
        followedUsers = friendsDAO.getFollowedUsers(userId);
        blockedUsers = friendsDAO.getBlockedUsers(userId);
        pendingFriendRequests = friendsDAO.getPendingFriendRequests(userId);
    }

    /**
     * Gets the friend list.
     *
     * @return ArrayList of User objects representing friends
     */
    public ArrayList<User> getFriendList() {
        return new ArrayList<>(friendList);
    }

    /**
     * Gets the followed users list.
     *
     * @return ArrayList of User objects representing followed users
     */
    public ArrayList<User> getFollowedUsers() {
        return new ArrayList<>(followedUsers);
    }

    /**
     * Gets the blocked users list.
     *
     * @return ArrayList of User objects representing blocked users
     */
    public ArrayList<User> getBlockedUsers() {
        return new ArrayList<>(blockedUsers);
    }

    /**
     * Gets the pending friend requests list.
     *
     * @return ArrayList of User objects representing pending requests
     */
    public ArrayList<User> getPendingFriendRequests() {
        return new ArrayList<>(pendingFriendRequests);
    }

    /**
     * Sends a friend request to another user.
     *
     * @param fromUserId The ID of the user sending the request
     * @param toUserId The ID of the user receiving the request
     * @return true if successful, false otherwise
     */
    public boolean addFriend(int fromUserId, int toUserId) {
        if (isBlocked(fromUserId, toUserId)) {
            LOGGER.warning("Cannot add friend: user is blocked");
            return false;
        }
        friendsDAO.addFriend(fromUserId, toUserId);
        return true;
    }

    /**
     * Removes a friend relationship.
     *
     * @param userId The ID of the first user
     * @param friendId The ID of the second user
     * @return true if successful, false otherwise
     */
    public boolean removeFriend(int userId, int friendId) {
        friendsDAO.removeFriend(userId, friendId);
        friendList.removeIf(user -> user.getId() == friendId);
        return true;
    }

    /**
     * Follows another user.
     *
     * @param followerId The ID of the user who is following
     * @param followedId The ID of the user being followed
     * @return true if successful, false otherwise
     */
    public boolean follow(int followerId, int followedId) {
        if (isBlocked(followerId, followedId)) {
            LOGGER.warning("Cannot follow: user is blocked");
            return false;
        }
        friendsDAO.follow(followerId, followedId);
        return true;
    }

    /**
     * Unfollows another user.
     *
     * @param followerId The ID of the user who is following
     * @param followedId The ID of the user being followed
     * @return true if successful, false otherwise
     */
    public boolean unfollow(int followerId, int followedId) {
        friendsDAO.unfollow(followerId, followedId);
        followedUsers.removeIf(user -> user.getId() == followedId);
        return true;
    }

    /**
     * Blocks another user.
     *
     * @param userId The ID of the user doing the blocking
     * @param blockedUserId The ID of the user being blocked
     * @return true if successful, false otherwise
     */
    public boolean block(int userId, int blockedUserId) {
        friendsDAO.block(userId, blockedUserId);
        blockedUsers.add(new User(blockedUserId, "", ""));
        friendList.removeIf(user -> user.getId() == blockedUserId);
        followedUsers.removeIf(user -> user.getId() == blockedUserId);
        return true;
    }

    /**
     * Unblocks another user.
     *
     * @param userId The ID of the user doing the unblocking
     * @param blockedUserId The ID of the user being unblocked
     * @return true if successful, false otherwise
     */
    public boolean unblock(int userId, int blockedUserId) {
        friendsDAO.unblock(userId, blockedUserId);
        blockedUsers.removeIf(user -> user.getId() == blockedUserId);
        return true;
    }

    /**
     * Gets the friend count for a user.
     *
     * @param userId The ID of the user
     * @return The number of friends
     */
    public int getFriendCount(int userId) {
        return friendsDAO.getFriendCount(userId);
    }

    /**
     * Gets the follower count for a user.
     *
     * @param userId The ID of the user
     * @return The number of followers
     */
    public int getFollowerCount(int userId) {
        return friendsDAO.getFollowerCount(userId);
    }

    /**
     * Searches for users by username.
     *
     * @param query The search query
     * @return ArrayList of User objects matching the query
     */
    public ArrayList<User> searchUsers(String query) {
        return friendsDAO.searchUsers(query);
    }

    /**
     * Checks if a user has blocked another user.
     *
     * @param userId The ID of the user
     * @param otherUserId The ID of the other user
     * @return true if otherUserId has blocked userId
     */
    public boolean isBlocked(int userId, int otherUserId) {
        return friendsDAO.isBlocked(userId, otherUserId);
    }

    /**
     * Checks if two users are friends.
     *
     * @param userId The ID of the first user
     * @param otherUserId The ID of the second user
     * @return true if they are friends
     */
    public boolean isFriend(int userId, int otherUserId) {
        return friendsDAO.isFriend(userId, otherUserId);
    }

    /**
     * Checks if a user is following another user.
     *
     * @param followerId The ID of the potential follower
     * @param followedId The ID of the potential followed user
     * @return true if followerId is following followedId
     */
    public boolean isFollowing(int followerId, int followedId) {
        return friendsDAO.isFollowing(followerId, followedId);
    }

    /**
     * Checks if there is a pending friend request.
     *
     * @param fromUserId The ID of the user who sent the request
     * @param toUserId The ID of the user who received the request
     * @return true if there is a pending request
     */
    public boolean hasPendingFriendRequest(int fromUserId, int toUserId) {
        return friendsDAO.hasPendingFriendRequest(fromUserId, toUserId);
    }

    /**
     * Accepts a friend request.
     *
     * @param userId The ID of the user accepting the request
     * @param requesterId The ID of the user who sent the request
     * @return true if successful, false otherwise
     */
    public boolean acceptFriendRequest(int userId, int requesterId) {
        friendsDAO.acceptFriendRequest(requesterId, userId);
        pendingFriendRequests.removeIf(user -> user.getId() == requesterId);
        loadUserData(userId);
        return true;
    }

    /**
     * Declines a friend request.
     *
     * @param userId The ID of the user declining the request
     * @param requesterId The ID of the user who sent the request
     * @return true if successful, false otherwise
     */
    public boolean declineFriendRequest(int userId, int requesterId) {
        friendsDAO.removeFriend(requesterId, userId);
        pendingFriendRequests.removeIf(user -> user.getId() == requesterId);
        return true;
    }

    /**
     * Gets the channel ID for DMs between two friends.
     *
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     * @return The channel ID for DMs, or 0 if friendship doesn't exist or isn't accepted
     */
    public int getChannelIdForFriendship(int user1Id, int user2Id) {
        return friendsDAO.getChannelIdForFriendship(user1Id, user2Id);
    }
}
