package fr.opal.util;

/**
 * Utility class for color operations
 */
public class ColorUtil {

    /**
     * Determine if text should be black or white based on background color luminosity
     * Uses the relative luminance formula from WCAG
     * @param hexColor The hex color (e.g., "#FF5500")
     * @return "#000000" for black text or "#FFFFFF" for white text
     */
    public static String getContrastTextColor(String hexColor) {
        String hex = hexColor.replace("#", "");
        
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        
        double luminance = calculateRelativeLuminance(r, g, b);
        
        return luminance > 0.5 ? "#000000" : "#FFFFFF";
    }

    /**
     * Calculate relative luminance according to WCAG guidelines
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return Luminance value (0-1)
     */
    private static double calculateRelativeLuminance(int r, int g, int b) {
        double rNorm = r / 255.0;
        double gNorm = g / 255.0;
        double bNorm = b / 255.0;
        
        rNorm = rNorm <= 0.03928 ? rNorm / 12.92 : Math.pow((rNorm + 0.055) / 1.055, 2.4);
        gNorm = gNorm <= 0.03928 ? gNorm / 12.92 : Math.pow((gNorm + 0.055) / 1.055, 2.4);
        bNorm = bNorm <= 0.03928 ? bNorm / 12.92 : Math.pow((bNorm + 0.055) / 1.055, 2.4);
        
        return 0.2126 * rNorm + 0.7152 * gNorm + 0.0722 * bNorm;
    }
}
