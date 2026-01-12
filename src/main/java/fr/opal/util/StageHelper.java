package fr.opal.util;

import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to get all open stages
 */
public class StageHelper {
    public static List<Stage> getStages() {
        List<Stage> stages = new ArrayList<>();
        try {
            Class<?> windowClass = Class.forName("javafx.stage.Window");
            java.lang.reflect.Method getWindows = windowClass.getMethod("getWindows");
            @SuppressWarnings("unchecked")
            List<javafx.stage.Window> windows = (List<javafx.stage.Window>) getWindows.invoke(null);
            for (javafx.stage.Window window : windows) {
                if (window instanceof Stage) {
                    stages.add((Stage) window);
                }
            }
        } catch (Exception e) {
            // Fallback : nothing
        }
        return stages;
    }
}

