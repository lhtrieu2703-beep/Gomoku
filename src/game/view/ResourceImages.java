package game.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public final class ResourceImages {
    private ResourceImages() {}
    public static BufferedImage load(String path) {
        URL resource = ResourceImages.class.getResource(path);
        if (resource == null) return null;
        try { return ImageIO.read(resource); }
        catch (IOException | IllegalArgumentException exception) { return null; }
    }
}
