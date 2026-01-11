package fr.opal.factory;

import fr.opal.dao.*;
import fr.opal.db.DatabaseManager;
import fr.opal.type.Entry;
import fr.opal.type.User;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * MySQL implementation of the DAO Factory.
 */
public class MySQLDAOFactory extends AbstractDAOFactory
{
    private final Connection connection;

    public MySQLDAOFactory()
    {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    @Override public UserDAO createUserDAO()
    {
        return new MySQLUserDAO(connection);
    }

    @Override public SessionDAO createSessionDAO()
    {
        return new MySQLSessionDAO(connection);
    }

    @Override public FriendsDAO createFriendsDAO()
    {
        return new MySQLFriendsDAO(connection);
    }

    @Override public EntryDAO createEntryDAO()
    {
        return new MySQLEntryDAO(connection);
    }

    @Override public Entry createEntry(String title, String content, User author)
    {
        return new Entry(title, content, author);
    }

    @Override public Entry createEntry(int id, String title, String content, User author)
    {
        return new Entry(id, title, content, author);
    }

    @Override public ChannelDAO createChannelDAO()
    {
        return new MySQLChannelDAO(connection);
    }
}
