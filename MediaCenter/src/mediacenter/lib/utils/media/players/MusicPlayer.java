package mediacenter.lib.utils.media.players;

import java.awt.Graphics;
import java.awt.Image;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.mpatric.mp3agic.Mp3File;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import mediacenter.lib.types.io.file.MediaFile;
import mediacenter.lib.utils.graphics.ImageTools;
import mediacenter.lib.utils.graphics.ImageTools.ScaleMode;

public class MusicPlayer extends JPanel implements mediacenter.lib.utils.media.MediaPlayer {
	
	private static final long serialVersionUID = 1L;
	
	private Image artwork;
	private ScaleMode scaleMode = ScaleMode.Fit;
	
	private MediaFile file;
	private MediaPlayer fxPlayer;
	private double volume;
	
	// Constructor
	public MusicPlayer() {
		new JFXPanel(); // Initialize the JFXToolkit
	}

	@Override
	public String[] getSupportedFileExts() {
		return new String[] {
				"mp3"
		};
	}

	@Override
	public void play(final MediaFile m, final Runnable onReady, final Runnable onFinish) {
		if (m != null) {
			String mediaURI = (file = m).getMediaFile().toURI().toString();
			if (isStopped() || !fxPlayer.getMedia().getSource().equals(mediaURI)) {
				fxPlayer = new MediaPlayer(new Media(mediaURI));
				fxPlayer.setVolume(volume);
				new Thread(null, new Runnable() {
					public void run() {
						try {
							Mp3File mp3 = new Mp3File(m.getMediaFile());
							if (mp3.hasId3v2Tag())
								artwork = ImageIO.read(new ByteArrayInputStream(mp3.getId3v2Tag().getAlbumImage()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, toString() + "-ArtworkLoader").start();
			}
			fxPlayer.setOnReady(onReady);
		} else if (isStopped())
			return;
		fxPlayer.play();
	}

	@Override
	public void pause() {
		if (!isStopped())
			fxPlayer.pause();
	}

	@Override
	public void stop() {
		if (!isStopped()) {
			fxPlayer.stop();
			fxPlayer = null;
		}
	}

	@Override
	public void seek(int time) {
		fxPlayer.seek(new Duration(time));
	}

	@Override
	public int getMediaLength() {
		return (int) fxPlayer.getStopTime().toMillis();
	}

	@Override
	public int getMediaTime() {
		return (int) fxPlayer.getCurrentTime().toMillis();
	}

	@Override
	public MediaFile getMedia() {
		return file;
	}

	@Override
	public boolean isPaused() {
		if (isStopped())
			return false;
		else
			return fxPlayer.getStatus() == MediaPlayer.Status.PAUSED;
	}

	@Override
	public boolean isStopped() {
		return fxPlayer == null || (fxPlayer.getStatus() == MediaPlayer.Status.STOPPED
				|| fxPlayer.getStatus() == MediaPlayer.Status.HALTED);
	}
	
	@Override
	public void setVolume(double volume) {
		this.volume = volume;
		if (fxPlayer != null)
			fxPlayer.setVolume(volume);
	}
	
	// Paint method
	@Override
	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		if (artwork != null) {
			if (scaleMode == ScaleMode.Stretch) {
				g.drawImage(artwork.getScaledInstance(getWidth(), getHeight(), Image.SCALE_AREA_AVERAGING), 0, 0,
						getWidth(), getHeight(), null);
			} else if (scaleMode == ScaleMode.Fill) {
				ImageTools.centerImage(artwork, getBounds(), g, true);
			} else if (scaleMode == ScaleMode.Fit) {
				ImageTools.centerImage(artwork, getBounds(), g, false);
			}
		}
	}
	
}
