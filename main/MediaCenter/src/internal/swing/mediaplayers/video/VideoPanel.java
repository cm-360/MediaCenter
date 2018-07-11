package internal.swing.mediaplayers.video;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
	private Media fxMedia;
	private MediaPlayer fxPlayer;
	private MediaView fxView;
	private Scene fxScene;
	
	private boolean sysDefault = false; // TODO read this from options
	
	public VideoPanel(CardLayout cl, final Container parent) {
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
		parent.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				if (fxMedia != null)
					if (fxMedia.getHeight() / parent.getHeight() > 1)
						fxView.fitHeightProperty().bind(fxScene.heightProperty());
					else
						fxView.fitWidthProperty().bind(fxScene.widthProperty());
			}
		});
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
			if (m != null) {
				fxMedia = new Media(m.getFilePath().toURI().toString());
				if (fxPlayer == null || fxMedia.getSource() != fxPlayer.getMedia().getSource()) // Play a new file
					try {
						fxView = new MediaView(fxPlayer = new MediaPlayer(fxMedia));
						fxPanel.setScene(fxScene = new Scene(new Group(fxView), parent.getWidth(), parent.getHeight()));
						if (fxMedia.getHeight() / parent.getHeight() > 1)
							fxView.fitHeightProperty().bind(fxScene.heightProperty());
						else
							fxView.fitWidthProperty().bind(fxScene.widthProperty());
						fxView.setPreserveRatio(true);
					} catch (Exception e) {
						e.printStackTrace();
					} 
			}
			fxPlayer.play();
		}
		// Show panel when done
		cl.show(parent, "video");
	}

	@Override
	public void pause() {
		fxPlayer.pause();
	}

	@Override
	public void stop() {
		fxPlayer.stop();
	}
	
	public void setVolume(double percent) {
		fxPlayer.setVolume(percent);
	}
	
	// Status methods
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
	
	// Access methods
	@Override
	public String[] getSupportedExtensions() {
		return supportedExtensions;
	}

}
