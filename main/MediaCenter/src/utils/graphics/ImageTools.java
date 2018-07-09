package utils.graphics;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;

public class ImageTools {
	
	public static enum ScaleMode {
		Stretch,
		Fill,
		Fit,
	}
	
	public static void centerImage(Image image, Container parent, Graphics g, boolean fill) {
		int w = parent.getWidth(), h = parent.getHeight();
		int iw = image.getWidth(null), ih = image.getHeight(null);
		double xScale = (double) w / iw, yScale = (double) h / ih;
		double scale = (fill ? Math.max(xScale, yScale) : Math.min(xScale, yScale));
		int width = (int) (scale * iw), height = (int) (scale * ih);
		int x = (w - width) / 2, y = (h - height) / 2;
		g.drawImage(image, x, y, width, height, null);
    }
	
}
