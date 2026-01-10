package fr.opal.type;

/**
 * Represents user session visual preferences
 */
public class SessionSettings {

    private int fontSize;
    private StylePalette stylePalette;
    private StyleColor accentColor;

    public SessionSettings() {
        this.fontSize = 14;
        this.stylePalette = StylePalette.LIGHT;
        this.accentColor = StyleColor.BLUE;
    }

    public SessionSettings(int fontSize, StylePalette stylePalette, StyleColor accentColor) {
        this.fontSize = fontSize;
        this.stylePalette = stylePalette;
        this.accentColor = accentColor;
    }

    /**
     * Get the font size
     *
     * @return the font size
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Set the font size
     *
     * @param fontSize the font size to set
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Get the style palette
     *
     * @return the style palette
     */
    public StylePalette getStylePalette() {
        return stylePalette;
    }

    /**
     * Set the style palette
     *
     * @param stylePalette the style palette to set
     */
    public void setStylePalette(StylePalette stylePalette) {
        this.stylePalette = stylePalette;
    }

    /**
     * Get the accent color
     *
     * @return the accent color
     */
    public StyleColor getAccentColor() {
        return accentColor;
    }

    /**
     * Set the accent color
     *
     * @param accentColor the accent color to set
     */
    public void setAccentColor(StyleColor accentColor) {
        this.accentColor = accentColor;
    }
}
