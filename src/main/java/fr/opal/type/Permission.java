package fr.opal.type;

/**
 * Represents a permission
 */
public class Permission {

    private int id;
    private String name;

    /**
     * Constructor
     */
    public Permission(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
