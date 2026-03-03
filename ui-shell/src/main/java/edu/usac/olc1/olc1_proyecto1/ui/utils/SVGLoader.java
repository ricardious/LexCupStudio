package edu.usac.olc1.olc1_proyecto1.ui.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.usac.olc1.olc1_proyecto1.ui.utils.PathBuilder.concatPaths;

public class SVGLoader {
    private static final Logger LOGGER = Logger.getLogger(SVGLoader.class.getName());

    private static final String ROOT_FOLDER = "/edu/usac/olc1/olc1_proyecto1/";
    private static final String DEFAULT_ICONS_FOLDER = ROOT_FOLDER + "icons/svg/";

    private static final Map<String, ImageView> cache = new ConcurrentHashMap<>();

    public static ImageView loadSVGIcon(String fileName, int width, int height) {
        return loadSVGIconFromFolder("", fileName, width, height);
    }

    public static ImageView loadSVGIconFromFolder(String baseFolder, String fileName, int width, int height) {

        String fullBaseFolder;
        if (baseFolder == null || baseFolder.isEmpty()) {
            fullBaseFolder = DEFAULT_ICONS_FOLDER;
        } else {
            fullBaseFolder = concatPaths(ROOT_FOLDER, baseFolder);
        }

        String key = baseFolder + fileName + "_" + width + "x" + height;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        String svgPath = concatPaths(fullBaseFolder, fileName);

        InputStream stream = SVGLoader.class.getResourceAsStream(svgPath);
        if (stream == null) {
            LOGGER.warning("No se encontró el archivo SVG: " + svgPath + ". Usando fallback.");
            String fallbackPath = concatPaths(ROOT_FOLDER, "icons/svg/ghost.svg");
            stream = SVGLoader.class.getResourceAsStream(fallbackPath);
            if (stream == null) {
                LOGGER.warning("No se encontró el fallback icon: " + fallbackPath + ". Se retornará null.");
                return null;
            }
        }
        try {
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);

            TranscoderInput input = new TranscoderInput(stream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);

            transcoder.transcode(input, output);
            outputStream.flush();

            Image fxImage = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
            outputStream.close();

            ImageView iv = new ImageView(fxImage);
            cache.put(key, iv);
            return iv;
        } catch (IOException | TranscoderException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar SVG: " + svgPath, e);
            return null;
        }
    }
}
