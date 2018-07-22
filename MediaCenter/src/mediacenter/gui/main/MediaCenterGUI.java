package mediacenter.gui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javafx.util.Duration;
import mediacenter.gui.applets.grabber.MediaGrabberGUI;
import mediacenter.gui.popups.frames.ComfirmPrompt;
import mediacenter.gui.popups.menus.JTextFieldPopupMenu;
import mediacenter.lib.types.MediaCenterApplet;
import mediacenter.lib.types.io.Download;
import mediacenter.lib.types.io.file.MediaFile;
import mediacenter.lib.types.media.Library;
import mediacenter.lib.types.media.Library.MediaType;
import mediacenter.lib.types.media.Playlist;
import mediacenter.lib.types.simple.SimpleList;
import mediacenter.lib.utils.io.DownloadManager;
import mediacenter.lib.utils.io.file.text.SimpleTextReader;
import mediacenter.lib.utils.media.PlayerPanel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MediaCenterGUI {
	
	private JFrame frmMediacenter;
	private JTabbedPane contentTabs;
	
	private Library library = new Library();
	private SimpleList<File> libraryDirs;
	private JPanel libraryPanel;
	private JTextField librarySearchBarField;
	private JComboBox<MediaType> librarySearchBarCombo;
	private JList<MediaFile> librarySearchResultsList;
	
	private JLabel libraryEditContentLabelFilename;
	private JTextField libraryEditContentFieldTitle;
	private JTextField libraryEditContentFieldAuthor;
	private JSpinner libraryEditContentSpinnerRating;
	private JTextField libraryEditContentFieldTags;
	
	private JTextField downloadSearchBarField;
	
	private PlayerPanel playerPanel;
	private Runnable onReady;
	private JButton controlButtonPlay;
	private ImageIcon playIcon, pauseIcon;
	private JSlider controlSeekSlider;
	private JLabel controlsSeekLabelTitle;
	private JLabel controlsSeekLabelStart;
	private JLabel controlsSeekLabelEnd;
	private JList<Download> downloadSearchResultsList;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MediaCenterGUI window = new MediaCenterGUI();
					window.frmMediacenter.setVisible(true);
					// Refresh library after window is visible
					window.libraryDirs = new SimpleList<File>(new File[] {new File("./documents/library")});
					File libraryDirsFile = new File("./documents/config/librarydirs.txt");
					if (libraryDirsFile.exists() && libraryDirsFile.isFile()) {
						String[] userDirs = new SimpleTextReader(libraryDirsFile).read().replace("\r", "").split("\n");
						for (String d : userDirs)
							window.libraryDirs.add(new File(d));
					} else {
						libraryDirsFile.getParentFile().mkdirs();
						libraryDirsFile.createNewFile();
					}
					window.library.refresh(window.libraryDirs);
					window.librarySearchResultsList.setListData(new Vector<MediaFile>(window.library.getContents().asList()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MediaCenterGUI() {
		onReady = new Runnable() {
			public void run() {
				Playlist p = playerPanel.getPlaylist();
				controlSeekSlider.setValue(0);
				controlSeekSlider.setMaximum(p.getEndTime());
				((CardLayout) playerPanel.getLayout()).show(playerPanel, p.getCurrentPlayer());
			}
		};
		playIcon = new ImageIcon(MediaCenterGUI.class.getResource("/mediacenter/resources/gui/main/icons/play.png"));
		pauseIcon = new ImageIcon(MediaCenterGUI.class.getResource("/mediacenter/resources/gui/main/icons/pause.png"));
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMediacenter = new JFrame();
		frmMediacenter.setTitle("MediaCenter");
		frmMediacenter.setMinimumSize(new Dimension(600, 400));
		frmMediacenter.setBounds(100, 100, 800, 500);
		frmMediacenter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentTabs = new JTabbedPane();
		contentTabs.setBorder(null);
		frmMediacenter.getContentPane().add(contentTabs, BorderLayout.CENTER);
		
		libraryPanel = new JPanel();
		libraryPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentTabs.addTab("Library", null, libraryPanel, null);
		libraryPanel.setLayout(new CardLayout(0, 0));
		
		JPanel librarySearchPanel = new JPanel();
		librarySearchPanel.setBorder(null);
		libraryPanel.add(librarySearchPanel, "search");
		librarySearchPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel librarySearchBarPanel = new JPanel();
		librarySearchBarPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		librarySearchPanel.add(librarySearchBarPanel, BorderLayout.NORTH);
		librarySearchBarPanel.setLayout(new BorderLayout(0, 0));
		
		librarySearchBarField = new JTextField();
		librarySearchBarField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				librarySearchResultsList
						.setListData(
								new Vector<MediaFile>(library
										.search(librarySearchBarField.getText(),
												librarySearchBarCombo
														.getItemAt(librarySearchBarCombo.getSelectedIndex()),
												playerPanel.getPlaylist())
										.asList()));
			}
		});
		librarySearchBarField.setComponentPopupMenu(new JTextFieldPopupMenu(librarySearchBarField));
		librarySearchBarPanel.add(librarySearchBarField, BorderLayout.CENTER);
		librarySearchBarField.setBorder(null);
		librarySearchBarField.setColumns(10);
		
		librarySearchBarCombo = new JComboBox<MediaType>();
		librarySearchBarCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				librarySearchResultsList
						.setListData(
								new Vector<MediaFile>(library
										.search(librarySearchBarField.getText(),
												librarySearchBarCombo
														.getItemAt(librarySearchBarCombo.getSelectedIndex()),
												playerPanel.getPlaylist())
										.asList()));
			}
		});
		librarySearchBarCombo.setModel(new DefaultComboBoxModel<MediaType>(MediaType.values()));
		librarySearchBarCombo.setBorder(null);
		librarySearchBarPanel.add(librarySearchBarCombo, BorderLayout.EAST);
		
		JScrollPane librarySearchResultsPane = new JScrollPane();
		librarySearchResultsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		librarySearchPanel.add(librarySearchResultsPane, BorderLayout.CENTER);
		
		librarySearchResultsList = new JList<MediaFile>();
		librarySearchResultsList.setBorder(null);
		librarySearchResultsList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					Playlist p = playerPanel.getPlaylist();
					p.stop();
					p.clear();
					p.add(librarySearchResultsList.getSelectedValuesList());
					p.play(onReady);
					controlButtonPlay.setIcon(pauseIcon);
					if (!p.getCurrentPlayer().equals("music"))
						contentTabs.setSelectedComponent(playerPanel);
				}
			}
		});
		librarySearchResultsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getClickCount() == 2) {
					Playlist p = playerPanel.getPlaylist();
					p.stop();
					p.clear();
					p.add(librarySearchResultsList.getSelectedValue());
					p.play(onReady);
					controlButtonPlay.setIcon(pauseIcon);
					if (!p.getCurrentPlayer().equals("music"))
						contentTabs.setSelectedComponent(playerPanel);
				}
			}
		});
		librarySearchResultsList.setComponentPopupMenu(new LibrarySearchResultsListPopup());
		librarySearchResultsPane.setViewportView(librarySearchResultsList);
		
		JPanel libraryEditPanel = new JPanel();
		libraryEditPanel.setBorder(null);
		libraryPanel.add(libraryEditPanel, "edit");
		libraryEditPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane libraryEditContentPane = new JScrollPane();
		libraryEditContentPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		libraryEditPanel.add(libraryEditContentPane, BorderLayout.CENTER);
		
		JPanel libraryEditContentPanel = new JPanel();
		libraryEditContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		libraryEditContentPane.setViewportView(libraryEditContentPanel);
		GridBagLayout gbl_libraryEditContentPanel = new GridBagLayout();
		gbl_libraryEditContentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_libraryEditContentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_libraryEditContentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_libraryEditContentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		libraryEditContentPanel.setLayout(gbl_libraryEditContentPanel);
		
		libraryEditContentLabelFilename = new JLabel("filename");
		GridBagConstraints gbc_libraryEditContentLabelFilename = new GridBagConstraints();
		gbc_libraryEditContentLabelFilename.gridwidth = 2;
		gbc_libraryEditContentLabelFilename.insets = new Insets(0, 0, 5, 5);
		gbc_libraryEditContentLabelFilename.gridx = 0;
		gbc_libraryEditContentLabelFilename.gridy = 0;
		libraryEditContentPanel.add(libraryEditContentLabelFilename, gbc_libraryEditContentLabelFilename);
		
		JLabel libraryEditContentLabelTitle = new JLabel("Title");
		GridBagConstraints gbc_libraryEditContentLabelTitle = new GridBagConstraints();
		gbc_libraryEditContentLabelTitle.insets = new Insets(0, 0, 5, 5);
		gbc_libraryEditContentLabelTitle.anchor = GridBagConstraints.EAST;
		gbc_libraryEditContentLabelTitle.gridx = 0;
		gbc_libraryEditContentLabelTitle.gridy = 1;
		libraryEditContentPanel.add(libraryEditContentLabelTitle, gbc_libraryEditContentLabelTitle);
		
		libraryEditContentFieldTitle = new JTextField();
		GridBagConstraints gbc_libraryEditContentFieldTitle = new GridBagConstraints();
		gbc_libraryEditContentFieldTitle.insets = new Insets(0, 0, 5, 0);
		gbc_libraryEditContentFieldTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryEditContentFieldTitle.gridx = 1;
		gbc_libraryEditContentFieldTitle.gridy = 1;
		libraryEditContentFieldTitle.setComponentPopupMenu(new JTextFieldPopupMenu(libraryEditContentFieldTitle));
		libraryEditContentPanel.add(libraryEditContentFieldTitle, gbc_libraryEditContentFieldTitle);
		libraryEditContentFieldTitle.setColumns(10);
		
		JLabel libraryEditContentLabelAuthor = new JLabel("Author");
		GridBagConstraints gbc_libraryEditContentLabelAuthor = new GridBagConstraints();
		gbc_libraryEditContentLabelAuthor.anchor = GridBagConstraints.EAST;
		gbc_libraryEditContentLabelAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_libraryEditContentLabelAuthor.gridx = 0;
		gbc_libraryEditContentLabelAuthor.gridy = 2;
		libraryEditContentPanel.add(libraryEditContentLabelAuthor, gbc_libraryEditContentLabelAuthor);
		
		libraryEditContentFieldAuthor = new JTextField();
		GridBagConstraints gbc_libraryEditContentFieldAuthor = new GridBagConstraints();
		gbc_libraryEditContentFieldAuthor.insets = new Insets(0, 0, 5, 0);
		gbc_libraryEditContentFieldAuthor.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryEditContentFieldAuthor.gridx = 1;
		gbc_libraryEditContentFieldAuthor.gridy = 2;
		libraryEditContentFieldAuthor.setComponentPopupMenu(new JTextFieldPopupMenu(libraryEditContentFieldAuthor));
		libraryEditContentPanel.add(libraryEditContentFieldAuthor, gbc_libraryEditContentFieldAuthor);
		libraryEditContentFieldAuthor.setColumns(10);
		
		JLabel libraryEditContentLabelRating = new JLabel("Rating");
		GridBagConstraints gbc_libraryEditContentLabelRating = new GridBagConstraints();
		gbc_libraryEditContentLabelRating.anchor = GridBagConstraints.EAST;
		gbc_libraryEditContentLabelRating.insets = new Insets(0, 0, 5, 5);
		gbc_libraryEditContentLabelRating.gridx = 0;
		gbc_libraryEditContentLabelRating.gridy = 3;
		libraryEditContentPanel.add(libraryEditContentLabelRating, gbc_libraryEditContentLabelRating);
		
		libraryEditContentSpinnerRating = new JSpinner();
		GridBagConstraints gbc_libraryEditContentSpinnerRating = new GridBagConstraints();
		gbc_libraryEditContentSpinnerRating.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryEditContentSpinnerRating.insets = new Insets(0, 0, 5, 0);
		gbc_libraryEditContentSpinnerRating.gridx = 1;
		gbc_libraryEditContentSpinnerRating.gridy = 3;
		libraryEditContentPanel.add(libraryEditContentSpinnerRating, gbc_libraryEditContentSpinnerRating);
		
		JLabel libraryEditContentLabelTags = new JLabel("Tags");
		GridBagConstraints gbc_libraryEditContentLabelTags = new GridBagConstraints();
		gbc_libraryEditContentLabelTags.anchor = GridBagConstraints.EAST;
		gbc_libraryEditContentLabelTags.insets = new Insets(0, 0, 0, 5);
		gbc_libraryEditContentLabelTags.gridx = 0;
		gbc_libraryEditContentLabelTags.gridy = 4;
		libraryEditContentPanel.add(libraryEditContentLabelTags, gbc_libraryEditContentLabelTags);
		
		libraryEditContentFieldTags = new JTextField();
		GridBagConstraints gbc_libraryEditContentFieldTags = new GridBagConstraints();
		gbc_libraryEditContentFieldTags.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryEditContentFieldTags.gridx = 1;
		gbc_libraryEditContentFieldTags.gridy = 4;
		libraryEditContentFieldTags.setComponentPopupMenu(new JTextFieldPopupMenu(libraryEditContentFieldTags));
		libraryEditContentPanel.add(libraryEditContentFieldTags, gbc_libraryEditContentFieldTags);
		libraryEditContentFieldTags.setColumns(10);
		
		JPanel libraryEditButtonPanel = new JPanel();
		libraryEditButtonPanel.setBorder(null);
		libraryEditPanel.add(libraryEditButtonPanel, BorderLayout.SOUTH);
		libraryEditButtonPanel.setLayout(new BorderLayout(0, 0));
		
		JButton libraryEditButtonBack = new JButton("Back");
		libraryEditButtonBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				((CardLayout) libraryPanel.getLayout()).show(libraryPanel, "search");
			}
		});
		libraryEditButtonPanel.add(libraryEditButtonBack, BorderLayout.WEST);
		
		JButton libraryEditButtonSave = new JButton("Save");
		libraryEditButtonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				MediaFile m = librarySearchResultsList.getSelectedValue();
				if (m != null) {
					m.setTag("title", libraryEditContentFieldTitle.getText());
					m.setTag("author", libraryEditContentFieldAuthor.getText());
					m.setTag("rating", libraryEditContentSpinnerRating.getValue().toString());
					m.setTag("tags", libraryEditContentFieldTags.getText());
					m.save();
				}
				((CardLayout) libraryPanel.getLayout()).show(libraryPanel, "search");
			}
		});
		libraryEditButtonPanel.add(libraryEditButtonSave, BorderLayout.CENTER);
		
		playerPanel = new PlayerPanel(new Runnable() {
			public void run() { // Runs while something is playing
				try {
					Playlist p = playerPanel.getPlaylist();
					controlSeekSlider.setValue(p.getCurrentTime());
					controlsSeekLabelTitle.setText(p.getCurrentMedia().getTag("title"));
					controlsSeekLabelStart.setText(new SimpleDateFormat(" mm:ss")
							.format(new Date((int) new Duration(p.getCurrentTime()).toMillis())));
					controlsSeekLabelEnd.setText(new SimpleDateFormat("mm:ss  ")
							.format(new Date((int) new Duration(p.getEndTime()).toMillis())));
				} catch (Exception e) {
					// Do Nothing
				}
			}
		});
		playerPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentTabs.addTab("Now Playing", null, playerPanel, null);
		
		JPanel appletPanel = new JPanel();
		appletPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentTabs.addTab("Applets", null, appletPanel, null);
		appletPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel appletSelectPanel = new JPanel();
		appletSelectPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		appletPanel.add(appletSelectPanel, BorderLayout.NORTH);
		appletSelectPanel.setLayout(new BorderLayout(0, 0));
		
		JComboBox<MediaCenterApplet> appletSelectCombo = new JComboBox<MediaCenterApplet>();
		appletSelectCombo.setBorder(null);
		appletSelectPanel.add(appletSelectCombo);
		
		JPanel appletViewPanel = new JPanel();
		appletViewPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		appletPanel.add(appletViewPanel, BorderLayout.CENTER);
		
		JPanel downloadPanel = new JPanel();
		downloadPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentTabs.addTab("Downloads", null, downloadPanel, null);
		downloadPanel.setLayout(new BorderLayout(0, 0));
		
		downloadSearchBarField = new JTextField();
		downloadSearchBarField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				String[] queries = downloadSearchBarField.getText().split("\\s");
				SimpleList<Download> results = new SimpleList<Download>();
				for (Download d : DownloadManager.getDownloads()) {
					// Check if URL contains all keywords
					String dURLString = d.getSource().toString();
					boolean containsAll = true;
					for (String q : queries)
						if (!dURLString.contains(q)) {
							containsAll = false;
							break;
						}
					if (containsAll)
						results.add(d);
				}
				downloadSearchResultsList.setListData(new Vector<Download>(results.asList()));
			}
		});
		downloadSearchBarField.setComponentPopupMenu(new JTextFieldPopupMenu(downloadSearchBarField));
		downloadPanel.add(downloadSearchBarField, BorderLayout.NORTH);
		downloadSearchBarField.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		downloadSearchBarField.setColumns(10);
		
		JScrollPane downloadSearchResultsPane = new JScrollPane();
		downloadSearchResultsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		downloadPanel.add(downloadSearchResultsPane, BorderLayout.CENTER);
		
		downloadSearchResultsList = new JList<Download>();
		downloadSearchResultsList.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent arg0) {
				for (ActionListener al : downloadSearchBarField.getActionListeners())
					al.actionPerformed(new ActionEvent(downloadSearchResultsList, 0, ""));
			}
		});
		downloadSearchResultsList.setBorder(null);
		downloadSearchResultsList.setComponentPopupMenu(new DownloadSearchResultsListPopup());
		downloadSearchResultsPane.setViewportView(downloadSearchResultsList);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(null);
		frmMediacenter.getContentPane().add(controlPanel, BorderLayout.SOUTH);
		controlPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel controlButtonPanel = new JPanel();
		controlButtonPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		controlPanel.add(controlButtonPanel, BorderLayout.WEST);
		controlButtonPanel.setLayout(new GridLayout(1, 0, 0, 0));
		
		JButton controlButtonBack = new JButton("");
		controlButtonBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				playerPanel.getPlaylist().skipBackward(onReady);
			}
		});
		controlButtonBack.setBorderPainted(false);
		controlButtonBack.setIcon(new ImageIcon(MediaCenterGUI.class.getResource("/mediacenter/resources/gui/main/icons/rewind.png")));
		controlButtonPanel.add(controlButtonBack);
		
		controlButtonPlay = new JButton("");
		controlButtonPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Playlist p = playerPanel.getPlaylist();
				if (p.isPaused() || p.isStopped())
					p.play(onReady);
				else
					p.pause();
				controlButtonPlay.setIcon(p.isPaused() ? pauseIcon : playIcon);
			}
		});
		controlButtonPlay.setBorderPainted(false);
		controlButtonPlay.setIcon(playIcon);
		controlButtonPanel.add(controlButtonPlay);
		
		JButton controlButtonForward = new JButton("");
		controlButtonForward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				playerPanel.getPlaylist().skipForward(onReady);
			}
		});
		controlButtonForward.setBorderPainted(false);
		controlButtonForward.setIcon(new ImageIcon(MediaCenterGUI.class.getResource("/mediacenter/resources/gui/main/icons/fast_forward.png")));
		controlButtonPanel.add(controlButtonForward);
		
		JPanel controlSeekPanel = new JPanel();
		controlSeekPanel.setBorder(null);
		controlPanel.add(controlSeekPanel, BorderLayout.CENTER);
		controlSeekPanel.setLayout(new BorderLayout(0, 0));
		
		controlSeekSlider = new JSlider();
		controlSeekSlider.setBorder(null);
		controlSeekSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent paramMouseEvent) {
				playerPanel.getPlaylist().seek(controlSeekSlider.getValue());
			}
		});
		controlSeekSlider.setValue(0);
		controlSeekSlider.setMaximum(0);
		controlSeekSlider.setPaintLabels(true);
		controlSeekPanel.add(controlSeekSlider, BorderLayout.SOUTH);
		
		controlsSeekLabelTitle = new JLabel("Nothing is Playing");
		controlsSeekLabelTitle.setBorder(null);
		controlsSeekLabelTitle.setHorizontalAlignment(SwingConstants.CENTER);
		controlSeekPanel.add(controlsSeekLabelTitle, BorderLayout.CENTER);
		
		controlsSeekLabelStart = new JLabel(" 00:00");
		controlsSeekLabelStart.setBorder(null);
		controlsSeekLabelStart.setFont(new Font("Dialog", Font.PLAIN, 12));
		controlSeekPanel.add(controlsSeekLabelStart, BorderLayout.WEST);
		
		controlsSeekLabelEnd = new JLabel("00:00  ");
		controlsSeekLabelEnd.setBorder(null);
		controlsSeekLabelEnd.setFont(new Font("Dialog", Font.PLAIN, 12));
		controlSeekPanel.add(controlsSeekLabelEnd, BorderLayout.EAST);
		
		JSlider controlSliderVolume = new JSlider();
		controlSliderVolume.setValue(100);
		controlSliderVolume.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				playerPanel.setVolume(controlSliderVolume.getValue() / 100.0);
			}
		});
		controlSliderVolume.setPreferredSize(new Dimension(100, 16));
		controlSliderVolume.setMaximumSize(new Dimension(100, 16));
		controlSliderVolume.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		controlPanel.add(controlSliderVolume, BorderLayout.EAST);
	}
	
	private class LibrarySearchResultsListPopup extends JPopupMenu {
		
		private static final long serialVersionUID = 1L;
		
		public LibrarySearchResultsListPopup() {
			// Play
			JMenuItem mntmOpen = new JMenuItem("Play");
			mntmOpen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Playlist p = playerPanel.getPlaylist();
					p.stop();
					p.clear();
					p.add(librarySearchResultsList.getSelectedValuesList());
					p.play(onReady);
					controlButtonPlay.setIcon(pauseIcon);
					if (!p.getCurrentPlayer().equals("music"))
						contentTabs.setSelectedComponent(playerPanel);
				}
			});
			add(mntmOpen);
			JMenuItem mntmSysDefault = new JMenuItem("Open With System Default");
			mntmSysDefault.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (Desktop.isDesktopSupported())
						try {
							Desktop.getDesktop().open(librarySearchResultsList.getSelectedValue().getMediaFile());
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			});
			add(mntmSysDefault);
			// Separator
			add(new JSeparator());
			// Play at time
			JMenuItem mntmPlayNext = new JMenuItem("Play Next");
			mntmPlayNext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Playlist p = playerPanel.getPlaylist();
					p.insert(p.getCurrentIndex(), librarySearchResultsList.getSelectedValuesList());
				}
			});
			add(mntmPlayNext);
			JMenuItem mntmPlayLater = new JMenuItem("Play Later");
			mntmPlayLater.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					playerPanel.getPlaylist().add(librarySearchResultsList.getSelectedValuesList());
				}
			});
			add(mntmPlayLater);
			// Separator
			add(new JSeparator());
			// Playlist options
			JMenuItem mntmAddToList = new JMenuItem("[WIP] Add to Playlist...");
			mntmAddToList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// TODO add to playlist
				}
			});
			add(mntmAddToList);
			// Separator
			add(new JSeparator());
			// Edit options
			JMenuItem mntmEdit = new JMenuItem("Edit");
			mntmEdit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					MediaFile m = librarySearchResultsList.getSelectedValue();
					if (m != null) {
						libraryEditContentLabelFilename.setText(m.getMediaFile().getName());
						libraryEditContentFieldTitle.setText(m.getTag("title"));
						libraryEditContentFieldAuthor.setText(m.getTag("author"));
						try {
							libraryEditContentSpinnerRating.setValue((int) Double.parseDouble(m.getTag("rating")));
						} catch (Exception e) { // Unknown was returned
							libraryEditContentSpinnerRating.setValue(0);
						}
						libraryEditContentFieldTags.setText(m.getTag("tags"));
					}
					((CardLayout) libraryPanel.getLayout()).show(libraryPanel, "edit");
				}
			});
			add(mntmEdit);
			JMenuItem mntmDelete = new JMenuItem("Delete");
			mntmDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					final List<MediaFile> sel = librarySearchResultsList.getSelectedValuesList();
					// Confirm deletion
					final ComfirmPrompt cp = new ComfirmPrompt("Confirm Deletion", String.format(
							"%s will be permanently deleted.",
							(sel.size() == 1 ? ("\"" + sel.get(0).toString() + "\"") : (sel.size() + " files"))));
					cp.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					// OK button
					cp.getOKButton().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							for (MediaFile m : sel) {
								m.getMediaFile().delete();
								m.getDataFile().getFile().delete();
							}
							library.refresh(libraryDirs);
							cp.dispose();
						}
					});
					// Cancel button
					cp.getCancelButton().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							cp.dispose();
						}
					});
					cp.setAlwaysOnTop(true);
					cp.setVisible(true);
				}
			});
			add(mntmDelete);
			JMenuItem mntmGoto = new JMenuItem("Go To Folder");
			mntmGoto.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					MediaFile m;
					if ((m = librarySearchResultsList.getSelectedValue()) != null)
						if (Desktop.isDesktopSupported())
							try {
								Desktop.getDesktop().open(m.getMediaFile().getParentFile());
							} catch (IOException e) {
								e.printStackTrace();
							}
				}
			});
			add(mntmGoto);
			// Separator
			add(new JSeparator());
			// Refresh library
			JMenuItem mntmRefresh = new JMenuItem("Refresh Library");
			mntmRefresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					library.refresh(libraryDirs);
				}
			});
			add(mntmRefresh);
			// Separator
			add(new JSeparator());
			JMenuItem mntmImport = new JMenuItem("Import...");
			mntmImport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					JFrame mediaGrabber = new JFrame();
					MediaGrabberGUI mediaGrabberGUI = new MediaGrabberGUI();
					mediaGrabberGUI.setLibrary(library, libraryDirs);
					mediaGrabberGUI.setPlaylist(playerPanel.getPlaylist());
					mediaGrabber.getContentPane().add(mediaGrabberGUI);
					mediaGrabber.setTitle("Import");
					mediaGrabber.setMinimumSize(new Dimension(400, 300));
					mediaGrabber.setBounds(110, 110, 600, 400);
					mediaGrabber.setVisible(true);
				}
			});
			add(mntmImport);
		}

	}
	
	private class DownloadSearchResultsListPopup extends JPopupMenu {
		
		private static final long serialVersionUID = 1L;
		
		public DownloadSearchResultsListPopup() {
			
		}
		
	}

}
