package edu.usac.olc1.olc1_proyecto1.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;

public class ImageViewer extends VBox {

    private final ImageView imageView;
    private final ScrollPane scrollPane;
    private final Label infoLabel;
    private final Label zoomLabel;
    private final Slider zoomSlider;
    private final Button fitToWindowButton;
    private final Button actualSizeButton;
    private final Button zoomInButton;
    private final Button zoomOutButton;

    private double currentZoom = 1.0;
    private final Image image;
    private final File imageFile;

    public ImageViewer(File imageFile) {
        this.imageFile = imageFile;
        this.image = new Image(imageFile.toURI().toString());
        this.imageView = new ImageView(image);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #1e1e1e;");
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().add(imageView);

        this.scrollPane = new ScrollPane(imageContainer);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scrollPane.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");

        this.zoomSlider = new Slider(0.1, 5.0, 1.0);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(0.5);
        zoomSlider.setMinorTickCount(4);
        zoomSlider.setPrefWidth(200);

        this.zoomLabel = new Label("100%");
        zoomLabel.setTextFill(Color.WHITE);
        zoomLabel.setFont(Font.font("Consolas", 12));
        zoomLabel.setPrefWidth(50);

        this.zoomInButton = new Button("+");
        this.zoomOutButton = new Button("-");
        this.fitToWindowButton = new Button("Ajustar a ventana");
        this.actualSizeButton = new Button("Tamaño real");

        String buttonStyle = "-fx-background-color: #3c3c3c; -fx-text-fill: white; " +
                           "-fx-border-color: #5a5a5a; -fx-border-radius: 3; " +
                           "-fx-background-radius: 3; -fx-padding: 5 10;";

        zoomInButton.setStyle(buttonStyle);
        zoomOutButton.setStyle(buttonStyle);
        fitToWindowButton.setStyle(buttonStyle);
        actualSizeButton.setStyle(buttonStyle);

        this.infoLabel = new Label(createImageInfoText());
        infoLabel.setTextFill(Color.WHITE);
        infoLabel.setFont(Font.font("Consolas", 11));
        infoLabel.setStyle("-fx-background-color: rgba(45, 45, 48, 0.9); " +
                          "-fx-padding: 8; -fx-background-radius: 4;");

        setupLayout();
        setupEventHandlers();

        initializeWithPreviewSize();
    }

    private void initializeWithPreviewSize() {
        double maxPreviewSize = 300;
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        if (imageWidth > maxPreviewSize || imageHeight > maxPreviewSize) {
            double scaleX = maxPreviewSize / imageWidth;
            double scaleY = maxPreviewSize / imageHeight;
            double previewScale = Math.min(scaleX, scaleY);

            currentZoom = previewScale;
            updateImageDisplay();
            updateZoomControls();
        } else {
            currentZoom = 1.0;
            updateImageDisplay();
            updateZoomControls();
        }
    }

    private void setupLayout() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(5, 10, 5, 10));
        toolbar.setStyle("-fx-background-color: #252526; -fx-border-color: #3e3e42; -fx-border-width: 0 0 1 0;");

        Label zoomLabelText = new Label("Zoom:");
        zoomLabelText.setTextFill(Color.WHITE);

        toolbar.getChildren().addAll(
            zoomOutButton, zoomInButton,
            new Separator(),
            zoomLabelText, zoomSlider, zoomLabel,
            new Separator(),
            fitToWindowButton, actualSizeButton
        );

        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().add(scrollPane);

        StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(infoLabel, new Insets(10));
        imageContainer.getChildren().add(infoLabel);

        // Layout principal
        this.setStyle("-fx-background-color: #1e1e1e;");
        this.getChildren().addAll(toolbar, imageContainer);
        VBox.setVgrow(imageContainer, Priority.ALWAYS);
    }

    private void setupEventHandlers() {
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> setZoom(newVal.doubleValue()));

        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        fitToWindowButton.setOnAction(e -> fitToWindow());
        actualSizeButton.setOnAction(e -> actualSize());

        scrollPane.setOnScroll(this::handleMouseScroll);

        scrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (currentZoom == -1) {
                fitToWindow();
            }
        });

        scrollPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (currentZoom == -1) {
                fitToWindow();
            }
        });
    }

    private void handleMouseScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            event.consume();
            double deltaY = event.getDeltaY();
            double scaleFactor = (deltaY > 0) ? 1.1 : 1.0 / 1.1;
            double newZoom = Math.max(0.1, Math.min(5.0, currentZoom * scaleFactor));
            setZoom(newZoom);
        }
    }

    private void setZoom(double zoom) {
        currentZoom = zoom;
        updateImageDisplay();
        updateZoomControls();
    }

    private void zoomIn() {
        double newZoom = Math.min(5.0, currentZoom * 1.2);
        setZoom(newZoom);
    }

    private void zoomOut() {
        double newZoom = Math.max(0.1, currentZoom / 1.2);
        setZoom(newZoom);
    }

    private void fitToWindow() {
        double containerWidth = scrollPane.getWidth() - 20;
        double containerHeight = scrollPane.getHeight() - 20;

        if (containerWidth > 0 && containerHeight > 0) {
            double scaleX = containerWidth / image.getWidth();
            double scaleY = containerHeight / image.getHeight();
            double scale = Math.min(scaleX, scaleY);

            currentZoom = -1;
            imageView.setFitWidth(image.getWidth() * scale);
            imageView.setFitHeight(image.getHeight() * scale);

            zoomSlider.setValue(scale);
            zoomLabel.setText(String.format("%.0f%%", scale * 100));
        }
    }

    private void actualSize() {
        setZoom(1.0);
    }

    private void updateImageDisplay() {
        if (currentZoom > 0) {
            imageView.setFitWidth(image.getWidth() * currentZoom);
            imageView.setFitHeight(image.getHeight() * currentZoom);
        }
    }

    private void updateZoomControls() {
        if (currentZoom > 0) {
            zoomSlider.setValue(currentZoom);
            zoomLabel.setText(String.format("%.0f%%", currentZoom * 100));
        }
    }

    private String createImageInfoText() {
        long fileSize = imageFile.length();
        String fileSizeText = formatFileSize(fileSize);

        return String.format("""
            Archivo: %s
            Dimensiones: %.0f × %.0f px
            Tamaño: %s
            Formato: %s""",
            imageFile.getName(),
            image.getWidth(),
            image.getHeight(),
            fileSizeText,
            getFileExtension(imageFile.getName()).toUpperCase()
        );
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0) ? fileName.substring(lastDotIndex + 1) : "";
    }
}
