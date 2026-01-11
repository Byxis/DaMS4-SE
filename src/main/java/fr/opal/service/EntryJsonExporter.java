package fr.opal.service;

import fr.opal.type.Entry;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * JSON exporter for entries
 */
public class EntryJsonExporter implements IEntryExporter {

    /**
     * Exports an entry and its subtree to a JSON file
     */
    @Override
    public void exportEntry(Entry entry, String filePath) throws Exception {
        String jsonContent = exportToString(entry);
        Files.write(Paths.get(filePath), jsonContent.getBytes());
    }

    /**
     * Exports entry data to a JSON string
     */
    @Override
    public String exportToString(Entry entry) throws Exception {
        // TODO: Serialize entry tree to JSON format
        // This would typically use a JSON library like Gson or Jackson
        // For now, providing stub implementation
        return "{}";
    }
}
