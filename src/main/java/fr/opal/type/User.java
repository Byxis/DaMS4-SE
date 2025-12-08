package fr.opal.type;

/**
 * 
 */
public class User {

    /**
     *
     */
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

    public int getId()
    {
        return id;
    }

    public String getPassword()
    {
        return password;
    }
}