package fr.opal.service;

import fr.opal.type.Entry;

/**
 * Interface for importing entries from various formats
 */
public interface IEntryImporter {

    /**
     * Imports an entry tree from a file
     * @param filePath Path to the file to import
     * @return The root entry of the imported tree
     */
    Entry importEntry(String filePath) throws Exception;

    /**
     * Imports entry data from a string
     * @param data String representation of entry data
     * @return The root entry of the imported tree
     */
    Entry importFromString(String data) throws Exception;
}
