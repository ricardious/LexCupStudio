package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowDragger {
    private double xOffset = 0;
    private double yOffset = 0;

    public void makeDraggable(Node node) {
        node.setOnMousePressed(event -> {
            Stage stage = (Stage) node.getScene().getWindow();
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        node.setOnMouseDragged(event -> {
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}
