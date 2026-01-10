package fr.opal.type;

/**
 * Represents a user
 */
public class User {

    private int id;
    private String username;
    private String password;

    /**
     * Default constructor
     */
    public User(int id, String username, String password)
    {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    /**
     * Get the user ID
     *
     * @return the user ID
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the username
     *
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Verify the password
     *
     * @param password the password to verify
     * @return true if the password is correct, false otherwise
     */
    public boolean verifyPassword(String password)
    {
        return this.password.equals(password);
    }
}