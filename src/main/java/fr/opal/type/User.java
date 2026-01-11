package fr.opal.type;

/**
 * Represents a user
 */
public class User {

    private int id;
    private String username;
    private String password;

    /**
     * Default empty constructor
     */
    public User() {
        this(0, "", "");
    }

    /**
     * Constructor with parameters
     */
    public User(int id, String username, String password)
    {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean verifyPassword(String password)
    {
        return this.password.equals(password);
    }
}