package mediacenter.gui.applets.grabber;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.OperationNotSupportedException;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mediacenter.gui.popups.menus.JTextPanePopupMenu;
import mediacenter.lib.types.io.file.MediaFile;
import mediacenter.lib.types.io.url.MediaURL;
import mediacenter.lib.types.media.Library;
import mediacenter.lib.types.media.Playlist;
import mediacenter.lib.types.simple.SimpleList;
import mediacenter.lib.types.simple.SimpleMap;
import mediacenter.lib.utils.io.DownloadManager;
import mediacenter.lib.utils.io.file.FileSearcher;
import mediacenter.lib.utils.io.file.filters.EverythingFilter;
import mediacenter.lib.utils.io.file.filters.JARFilter;
import mediacenter.lib.utils.io.file.filters.MediaFilter;
import mediacenter.lib.utils.io.file.text.SimpleTextReader;
import mediacenter.lib.utils.io.url.HTMLGrabber;
import mediacenter.lib.utils.io.url.JARLoader;
import mediacenter.lib.utils.media.MediaPlayer;
import mediacenter.lib.utils.text.StringTools;

public class MediaGrabberGUI extends JApplet {
	
	private static final long serialVersionUID = 1L;
	
	private SimpleList<DomainGrabber> grabbers = new SimpleList<DomainGrabber>();
	
	private JTextPane grabberSearchPathsText;
	
	private JList<MediaURL> grabberSearchResultsList;
	private JList<MediaURL> grabberEditResultsList;
	
	private JLabel grabberEditInfoContentLabelFilename;
	private JTextField grabberEditInfoContentFieldTitle;
	private JTextField grabberEditInfoContentFieldAuthor;
	private JSpinner grabberEditInfoContentSpinnerRating;
	private JTextField grabberEditInfoContentFieldTags;
	
	private Library lib;
	private SimpleList<File> libDirs;
	private Playlist playlist;
	
	
	/**
	 * Create the applet.
	 */
	public MediaGrabberGUI() {
		setSize(new Dimension(400, 300));
		setPreferredSize(new Dimension(400, 300));
		setMinimumSize(new Dimension(300, 200));
		
		JPanel grabberPanel = new JPanel();
		grabberPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(grabberPanel, BorderLayout.CENTER);
		grabberPanel.setLayout(new CardLayout(0, 0));
		
		JSplitPane grabberSearchPane = new JSplitPane();
		grabberSearchPane.setBorder(null);
		grabberSearchPane.setResizeWeight(0.75);
		grabberPanel.add(grabberSearchPane, "search");
		
		JPanel grabberSearchPathsPanel = new JPanel();
		grabberSearchPathsPanel.setBorder(null);
		grabberSearchPane.setLeftComponent(grabberSearchPathsPanel);
		grabberSearchPathsPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane grabberSearchPathsPane = new JScrollPane();
		grabberSearchPathsPanel.add(grabberSearchPathsPane, BorderLayout.CENTER);
		grabberSearchPathsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		grabberSearchPathsText = new JTextPane();
		grabberSearchPathsText.setComponentPopupMenu(new JTextPanePopupMenu(grabberSearchPathsText));
		grabberSearchPathsPane.setViewportView(grabberSearchPathsText);
		
		JLabel grabberSearchPathsLabel = new JLabel("Enter URLs or file paths below.");
		grabberSearchPathsPane.setColumnHeaderView(grabberSearchPathsLabel);
		
		JButton grabberSearchPathsButtonSearch = new JButton("Import");
		grabberSearchPathsButtonSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				String[] lines = grabberSearchPathsText.getText().replace("\r", "").split("\\n");
				SimpleList<MediaURL> results = new SimpleList<MediaURL>();
				for (String l : lines)
					results.add(grab(l));
				grabberSearchResultsList.setListData(results.asList().toArray(new MediaURL[results.length()]));
			}
		});
		grabberSearchPathsPanel.add(grabberSearchPathsButtonSearch, BorderLayout.SOUTH);
		
		JPanel grabberSearchResultsPanel = new JPanel();
		grabberSearchResultsPanel.setBorder(null);
		grabberSearchPane.setRightComponent(grabberSearchResultsPanel);
		grabberSearchResultsPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane grabberSearchResultsPane = new JScrollPane();
		grabberSearchResultsPanel.add(grabberSearchResultsPane);
		grabberSearchResultsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		grabberSearchResultsList = new JList<MediaURL>();
		grabberSearchResultsPane.setViewportView(grabberSearchResultsList);
		
		JLabel grabberSearchResultsLabel = new JLabel("Select the files to import below.");
		grabberSearchResultsPane.setColumnHeaderView(grabberSearchResultsLabel);
		
		JButton grabberSearchResultsButtonDownload = new JButton("Download");
		grabberSearchResultsButtonDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				List<MediaURL> sel = grabberSearchResultsList.getSelectedValuesList();
				if (!sel.isEmpty()) {
					grabberEditResultsList.setListData(sel.toArray(new MediaURL[sel.size()]));
					grabberEditResultsList.setSelectedIndex(0);
					((CardLayout) grabberPanel.getLayout()).show(grabberPanel, "edit");
				}
			}
		});
		grabberSearchResultsPanel.add(grabberSearchResultsButtonDownload, BorderLayout.SOUTH);
		
		JPanel grabberEditPanel = new JPanel();
		grabberEditPanel.setBorder(null);
		grabberPanel.add(grabberEditPanel, "edit");
		grabberEditPanel.setLayout(new BorderLayout(0, 0));
		
		JSplitPane grabberEditPane = new JSplitPane();
		grabberEditPane.setResizeWeight(0.5);
		grabberEditPane.setBorder(null);
		grabberEditPanel.add(grabberEditPane);
		
		JScrollPane grabberEditResultsPane = new JScrollPane();
		grabberEditResultsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		grabberEditPane.setLeftComponent(grabberEditResultsPane);
		
		grabberEditResultsList = new JList<MediaURL>();
		grabberEditResultsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent paramListSelectionEvent) {
				MediaURL m = grabberEditResultsList.getSelectedValue();
				if (m != null) {
					grabberEditInfoContentLabelFilename.setText(m.getFilename());
					grabberEditInfoContentFieldTitle.setText(m.getData().get("title"));
					grabberEditInfoContentFieldAuthor.setText(m.getData().get("author"));
					try {
						grabberEditInfoContentSpinnerRating.setValue((int) Double.parseDouble(m.getData().get("rating")));
					} catch (Exception e) { // Unknown was returned
						grabberEditInfoContentSpinnerRating.setValue(0);
					}
					grabberEditInfoContentFieldTags.setText(m.getData().get("tags"));
				}
			}
		});
		grabberEditResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		grabberEditResultsPane.setViewportView(grabberEditResultsList);
		
		JPanel grabberEditInfoPanel = new JPanel();
		grabberEditInfoPanel.setBorder(null);
		grabberEditPane.setRightComponent(grabberEditInfoPanel);
		grabberEditInfoPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane grabberEditInfoContentPane = new JScrollPane();
		grabberEditInfoContentPane.setBorder(null);
		grabberEditInfoContentPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		grabberEditInfoPanel.add(grabberEditInfoContentPane, BorderLayout.CENTER);
		
		JPanel grabberEditInfoContentPanel = new JPanel();
		grabberEditInfoContentPanel.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), new EmptyBorder(3, 3, 3, 3)));
		grabberEditInfoContentPane.setViewportView(grabberEditInfoContentPanel);
		GridBagLayout gbl_grabberEditInfoContentPanel = new GridBagLayout();
		gbl_grabberEditInfoContentPanel.columnWidths = new int[] {0, 0, 0};
		gbl_grabberEditInfoContentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_grabberEditInfoContentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_grabberEditInfoContentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		grabberEditInfoContentPanel.setLayout(gbl_grabberEditInfoContentPanel);
		
		grabberEditInfoContentLabelFilename = new JLabel("filename");
		GridBagConstraints gbc_grabberEditInfoContentLabelFilename = new GridBagConstraints();
		gbc_grabberEditInfoContentLabelFilename.gridwidth = 2;
		gbc_grabberEditInfoContentLabelFilename.insets = new Insets(0, 0, 5, 5);
		gbc_grabberEditInfoContentLabelFilename.gridx = 0;
		gbc_grabberEditInfoContentLabelFilename.gridy = 0;
		grabberEditInfoContentPanel.add(grabberEditInfoContentLabelFilename, gbc_grabberEditInfoContentLabelFilename);
		
		JLabel grabberEditInfoContentLabelTitle = new JLabel("Title");
		grabberEditInfoContentLabelTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
		grabberEditInfoContentLabelTitle.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_grabberEditInfoContentLabelTitle = new GridBagConstraints();
		gbc_grabberEditInfoContentLabelTitle.insets = new Insets(0, 0, 5, 5);
		gbc_grabberEditInfoContentLabelTitle.anchor = GridBagConstraints.EAST;
		gbc_grabberEditInfoContentLabelTitle.gridx = 0;
		gbc_grabberEditInfoContentLabelTitle.gridy = 1;
		grabberEditInfoContentPanel.add(grabberEditInfoContentLabelTitle, gbc_grabberEditInfoContentLabelTitle);
		
		grabberEditInfoContentFieldTitle = new JTextField();
		GridBagConstraints gbc_grabberEditInfoContentFieldTitle = new GridBagConstraints();
		gbc_grabberEditInfoContentFieldTitle.insets = new Insets(0, 0, 5, 0);
		gbc_grabberEditInfoContentFieldTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_grabberEditInfoContentFieldTitle.gridx = 1;
		gbc_grabberEditInfoContentFieldTitle.gridy = 1;
		grabberEditInfoContentPanel.add(grabberEditInfoContentFieldTitle, gbc_grabberEditInfoContentFieldTitle);
		grabberEditInfoContentFieldTitle.setColumns(10);
		
		JLabel grabberEditInfoContentLabelAuthor = new JLabel("Author");
		grabberEditInfoContentLabelAuthor.setFont(new Font("Dialog", Font.PLAIN, 12));
		grabberEditInfoContentLabelAuthor.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_grabberEditInfoContentLabelAuthor = new GridBagConstraints();
		gbc_grabberEditInfoContentLabelAuthor.anchor = GridBagConstraints.EAST;
		gbc_grabberEditInfoContentLabelAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_grabberEditInfoContentLabelAuthor.gridx = 0;
		gbc_grabberEditInfoContentLabelAuthor.gridy = 2;
		grabberEditInfoContentPanel.add(grabberEditInfoContentLabelAuthor, gbc_grabberEditInfoContentLabelAuthor);
		
		grabberEditInfoContentFieldAuthor = new JTextField();
		GridBagConstraints gbc_grabberEditInfoContentFieldAuthor = new GridBagConstraints();
		gbc_grabberEditInfoContentFieldAuthor.insets = new Insets(0, 0, 5, 0);
		gbc_grabberEditInfoContentFieldAuthor.fill = GridBagConstraints.HORIZONTAL;
		gbc_grabberEditInfoContentFieldAuthor.gridx = 1;
		gbc_grabberEditInfoContentFieldAuthor.gridy = 2;
		grabberEditInfoContentPanel.add(grabberEditInfoContentFieldAuthor, gbc_grabberEditInfoContentFieldAuthor);
		grabberEditInfoContentFieldAuthor.setColumns(10);
		
		JLabel grabberEditInfoContentLabelRating = new JLabel("Rating");
		grabberEditInfoContentLabelRating.setFont(new Font("Dialog", Font.PLAIN, 12));
		grabberEditInfoContentLabelRating.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_grabberEditInfoContentLabelRating = new GridBagConstraints();
		gbc_grabberEditInfoContentLabelRating.anchor = GridBagConstraints.EAST;
		gbc_grabberEditInfoContentLabelRating.insets = new Insets(0, 0, 5, 5);
		gbc_grabberEditInfoContentLabelRating.gridx = 0;
		gbc_grabberEditInfoContentLabelRating.gridy = 3;
		grabberEditInfoContentPanel.add(grabberEditInfoContentLabelRating, gbc_grabberEditInfoContentLabelRating);
		
		grabberEditInfoContentSpinnerRating = new JSpinner();
		grabberEditInfoContentSpinnerRating.setModel(new SpinnerNumberModel(0, 0, 5, 1));
		GridBagConstraints gbc_grabberEditInfoContentSpinnerRating = new GridBagConstraints();
		gbc_grabberEditInfoContentSpinnerRating.fill = GridBagConstraints.HORIZONTAL;
		gbc_grabberEditInfoContentSpinnerRating.insets = new Insets(0, 0, 5, 0);
		gbc_grabberEditInfoContentSpinnerRating.gridx = 1;
		gbc_grabberEditInfoContentSpinnerRating.gridy = 3;
		grabberEditInfoContentPanel.add(grabberEditInfoContentSpinnerRating, gbc_grabberEditInfoContentSpinnerRating);
		
		JLabel grabberEditInfoContentLabelTags = new JLabel("Tags");
		grabberEditInfoContentLabelTags.setFont(new Font("Dialog", Font.PLAIN, 12));
		grabberEditInfoContentLabelTags.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_grabberEditInfoContentLabelTags = new GridBagConstraints();
		gbc_grabberEditInfoContentLabelTags.anchor = GridBagConstraints.EAST;
		gbc_grabberEditInfoContentLabelTags.insets = new Insets(0, 0, 0, 5);
		gbc_grabberEditInfoContentLabelTags.gridx = 0;
		gbc_grabberEditInfoContentLabelTags.gridy = 4;
		grabberEditInfoContentPanel.add(grabberEditInfoContentLabelTags, gbc_grabberEditInfoContentLabelTags);
		
		grabberEditInfoContentFieldTags = new JTextField();
		GridBagConstraints gbc_grabberEditInfoContentFieldTags = new GridBagConstraints();
		gbc_grabberEditInfoContentFieldTags.fill = GridBagConstraints.HORIZONTAL;
		gbc_grabberEditInfoContentFieldTags.gridx = 1;
		gbc_grabberEditInfoContentFieldTags.gridy = 4;
		grabberEditInfoContentPanel.add(grabberEditInfoContentFieldTags, gbc_grabberEditInfoContentFieldTags);
		grabberEditInfoContentFieldTags.setColumns(10);
		
		JButton grabberEditInfoButtonApply = new JButton("Apply");
		grabberEditInfoButtonApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				MediaURL m = grabberEditResultsList.getSelectedValue();
				if (m != null) {
					m.getData().put("title", grabberEditInfoContentFieldTitle.getText());
					m.getData().put("author", grabberEditInfoContentFieldAuthor.getText());
					m.getData().put("rating", grabberEditInfoContentSpinnerRating.getValue().toString());
					m.getData().put("tags", grabberEditInfoContentFieldTags.getText());
				}
			}
		});
		grabberEditInfoPanel.add(grabberEditInfoButtonApply, BorderLayout.SOUTH);
		
		JPanel grabberEditButtonPanel = new JPanel();
		grabberEditButtonPanel.setBorder(null);
		grabberEditPanel.add(grabberEditButtonPanel, BorderLayout.SOUTH);
		grabberEditButtonPanel.setLayout(new BorderLayout(0, 0));
		
		JButton grabberEditButtonBack = new JButton("Back");
		grabberEditButtonBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				((CardLayout) grabberPanel.getLayout()).show(grabberPanel, "search");
			}
		});
		grabberEditButtonPanel.add(grabberEditButtonBack, BorderLayout.WEST);
		
		JButton grabberEditButtonDownload = new JButton("Download");
		grabberEditButtonDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				for (MediaURL m : grabberSearchResultsList.getSelectedValuesList()) {
					try {
						final String name = m.getFilename(), ext = name.substring(name.lastIndexOf("."));
						Runnable r = new Runnable() {
							public void run() {
								// Save the tags for the finished file
								new MediaFile(new File(String.format("./documents/library/%s%s",
										DownloadManager.getDownload(m.getURL()).getHash(), ext)), m.getData());
								lib.refresh(libDirs);
							}
						};
						DownloadManager.start(m.getURL(), new File("./documents/library/%HASH%" + ext), false, r);
					} catch (OperationNotSupportedException e) {
						e.printStackTrace(); // Should never be called
					}
				}
				((CardLayout) grabberPanel.getLayout()).show(grabberPanel, "search");
			}
		});
		grabberEditButtonPanel.add(grabberEditButtonDownload, BorderLayout.CENTER);

	}
	
	public SimpleList<MediaURL> grab(String path) {
		SimpleList<MediaURL> results = new SimpleList<MediaURL>();
		try { // Try as a URL
			URL url = new URL(path);
			for (DomainGrabber g : grabbers) { // Try each registered grabber
				if (path.matches(g.getSiteRegex()))
					results.add(g.search(url));
			}
			if (results.isEmpty()) { // Unspecialized search
				try {
					String html = HTMLGrabber.grab(url); // Get the pages html
					// Create a list of supported file extensions
					HashSet<String> exts = new HashSet<String>();
					for (MediaPlayer p : playlist.getPlayers())
						exts.addAll(Arrays.asList(p.getSupportedFileExts()));
					// Search for media files
					Matcher urlMatcher = Pattern.compile(
							"(?:https?:\\/\\/)(?:(?:[\\w-]+\\.)+[\\w-]+)(?:\\/[\\w-]+)+(?:\\.[\\w-]+)+(?:\\?(?:[\\w-]+(?:=[\\w-]+&?)?)+)?")
							.matcher(html);
					while (urlMatcher.find()) {
						// Make sure the file to download has a legal, recognized extension
						String group = urlMatcher.group(), ext = group.substring(group.lastIndexOf(".") + 1);
						if (ext.matches("[\\w-]+") && exts.contains(ext.toLowerCase())) {
							// Uses the html <title> tag and the filename as final title
							StringBuilder title = new StringBuilder();
							Matcher titleMatcher = Pattern.compile("(?<=<title>)[^<>]+(?=<\\/title>)").matcher(html);
							if (titleMatcher.find())
								title.append(titleMatcher.group().replaceAll("[^\\w\\s]", "") + " - ");
							title.append(String.format("%s.%s", StringTools.toTitleCase(
									group.substring(group.lastIndexOf("/") + 1, group.lastIndexOf("."))), ext));
							results.add(
									new MediaURL(new URL(group), title.toString(), guessData(lib, title.toString())));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e1) { // Line is not a URL
			File file = new File(path);
			if (file.exists()) { // Line is a valid file path
				if (file.isDirectory()) {
					MediaFilter mf = new MediaFilter();
					for (File f : FileSearcher.listFiles(file, new EverythingFilter(), true))
						if (mf.accept(f)) // Import media file
							results.add(grab(f.toString()));
						else { // Try as a text file
							String fName = f.getName(), fText = new SimpleTextReader(file).read();
							if (fName.endsWith(".url")) { // Internet shortcut
								Matcher urlMatcher = Pattern.compile("(?<=URL=)[^\\n\\r]+").matcher(fText);
								while (urlMatcher.find())
									results.add(grab(urlMatcher.group()));
							} else if (fName.endsWith(".txt")) { // Text file
								String[] lines = fText.replaceAll("\r", "").split("\\n");
								for (String l : lines)
									results.add(grab(l));
							}
						}
				} else { // Read as file
					try {
						URL fUrl = file.toURI().toURL();
						File tagFile = new File(file.toString().replaceAll("\\.[^\\\\\\/\\.]+$", ".json"));
						String fileName = file.getName();
						if (!tagFile.exists())
							results.add(new MediaURL(fUrl, fileName, guessData(lib, fileName)));
						else {
							// Read tags from file
							SimpleTextReader jsonReader = new SimpleTextReader(tagFile);
							Gson gson = new GsonBuilder().setPrettyPrinting().create();
							@SuppressWarnings("unchecked")
							HashMap<String, Object> gsonMap = gson.fromJson(jsonReader.read(), HashMap.class);
							SimpleMap<String, String> castMap = new SimpleMap<String, String>();
							if (gsonMap != null)
								for (String key : gsonMap.keySet()) // For compatibility
									castMap.put(key, gsonMap.get(key).toString().replaceAll("[^\\w\\s,\\.]", ""));
							results.add(new MediaURL(fUrl, fileName, castMap));
						}
					} catch (MalformedURLException e2) {
						e2.printStackTrace();
					}
				}
			}
		}
		return results;
	}
	
	public static SimpleMap<String, String> guessData(Library lib, String filename) {
		SimpleMap<String, String> results = new SimpleMap<String, String>();
		String nameClone = StringTools.removeSpecialChars(new String(filename).toLowerCase());
		// Check title for author
		for (final String s : lib.getDataValues("author")) {
			String s2 = StringTools.removeSpecialChars(s).toLowerCase().replaceAll("\\s+", " ");
			if (!s.equals("unknown") && nameClone.contains(s2)) {
				results.put("author", s);
				nameClone.replace(s2, "");
				break;
			}
		}
		// Check title for tags
		SimpleList<String> tags = new SimpleList<String>();
		for (final String ts : lib.getDataValues("tags"))
			for (final String s : ts.split(",")) {
				String s2 = StringTools.removeSpecialChars(s).toLowerCase().replaceAll("\\s+", " ");
				if (!(s2.equals("unknown") || tags.contains(s2)))
					tags.add(s2);
			}
		for (final String s : tags) {
			if (!s.equals("unknown") && nameClone.contains(s))
				results.put("tags", String.format("%s, ", results.get("tags"), s));
		}
		// Set title tag last
		results.put("title",
				StringTools.toTitleCase(StringTools.removeSpecialChars(nameClone)).replaceAll("\\s+", " "));
		return results;
	}
	
	public void setLibrary(final Library lib, final SimpleList<File> libDirs) {
		this.lib = lib;
		this.libDirs = libDirs;
		File addonDir = new File("./documents/applets/grabber/addons");
		if (addonDir.exists())
			grabbers = JARLoader.getInstances(DomainGrabber.class, "mediagrabber.GrabMedia",
					FileSearcher.listFiles(addonDir, new JARFilter(), true), new Object[] {});
		else
			addonDir.mkdirs();
		for (DomainGrabber g : grabbers)
			g.setLibrary(lib);
	}
	
	public void setPlaylist(final Playlist playlist) {
		this.playlist = playlist;
	}

}
