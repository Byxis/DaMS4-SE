package fr.opal.type;

/**
 * Represents a permission
 */
public class Permission {

    private int id;
    private String name;

    public Permission(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the ID of the permission.
     *
     * @return the permission ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the name of the permission.
     *
     * @return the permission name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the ID of the permission.
     *
     * @param id the permission ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the name of the permission.
     *
     * @param name the permission name
     */
    public void setName(String name) {
        this.name = name;
    }
}
