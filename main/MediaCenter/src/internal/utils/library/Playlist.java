package internal.utils.library;

import java.awt.Component;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import internal.swing.mediaplayers.MusicPanel;
import internal.swing.mediaplayers.PlayerPanel;
import internal.swing.mediaplayers.VideoPanel;
import utils.io.file.MediaFile;
import utils.swing.ComponentMap;

public class Playlist {
	
	public boolean repeat = false;
	
	private ComponentMap mediaPlayers;
	private String currentPlayer = "image";
	
	private int index = 0;
	private ArrayList<MediaFile> list = new ArrayList<MediaFile>();
	
	// Constructors
	public Playlist(ComponentMap players) {
		mediaPlayers = players;
	}
	
	// Modification methods
	public Playlist add(MediaFile m) {
		list.add(m);
		return this;
	}
	
	public Playlist insert(MediaFile m, int index) {
		list.set(index, m);
		return this;
	}
	
	public Playlist remove(int index) {
		list.remove(index);
		return this;
	}
	
	public Playlist removeAll() {
		stop();
		list.clear();
		return this;
	}
	
	// Control methods
	public void play() {
		PlayerPanel player = getCurrentPlayer();
		if (!list.isEmpty())
			if (index >= list.size()) {
				index = 0;
				play(list.get(index));
			} else {
				if (!player.isStopped()) {
					if (player.isPaused())
						player.play(null);
				} else {
					play(list.get(index));
				}
			}
	}
	
	public void pause() {
		getCurrentPlayer().pause();
	}
	
	public void stop() {
		getCurrentPlayer().stop();
	}
	
	public void skipForward() {
		// TODO skip forward
		stop();
		if (index < list.size()) {
			index++;
			play();
		}
	}
	
	public void skipBackward() {
		// TODO skip backward
	}
	
	public void setVolume(double value) {
		((MusicPanel) mediaPlayers.getComponent("music")).setVolume(value);
		((VideoPanel) mediaPlayers.getComponent("video")).setVolume(value);
	}
	
	// Internal control methods
	private void play(MediaFile m) {
		String ext = "";
		Matcher extMatcher = Pattern.compile("(?<=\\.)[^\\\\\\/\\.]+(?=$)").matcher(m.getFilePath().toString());
		if (extMatcher.find())
			ext = extMatcher.group();
		for (Component c : mediaPlayers.getComponents().values()) {
			if (PlayerPanel.class.isInstance(c)) {
				PlayerPanel p = (PlayerPanel) c;
				for (String regex : p.getSupportedExtensions())
					if (ext.toLowerCase().matches(regex)) {
						p.play(m);
						currentPlayer = c.getName();
					} 
			}
		}	
	}
	
	// Access methods
	public ArrayList<MediaFile> getList() {
		return list;
	}
	
	public PlayerPanel getCurrentPlayer() {
		return (PlayerPanel) mediaPlayers.getComponent(currentPlayer);
	}
	
	public ComponentMap getComponentMap() {
		return mediaPlayers;
	}
	
	// Repeat methods
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	public boolean getRepeat() {
		return repeat;
	}
	
}
