package internal.swing.mediaplayers.video;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import internal.swing.mediaplayers.PlayerPanel;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import utils.io.file.Download;
import utils.io.file.MediaFile;
import utils.io.file.text.TextReader;

public class VideoPanel extends JPanel implements PlayerPanel {
	
	private static final long serialVersionUID = 1L;
	
	private String[] supportedExtensions;
	
	private CardLayout cl;
	private Container parent;
	
	private JFXPanel fxPanel;
	private MediaPlayer player;
	
	private boolean sysDefault = true; // TODO read this from options
	
	public VideoPanel(CardLayout cl, Container parent) {
		this.cl = cl;
		this.parent = parent;
		final File extFile = new File(System.getProperty("user.dir") + "/config/mediaplayers/video/extensions.txt");
		if (extFile.exists())
			supportedExtensions = new TextReader(extFile).read().replaceAll("\r", "").split("\\n");
		else {
			try {
				extFile.getParentFile().mkdirs();
				Download d = new Download(toString(), getClass().getResource("/resources/files/config/mediaplayers/video/extensions.txt"), extFile);
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
		add(fxPanel = new JFXPanel());
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
	}
	
	// Control methods
	@Override
	public void play(MediaFile m) {
		if (sysDefault) {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().open(m.getFilePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				MediaView mv = new MediaView(player = new MediaPlayer(new Media(m.getFilePath().toURI().toURL().toString())));
				Scene scene;
				fxPanel.setScene(scene = new Scene(new Group(mv), parent.getWidth(), parent.getHeight()));
				mv.fitWidthProperty().bind(scene.widthProperty());
				mv.fitHeightProperty().bind(scene.heightProperty());
				mv.setPreserveRatio(true);
				player.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Show panel when done
		cl.show(parent, "video");
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void stop() {
		player.stop();
	}
	
	public void setVolume(double percent) {
		player.setVolume(percent);
	}
	
	// Status methods
	@Override
	public boolean isPaused() {
		return false;
	}

	@Override
	public boolean isStopped() {
		return true;
	}
	
	// Access methods
	@Override
	public String[] getSupportedExtensions() {
		return supportedExtensions;
	}

}
