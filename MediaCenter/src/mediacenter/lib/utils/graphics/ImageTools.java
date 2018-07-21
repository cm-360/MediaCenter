package mediacenter.lib.utils.graphics;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class ImageTools {
	
	public static enum ScaleMode {
		Stretch,
		Fill,
		Fit,
	}
	
	/**
	 * Centers an image on the given {@code Graphics}
	 * 
	 * @param image
	 *            The image to draw
	 * @param bounds
	 *            The bounding rectangle to center the image in
	 * @param g
	 *            The graphics object to draw on
	 * @param fill
	 *            Fills bounds with image if {@code true}, fits image to bounds
	 *            otherwise
	 */
	public static void centerImage(Image image, Rectangle bounds, Graphics g, boolean fill) {
		int w = bounds.width, h = bounds.height;
		int iw = image.getWidth(null), ih = image.getHeight(null);
		double xScale = (double) w / iw, yScale = (double) h / ih;
		double scale = (fill ? Math.max(xScale, yScale) : Math.min(xScale, yScale));
		int width = (int) (scale * iw), height = (int) (scale * ih);
		int x = (w - width) / 2, y = (h - height) / 2;
		g.drawImage(image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING), x, y, width, height, null);
    }
	
}
