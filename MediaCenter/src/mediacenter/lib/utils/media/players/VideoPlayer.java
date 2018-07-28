package mediacenter.lib.utils.media.players;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import mediacenter.lib.types.io.file.MediaFile;

public class VideoPlayer extends JPanel implements mediacenter.lib.utils.media.MediaPlayer {
	
	private static final long serialVersionUID = 1L;
	
	private MediaFile file;
	private Media fxMedia;
	private MediaPlayer fxPlayer;
	private MediaView fxView;
	private Scene fxScene;
	private JFXPanel fxPanel;
	private double volume;
	
	public VideoPlayer() {
		add(fxPanel = new JFXPanel());
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				if (fxMedia != null) {
					Platform.runLater(new Runnable() {
						public void run() {
							fxPanel.setScene(fxScene = new Scene(new Group(fxView), getWidth(), getHeight()));
							try {
								if (fxMedia.getWidth() / fxMedia.getHeight() < getWidth() / getHeight())
									fxView.fitHeightProperty().bind(fxScene.heightProperty());
								else
									fxView.fitWidthProperty().bind(fxScene.widthProperty());
							} catch (ArithmeticException e) { // Divide by zero
								fxView.fitWidthProperty().bind(fxScene.widthProperty());
							}
						}
					});
				}
			}
		});
	}
	
	@Override
	public String[] getSupportedFileExts() {
		return new String[] {
				"m4v", "mp4"
		};
	}

	@Override
	public void play(MediaFile m, Runnable onReady, Runnable onFinish) {
		if (m != null) {
			String mediaURI = (file = m).getMediaFile().toURI().toString();
			if (isStopped() || !fxPlayer.getMedia().getSource().equals(mediaURI)) {
				fxView = new MediaView(fxPlayer = new MediaPlayer(fxMedia = new Media(mediaURI)));
				Runnable onReadyNew = new Runnable() {
					public void run() {
						fxPanel.setScene(fxScene = new Scene(new Group(fxView), getWidth(), getHeight()));
						try {
							if (fxMedia.getWidth() / fxMedia.getHeight() < getWidth() / getHeight())
								fxView.fitHeightProperty().bind(fxScene.heightProperty());
							else
								fxView.fitWidthProperty().bind(fxScene.widthProperty());
						} catch (ArithmeticException e) { // Divide by zero
							fxView.fitWidthProperty().bind(fxScene.widthProperty());
						}
						fxView.setPreserveRatio(true);
						fxPlayer.setVolume(volume);
						onReady.run();
					}
				};
				fxPlayer.setOnReady(onReadyNew);
			}
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
	
	
	
}
