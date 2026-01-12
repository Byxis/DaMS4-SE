package fr.opal.util;

import fr.opal.type.SessionSettings;

/**
 * Utility class for handling CSS theme string generation.
 * Separates the string manipulation logic from the session management.
 */
public class ThemeHelper {

    /**
     * Updates an existing CSS style string with the values from SessionSettings.
     * Removes old theme variables and appends the new ones.
     *
     * @param currentStyle The current style string of the node (can be null).
     * @param settings     The session settings containing user preferences.
     * @return The updated CSS style string.
     */
    public static String updateStyle(String currentStyle, SessionSettings settings) {
        if (currentStyle == null) {
            currentStyle = "";
        }
        
        String cleanedStyle = currentStyle
                .replaceAll("-fx-accent-color:\\s*#[0-9A-Fa-f]+;?", "")
                .replaceAll("-fx-accent:\\s*#[0-9A-Fa-f]+;?", "")
                .replaceAll("-fx-focus-color:\\s*#[0-9A-Fa-f]+;?", "")
                .replaceAll("-fx-faint-focus-color:\\s*[^;]+;?", "")
                .replaceAll("-fx-font-size:\\s*[0-9]+px;?", "");

        if (!cleanedStyle.isEmpty() && !cleanedStyle.endsWith(";")) {
            cleanedStyle += "; ";
        }
        int fontSize = settings.getFontSize() > 0 ? settings.getFontSize() : 14;
        String accentColor = settings.getAccentColor().getHexCode();

        StringBuilder newStyle = new StringBuilder(cleanedStyle);
        
        newStyle.append("-fx-font-size: ").append(fontSize).append("px; ");
        newStyle.append("-fx-accent-color: ").append(accentColor).append("; ");
        newStyle.append("-fx-accent: ").append(accentColor).append("; ");
        newStyle.append("-fx-focus-color: ").append(accentColor).append("; ");
        newStyle.append("-fx-faint-focus-color: transparent;");

        return newStyle.toString();
    }
}
