package fr.opal.type;

/**
 * Represents user session visual preferences
 */
public class SessionSettings {

    private int fontSize;
    private StylePalette stylePalette;
    private StyleColor accentColor;

    /**
     * Default constructor with default values
     */
    public SessionSettings() {
        this.fontSize = 14;
        this.stylePalette = StylePalette.LIGHT;
        this.accentColor = StyleColor.BLACK;
    }

    /**
     * Constructor with all parameters
     */
    public SessionSettings(int fontSize, StylePalette stylePalette, StyleColor accentColor) {
        this.fontSize = fontSize;
        this.stylePalette = stylePalette;
        this.accentColor = accentColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public StylePalette getStylePalette() {
        return stylePalette;
    }

    public void setStylePalette(StylePalette stylePalette) {
        this.stylePalette = stylePalette;
    }

    public StyleColor getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(StyleColor accentColor) {
        this.accentColor = accentColor;
    }
}
