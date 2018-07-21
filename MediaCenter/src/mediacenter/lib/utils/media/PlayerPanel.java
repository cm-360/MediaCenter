package mediacenter.lib.utils.media;

import java.awt.CardLayout;

import javax.swing.JPanel;

import mediacenter.lib.types.media.Playlist;
import mediacenter.lib.types.simple.SimpleList;
import mediacenter.lib.utils.media.players.ImagePlayer;
import mediacenter.lib.utils.media.players.MusicPlayer;
import mediacenter.lib.utils.media.players.VideoPlayer;

public class PlayerPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private SimpleList<MediaPlayer> players = new SimpleList<MediaPlayer>();
	private Playlist playlist;
	
	// Constructor
	/**
	 * Create the panel.
	 */
	public PlayerPanel(Runnable whilePlaying) {
		setLayout(new CardLayout(0, 0));
		
		ImagePlayer playerImage = new ImagePlayer();
		playerImage.setName("image");
		players.add(playerImage);
		add(playerImage, playerImage.getName());
		
		MusicPlayer playerMusic = new MusicPlayer();
		playerMusic.setName("music");
		players.add(playerMusic);
		add(playerMusic, playerMusic.getName());
		
		VideoPlayer playerVideo = new VideoPlayer();
		playerVideo.setName("video");
		players.add(playerVideo);
		add(playerVideo, playerVideo.getName());
		
		playlist = new Playlist(players, whilePlaying);
	}
	
	public void setVolume(double volume) {
		for (MediaPlayer mp : players)
			mp.setVolume(volume);
	}
	
	// Access method
	public Playlist getPlaylist() {
		return playlist;
	}
	
}
