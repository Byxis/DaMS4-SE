package fr.opal.type;

/**
 * Enumeration representing available accent colors
 */
public enum StyleColor {
    BLACK("Black", "#2D2D2D"),
    BLUE("Blue", "#2196F3"),
    GREEN("Green", "#4CAF50"),
    PURPLE("Purple", "#9C27B0"),
    ORANGE("Orange", "#FF9000"),
    RED("Red", "#F44336"),
    WHITE("White", "#FFFFFF");

    private final String displayName;
    private final String hexCode;

    StyleColor(String displayName, String hexCode) {
        this.displayName = displayName;
        this.hexCode = hexCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHexCode() {
        return hexCode;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
