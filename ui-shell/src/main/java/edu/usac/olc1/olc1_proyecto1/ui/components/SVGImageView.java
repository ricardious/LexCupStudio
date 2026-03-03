package edu.usac.olc1.olc1_proyecto1.ui.components;

import edu.usac.olc1.olc1_proyecto1.ui.utils.SVGLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

public class SVGImageView extends ImageView {

    private final StringProperty svgPath = new SimpleStringProperty();

    public SVGImageView() {
        getStyleClass().add("svg-image-view");
        svgPath.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                int width = (int) getFitWidth();
                int height = (int) getFitHeight();
                if (width <= 0) width = 16;
                if (height <= 0) height = 16;

                ImageView loaded = SVGLoader.loadSVGIcon(newVal, width, height);
                if (loaded != null) {
                    setImage(loaded.getImage());
                }
            }
        });
    }

    public final String getSvgPath() {
        return svgPath.get();
    }

    public final void setSvgPath(String value) {
        this.svgPath.set(value);
    }

    public StringProperty svgPathProperty() {
        return svgPath;
    }
}
