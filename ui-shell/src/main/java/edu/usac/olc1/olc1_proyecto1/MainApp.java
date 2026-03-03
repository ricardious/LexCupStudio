package edu.usac.olc1.olc1_proyecto1;

import edu.usac.olc1.olc1_proyecto1.ui.utils.Fonts;
import edu.usac.olc1.olc1_proyecto1.ui.utils.BrandingConfig;
import edu.usac.olc1.olc1_proyecto1.ui.utils.Styles;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class MainApp extends Application {
    /**
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws IOException {
        startProgram();

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("fxml/editor.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Styles.forName("main.css"));
        stage.setScene(scene);
        stage.setTitle(BrandingConfig.getAppName());
        Image appLogo = BrandingConfig.getLogoImage();
        if (appLogo != null) {
            stage.getIcons().add(appLogo);
        }
        stage.initStyle(StageStyle.UNDECORATED);

        stage.show();
    }

    public static void startProgram() {
        Font.loadFont(Fonts.ttf("JetBrainsMono-BoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Bold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraBoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraBold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraLightItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ExtraLight.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Italic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-LightItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Light.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-MediumItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Medium.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-BoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-Bold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-ExtraBoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-ExtraBold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-ExtraLightItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-ExtraLight.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-Italic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-LightItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-Light.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-MediumItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-Medium.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-Regular.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-SemiBoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-SemiBold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-ThinItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMonoNL-Thin.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Regular.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-SemiBoldItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-SemiBold.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-ThinItalic.ttf"), 10);
        Font.loadFont(Fonts.ttf("JetBrainsMono-Thin.ttf"), 10);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
