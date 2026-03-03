package edu.usac.olc1.olc1_proyecto1.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.StackPane;
import edu.usac.olc1.olc1_proyecto1.ui.utils.SVGLoader;

public class CustomSplitMenuButton extends SplitMenuButton {

    private final StringProperty dropdownIconPath = new SimpleStringProperty("chevron-down.svg");
    private final ObjectProperty<Node> dropdownGraphic = new SimpleObjectProperty<>();
    private StackPane arrowRegion;

    public CustomSplitMenuButton() {
        super();
        initialize();
    }

    public CustomSplitMenuButton(String text) {
        super();
        setText(text);
        initialize();
    }

    public CustomSplitMenuButton(String text, Node graphic) {
        super();
        setText(text);
        setGraphic(graphic);
        initialize();
    }

    private void initialize() {
        getStyleClass().add("custom-split-menu-button");

        setStyle("-fx-arrow-visible: false;");

        sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                applySkinImmediately();
            }
        });

        dropdownIconPath.addListener((obs, oldPath, newPath) -> {
            if (newPath != null && !newPath.isEmpty()) {
                updateDropdownIcon();
            }
        });

        dropdownGraphic.addListener((obs, oldGraphic, newGraphic) -> {
            if (newGraphic != null) {
                replaceArrowWithCustomGraphic(newGraphic);
            }
        });
    }

    private void applySkinImmediately() {
        javafx.application.Platform.runLater(() -> {
            lookupArrowButton();
            updateDropdownIcon();
        });
    }

    private void lookupArrowButton() {
        arrowRegion = (StackPane) lookup(".arrow-button");

        if (arrowRegion != null) {
            Node arrow = lookup(".arrow");
            if (arrow != null && arrow.getParent() == arrowRegion) {
                arrow.setVisible(false);
                arrow.setManaged(false);
            }
        }
    }

    private void updateDropdownIcon() {
        if (arrowRegion == null) {
            lookupArrowButton();
            if (arrowRegion == null) return;
        }

        if (getDropdownGraphic() != null) {
            replaceArrowWithCustomGraphic(getDropdownGraphic());
        }
        else if (getDropdownIconPath() != null && !getDropdownIconPath().isEmpty()) {
            Node icon = SVGLoader.loadSVGIcon(getDropdownIconPath(), 12, 12);
            if (icon != null) {
                replaceArrowWithCustomGraphic(icon);
            }
        }
    }

    private void replaceArrowWithCustomGraphic(Node graphic) {
        if (arrowRegion == null) return;

        arrowRegion.getChildren().removeIf(child -> !child.getStyleClass().contains("arrow"));

        arrowRegion.getChildren().add(graphic);

        arrowRegion.setVisible(true);
        arrowRegion.setManaged(true);
    }

    public String getDropdownIconPath() {
        return dropdownIconPath.get();
    }

    public void setDropdownIconPath(String path) {
        dropdownIconPath.set(path);
    }

    public StringProperty dropdownIconPathProperty() {
        return dropdownIconPath;
    }

    public Node getDropdownGraphic() {
        return dropdownGraphic.get();
    }

    public void setDropdownGraphic(Node graphic) {
        dropdownGraphic.set(graphic);
    }

    public ObjectProperty<Node> dropdownGraphicProperty() {
        return dropdownGraphic;
    }

    public void refreshDropdownIcon() {
        updateDropdownIcon();
    }
}