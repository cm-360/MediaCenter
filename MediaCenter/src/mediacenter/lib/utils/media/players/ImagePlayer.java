package mediacenter.lib.utils.media.players;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import mediacenter.lib.types.io.file.MediaFile;
import mediacenter.lib.utils.graphics.ImageTools;
import mediacenter.lib.utils.graphics.ImageTools.ScaleMode;
import mediacenter.lib.utils.media.MediaPlayer;

public class ImagePlayer extends JPanel implements MediaPlayer {
	
	private static final long serialVersionUID = 1L;
	
	private Image image;
	private ScaleMode scaleMode = ScaleMode.Fit;
	
	private MediaFile file;

	@Override
	public String[] getSupportedFileExts() {
		return new String[] {
				"bmp", "jpeg", "jpg", "png"
		};
	}

	@Override
	public void play(MediaFile m, Runnable onReady, Runnable onFinish) {
		try {
			image = ImageIO.read(m.getMediaFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pause() {
		// Images can't be paused
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void seek(int time) {
		// Images can't be sought through
	}
	
	@Override
	public int getMediaLength() {
		return 0; // Images are still
	}

	@Override
	public int getMediaTime() {
		return 0; // Images are still
	}

	@Override
	public MediaFile getMedia() {
		return file;
	}

	@Override
	public boolean isPaused() {
		return false; // Images are still
	}

	@Override
	public boolean isStopped() {
		return image == null;
	}

	@Override
	public void setVolume(double volume) {
		// Images don't have sound
	}
	
	// Paint method
	@Override
	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		if (image != null) {
			if (scaleMode == ScaleMode.Stretch) {
				g.drawImage(image.getScaledInstance(getWidth(), getHeight(), Image.SCALE_AREA_AVERAGING), 0, 0,
						getWidth(), getHeight(), null);
			} else if (scaleMode == ScaleMode.Fill) {
				ImageTools.centerImage(image, getBounds(), g, true);
			} else if (scaleMode == ScaleMode.Fit) {
				ImageTools.centerImage(image, getBounds(), g, false);
			}
		}
	}
	
}
