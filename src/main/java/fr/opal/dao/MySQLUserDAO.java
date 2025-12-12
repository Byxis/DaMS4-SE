package fr.opal.dao;

import fr.opal.type.User;

import java.sql.*;

/**
 *
 */
public class MySQLUserDAO extends UserDAO
{
    private Connection conn;

    /**
     * Default constructor
     */
    public MySQLUserDAO(Connection _conn)
    {
        super();
        this.conn = _conn;
    }


    /**
     * @return
     */
    public User getUserById(String username)
    {
        String sql = "SELECT id, username, password FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (rs.next())
            {
                User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                return user;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public User createUser(String username, String password)
    {
        String sql = "INSERT INTO users(username, password) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if (rs.next())
                {
                    int id = rs.getInt(1);
                    return new User(id, username, password);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}