package fr.opal.type;

import java.util.Date;

/**
 * Represents metadata information for an entry
 */
public class MetaData {
    private Date creationDate;
    private Date lastModified;

    /**
     * Constructor
     */
    public MetaData() {
        this.creationDate = new Date();
        this.lastModified = new Date();
    }

    /**
     * Constructor with custom dates
     */
    public MetaData(Date creationDate, Date lastModified) {
        this.creationDate = creationDate;
        this.lastModified = lastModified;
    }

    // Getters
    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    // Setters
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
