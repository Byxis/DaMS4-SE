package fr.opal.type;

/**
 * Enumeration representing available style palettes (themes)
 */
public enum StylePalette {
    LIGHT("Light Mode", "light"),
    DARK("Dark Mode", "dark");

    private final String displayName;
    private final String cssClass;

    StylePalette(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
