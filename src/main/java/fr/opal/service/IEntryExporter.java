package fr.opal.service;

import fr.opal.type.Entry;
import java.util.List;

/**
 * Interface for exporting entries to various formats
 */
public interface IEntryExporter {

    /**
     * Exports an entry and its subtree to a file
     * @param entry The root entry to export
     * @param filePath Path where the file will be saved
     */
    void exportEntry(Entry entry, String filePath) throws Exception;

    /**
     * Exports entry data to a string representation
     * @param entry The root entry to export
     * @return String representation of the entry tree
     */
    String exportToString(Entry entry) throws Exception;
}
