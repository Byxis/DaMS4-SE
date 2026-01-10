package fr.opal.controller;

import fr.opal.facade.SessionPropertiesFacade;
import fr.opal.type.SessionSettings;
import fr.opal.type.StyleColor;
import fr.opal.type.StylePalette;
import fr.opal.util.ColorUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for session properties (appearance settings)
 */
public class SessionPropertiesController {

    @FXML
    private ToggleGroup paletteToggleGroup;
    @FXML
    private RadioButton lightModeRadio;
    @FXML
    private RadioButton darkModeRadio;
    @FXML
    private Slider fontSizeSlider;
    @FXML
    private Label fontSizeLabel;
    @FXML
    private HBox colorButtonsContainer;
    @FXML
    private VBox rootContainer;

    private SessionPropertiesFacade facade;
    private StyleColor selectedAccentColor;
    private Runnable onSettingsChanged;

    private static final List<Runnable> globalSettingsListeners = new ArrayList<>();

    public SessionPropertiesController() {
        this.facade = SessionPropertiesFacade.getInstance();
    }

    @FXML
    public void initialize() {
        SessionSettings settings = facade.getSettings();

        // Initialize palette selection
        if (settings.getStylePalette() == StylePalette.DARK) {
            darkModeRadio.setSelected(true);
        } else {
            lightModeRadio.setSelected(true);
        }

        // Initialize font size
        fontSizeSlider.setValue(settings.getFontSize());
        updateFontSizeLabel(settings.getFontSize());

        // Initialize accent color
        selectedAccentColor = settings.getAccentColor();
        setupColorButtons();

        // Add listeners
        setupListeners();
    }

    /**
     * Setup event listeners
     */
    private void setupListeners() {
        // Palette change listener
        paletteToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal == lightModeRadio) {
                    submitPalette(StylePalette.LIGHT);
                } else if (newVal == darkModeRadio) {
                    submitPalette(StylePalette.DARK);
                }
            }
        });

        // Font size change listener - change on release to avoid too many updates
        fontSizeSlider.setOnMouseReleased(e -> {
            int fontSize = (int) fontSizeSlider.getValue();
            updateFontSizeLabel(fontSize);
            submitFontSize(fontSize);
        });
        
        // Also update label while dragging
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFontSizeLabel(newVal.intValue());
        });
    }

    /**
     * Update font size display label
     */
    private void updateFontSizeLabel(int fontSize) {
        fontSizeLabel.setText(fontSize + "px");
    }

    /**
     * Setup accent color buttons
     */
    private void setupColorButtons() {
        colorButtonsContainer.getChildren().clear();
        
        for (StyleColor color : StyleColor.values()) {
            Button colorBtn = createColorButton(color);
            colorButtonsContainer.getChildren().add(colorBtn);
        }
    }

    /**
     * Create a color selection button
     */
    private Button createColorButton(StyleColor color) {
        Button btn = new Button();
        btn.getStyleClass().add("color-button");
        
        // Get contrast text color based on background
        String textColor = ColorUtil.getContrastTextColor(color.getHexCode());
        
        String baseStyle = "-fx-background-color: " + color.getHexCode() + "; " +
                           "-fx-text-fill: " + textColor + "; " +
                           "-fx-min-width: 40; -fx-min-height: 40; " +
                           "-fx-background-radius: 20; -fx-cursor: hand; " +
                           "-fx-border-radius: 20; -fx-padding: 0;";
        
        if (color == selectedAccentColor) {
            btn.setStyle(baseStyle + "-fx-border-color: " + textColor + "; -fx-border-width: 3;");
        } else {
            btn.setStyle(baseStyle + "-fx-border-width: 0;");
        }

        btn.setOnAction(e -> {
            selectedAccentColor = color;
            submitAccentColor(color);
            setupColorButtons(); // Refresh to show selection
        });

        // Tooltip
        Tooltip tooltip = new Tooltip(color.getDisplayName());
        btn.setTooltip(tooltip);

        return btn;
    }

    /**
     * Submit font size change
     * @return The saved font size
     */
    public int submitFontSize() {
        int fontSize = (int) fontSizeSlider.getValue();
        facade.saveFontSize(fontSize);
        notifySettingsChanged();
        return fontSize;
    }

    /**
     * Submit font size change with specific value
     */
    private void submitFontSize(int fontSize) {
        facade.saveFontSize(fontSize);
        notifySettingsChanged();
    }

    /**
     * Submit palette change
     * @return The saved palette
     */
    public StylePalette submitPalette() {
        StylePalette palette = lightModeRadio.isSelected() ? StylePalette.LIGHT : StylePalette.DARK;
        facade.saveStylePalette(palette);
        notifySettingsChanged();
        return palette;
    }

    /**
     * Submit palette change with specific value
     */
    private void submitPalette(StylePalette palette) {
        facade.saveStylePalette(palette);
        notifySettingsChanged();
    }

    /**
     * Submit accent color change
     */
    private void submitAccentColor(StyleColor color) {
        facade.saveAccentColor(color);
        notifySettingsChanged();
    }

    /**
     * Set callback for settings changes
     */
    public void setOnSettingsChanged(Runnable callback) {
        this.onSettingsChanged = callback;
    }

    /**
     * Notify that settings have changed
     */
    private void notifySettingsChanged() {
        if (onSettingsChanged != null) {
            onSettingsChanged.run();
        }
        for (Runnable listener : globalSettingsListeners) {
            listener.run();
        }

        if (rootContainer != null) {
            // Apply the full theme (classes + variables) to the local view immediately
            facade.applyTheme(rootContainer);
        }
    }

    /**
     * Add a global listener for settings changes.
     */
    public static void addGlobalSettingsListener(Runnable listener) {
        globalSettingsListeners.add(listener);
    }

    /**
     * Remove a global listener for settings changes.
     */
    public static void removeGlobalSettingsListener(Runnable listener) {
        globalSettingsListeners.remove(listener);
    }
}
