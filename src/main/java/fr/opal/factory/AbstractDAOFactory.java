package fr.opal.factory;

import fr.opal.dao.ChannelDAO;
import fr.opal.dao.EntryDAO;
import fr.opal.dao.FriendsDAO;
import fr.opal.dao.SessionDAO;
import fr.opal.dao.UserDAO;
import fr.opal.type.Entry;
import fr.opal.type.User;

/**
 * Abstract DAO Factory.
 * Centralizes the creation of all DAOs.
 * Implements the Abstract Factory pattern.
 */
public abstract class AbstractDAOFactory
{
    /**
     * Enum for supported factory types.
     */
    public enum FactoryType
    {
        MYSQL,
        // XML, JSON, etc. can be added here
    }

    private static AbstractDAOFactory instance;

    /**
     * Returns the singleton instance of the DAO Factory.
     * Starts with a default MySQL factory.
     * @return The AbstractDAOFactory instance
     */
    public static synchronized AbstractDAOFactory getFactory()
    {
        if (instance == null)
        {
            // Default to MySQL
            instance = new MySQLDAOFactory();
        }
        return instance;
    }

    public abstract UserDAO createUserDAO();
    public abstract SessionDAO createSessionDAO();
    public abstract FriendsDAO createFriendsDAO();
    public abstract EntryDAO createEntryDAO();
    public abstract Entry createEntry(String title, String content, User author);
    public abstract Entry createEntry(int id, String title, String content, User author);
    public abstract ChannelDAO createChannelDAO();
}
