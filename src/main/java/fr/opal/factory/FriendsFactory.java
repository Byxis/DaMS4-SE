package fr.opal.factory;

import fr.opal.dao.FriendsDAO;

/**
 * Abstract factory for creating FriendsDAO instances.
 */
public abstract class FriendsFactory {

    private static FriendsFactory instance;

    /**
     * Gets the singleton instance of the FriendsFactory.
     *
     * @return The FriendsFactory instance
     */
    public static FriendsFactory getInstance() {
        if (instance == null) {
            instance = new MySQLFriendsFactory();
        }
        return instance;
    }

    /**
     * Creates a FriendsDAO instance.
     *
     * @return A new FriendsDAO instance
     */
    public abstract FriendsDAO createFriendsDAO();
}
