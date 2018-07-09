package internal.swing.mediaplayers;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.swing.JPanel;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import utils.graphics.ImageTools;
import utils.graphics.ImageTools.ScaleMode;
import utils.io.file.Download;
import utils.io.file.MediaFile;
import utils.io.file.text.TextReader;

public class MusicPanel extends JPanel implements PlayerPanel {
	
	private static final long serialVersionUID = 1L;
	
	private String[] supportedExtensions;
	
	private CardLayout cl;
	private Container parent;
	
	private Image artwork;
	private MediaPlayer mp;
	
	private ScaleMode scaleMode = ScaleMode.Fit; // Default
	
	public MusicPanel(CardLayout cl, Container parent) {
		this.cl = cl;
		this.parent = parent;
		final File extFile = new File(System.getProperty("user.dir") + "/config/mediaplayers/music/extensions.txt");
		if (extFile.exists())
			supportedExtensions = new TextReader(extFile).read().replaceAll("\r", "").split("\\n");
		else {
			try {
				extFile.getParentFile().mkdirs();
				Download d = new Download(toString(), getClass().getResource("/resources/files/config/mediaplayers/music/extensions.txt"), extFile);
				d.start(new Runnable[] { new Runnable() {
					@Override
					public void run() {
						supportedExtensions = new TextReader(extFile).read().replaceAll("\r", "").split("\\n");
					}
				}});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		if (artwork != null) {
			if (scaleMode == ScaleMode.Stretch) {
				g.drawImage(artwork, 0, 0, getWidth(), getHeight(), null);
			} else if (scaleMode == ScaleMode.Fill) {
				ImageTools.centerImage(artwork, parent, g, true);
			} else if (scaleMode == ScaleMode.Fit) {
				ImageTools.centerImage(artwork, parent, g, false);
			}
		}
	}
	
	// Control methods
	@Override
	public void play(MediaFile m) {
		if (m != null) {
			String mediaURI = m.getFilePath().toURI().toString();
			if (isStopped() || !mp.getMedia().getSource().equals(mediaURI))
				mp = new MediaPlayer(new Media(mediaURI));
		} else if (isStopped())
			return;
		mp.play();
		cl.show(parent, "music");
	}

	@Override
	public void pause() {
		if (!isStopped())
			mp.pause();
	}

	@Override
	public void stop() {
		if (!isStopped()) {
			mp.stop();
			mp = null;
		}
	}
	
	public void setVolume(double percent) {
		if (!isStopped())
			mp.setVolume(percent);
	}
	
	// Status methods
	@Override
	public boolean isPaused() {
		if (isStopped())
			return false;
		else
			return mp.getStatus() == MediaPlayer.Status.PAUSED;
	}

	@Override
	public boolean isStopped() {
		return mp == null || (mp.getStatus() == MediaPlayer.Status.STOPPED || mp.getStatus() == MediaPlayer.Status.HALTED);
	}
	
	// Access methods
	@Override
	public String[] getSupportedExtensions() {
		return supportedExtensions;
	}

}
