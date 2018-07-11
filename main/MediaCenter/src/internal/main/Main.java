package internal.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
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
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import internal.swing.guis.applets.grabber.GrabberGUI;
import internal.swing.guis.popups.BooleanPrompt;
import internal.swing.mediaplayers.PlayerPanel;
import internal.swing.mediaplayers.image.ImagePanel;
import internal.swing.mediaplayers.music.MusicPanel;
import internal.swing.mediaplayers.video.VideoPanel;
import internal.utils.library.Library;
import internal.utils.library.Playlist;
import javafx.embed.swing.JFXPanel;
import utils.io.file.MediaFile;
import utils.io.file.filters.MediaFilter;
import utils.io.file.text.TextReader;
import utils.swing.ComponentMap;

public class Main {

	private JFrame frmMediacenter;
	private JTabbedPane tabs;
	
	private Library library;
	private Playlist playlist;
	
	private CardLayout cl_panelLibrary = new CardLayout(0, 0);
	private JPanel panelLibrary;
	
	private JTextField fieldLibraryFilterSearch;
	private JComboBox<String> comboLibraryFilterTypes;
	private JList<MediaFile> listLibraryViewResults;
	
	private JPanel panelLibraryEdit;
	private MediaFile editingFile;
	private JLabel lblName;
	private JTextField fieldTitle;
	private JTextField fieldAuthor;
	private JSpinner spinnerRating;
	private JTextField fieldTags;
	
	private JPanel panelContent;
	private CardLayout cl_panelContent = new CardLayout(0, 0);
	private ComponentMap panelContentMap;
	
	private JPanel panelAppletsTab;
	private JPanel panelApplets;
	private CardLayout cl_panelApplets = new CardLayout(0, 0);
	private JComboBox<String> comboApplet;
	
	private GrabberGUI appletGrabber;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmMediacenter.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize(); // Swing initialization
		new JFXPanel(); // JFX Toolkit initialization
		// Library initialization
		panelContentMap = new ComponentMap(panelContent);
		playlist = new Playlist(panelContentMap);
		// Read included directories from config
		ArrayList<File> libDirs = new ArrayList<File>();
		libDirs.add(new File(System.getProperty("user.dir") + "/library"));
		File libDirsFile = new File(System.getProperty("user.dir") + "/config/library/alsoinclude.txt");
		if (libDirsFile.exists()) {
			TextReader libDirsFileReader = new TextReader(libDirsFile);
			for (String fs : libDirsFileReader.read().split("\\n"))
				libDirs.add(new File(fs));
			library = new Library(libDirs.toArray(new File[libDirs.size()]), new MediaFilter(), panelContentMap,
					listLibraryViewResults);
		} else {
			try {
				libDirsFile.getParentFile().mkdirs();
				libDirsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		listLibraryViewResults.setComponentPopupMenu(new LibraryContextMenu());
		appletGrabber = new GrabberGUI();
		appletGrabber.setLibrary(library);
		appletGrabber.setName("Media Grabber");
		panelApplets.add(appletGrabber, "grabber");
		for (Component c : panelApplets.getComponents())
			if (JApplet.class.isInstance(c))
				comboApplet.addItem(c.getName());
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMediacenter = new JFrame();
		frmMediacenter.setMinimumSize(new Dimension(600, 400));
		frmMediacenter.setTitle("MediaCenter");
		frmMediacenter.setBounds(100, 100, 800, 500);
		frmMediacenter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.setBorder(null);
		frmMediacenter.getContentPane().add(tabs, BorderLayout.CENTER);
		
		panelLibrary = new JPanel();
		panelLibrary.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tabs.addTab("Library", null, panelLibrary, null);
		tabs.setEnabledAt(0, true);
		panelLibrary.setLayout(cl_panelLibrary);
		
		JPanel panelLibraryView = new JPanel();
		panelLibrary.add(panelLibraryView, "results");
		panelLibraryView.setBorder(null);
		panelLibraryView.setLayout(new BorderLayout(0, 0));
		
		JPanel panelLibraryViewFilter = new JPanel();
		panelLibraryViewFilter.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelLibraryView.add(panelLibraryViewFilter, BorderLayout.NORTH);
		panelLibraryViewFilter.setLayout(new BorderLayout(0, 0));
		
		fieldLibraryFilterSearch = new JTextField();
		fieldLibraryFilterSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				library.search(fieldLibraryFilterSearch.getText(), comboLibraryFilterTypes.getSelectedIndex());
			}
		});
		fieldLibraryFilterSearch.setBorder(null);
		panelLibraryViewFilter.add(fieldLibraryFilterSearch, BorderLayout.CENTER);
		fieldLibraryFilterSearch.setColumns(10);
		
		comboLibraryFilterTypes = new JComboBox<String>();
		comboLibraryFilterTypes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				library.search(fieldLibraryFilterSearch.getText(), comboLibraryFilterTypes.getSelectedIndex());
			}
		});
		comboLibraryFilterTypes.setBorder(null);
		comboLibraryFilterTypes.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "Pictures", "Music", "Videos", "Playlists"}));
		panelLibraryViewFilter.add(comboLibraryFilterTypes, BorderLayout.EAST);
		
		JScrollPane paneLibraryViewResults = new JScrollPane();
		panelLibraryView.add(paneLibraryViewResults, BorderLayout.CENTER);
		
		listLibraryViewResults = new JList<MediaFile>();
		listLibraryViewResults.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) { // Enter
					playlist.stop();
					playlist.removeAll().add(listLibraryViewResults.getSelectedValue()).play();
					if (!((Component) playlist.getCurrentPlayer()).getName().toLowerCase().equals("music"))
						tabs.setSelectedComponent(panelContent);
				}
			}
		});
		listLibraryViewResults.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getClickCount() == 2) { // Double-click
					playlist.stop();
					playlist.removeAll().add(listLibraryViewResults.getSelectedValue()).play();
					if (!((Component) playlist.getCurrentPlayer()).getName().toLowerCase().equals("music"))
						tabs.setSelectedComponent(panelContent);
				}
			}
		});
		paneLibraryViewResults.setViewportView(listLibraryViewResults);
		
		panelLibraryEdit = new JPanel();
		panelLibrary.add(panelLibraryEdit, "edit");
		
		lblName = new JLabel("filename");
		lblName.setName("lblName");
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setHorizontalTextPosition(SwingConstants.CENTER);
		
		JLabel lblTitle = new JLabel("Title");
		
		JLabel lblAuthor = new JLabel("Author");
		
		JLabel lblRating = new JLabel("Rating");
		
		JLabel lblTags = new JLabel("Tags");
		
		fieldTitle = new JTextField();
		fieldTitle.setName("fieldTitle");
		fieldTitle.setColumns(10);
		
		fieldAuthor = new JTextField();
		fieldAuthor.setName("fieldAuthor");
		fieldAuthor.setColumns(10);
		
		spinnerRating = new JSpinner();
		spinnerRating.setName("spinnerRating");
		spinnerRating.setModel(new SpinnerNumberModel(0, 0, 5, 1));
		
		fieldTags = new JTextField();
		fieldTags.setName("fieldTags");
		fieldTags.setColumns(10);
		
		JButton btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (editingFile != null) {
					editingFile.setTag("title", fieldTitle.getText());
					editingFile.setTag("author", fieldAuthor.getText());
					editingFile.setTag("rating", Integer.parseInt(spinnerRating.getValue().toString()));
					editingFile.setTag("tags", new ArrayList<String>(Arrays.asList(fieldTags.getText().split(","))));
					editingFile.save();
				}
				cl_panelLibrary.show(panelLibrary, "results");
			}
		});
		
		JButton btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cl_panelLibrary.show(panelLibrary, "results");
			}
		});
		GroupLayout gl_panelLibraryEdit = new GroupLayout(panelLibraryEdit);
		gl_panelLibraryEdit.setHorizontalGroup(
			gl_panelLibraryEdit.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLibraryEdit.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelLibraryEdit.createSequentialGroup()
							.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblName, GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
								.addGroup(gl_panelLibraryEdit.createSequentialGroup()
									.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.LEADING)
										.addComponent(lblTitle)
										.addComponent(lblAuthor)
										.addComponent(lblRating)
										.addComponent(lblTags))
									.addGap(28)
									.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.LEADING)
										.addComponent(fieldTags, GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
										.addComponent(fieldTitle, GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
										.addComponent(fieldAuthor, GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
										.addComponent(spinnerRating, GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE))))
							.addContainerGap())
						.addGroup(gl_panelLibraryEdit.createSequentialGroup()
							.addComponent(btnBack)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnApply, GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
							.addContainerGap())))
		);
		gl_panelLibraryEdit.setVerticalGroup(
			gl_panelLibraryEdit.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLibraryEdit.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblName)
					.addGap(18)
					.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTitle)
						.addComponent(fieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAuthor)
						.addComponent(fieldAuthor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRating)
						.addComponent(spinnerRating, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTags)
						.addComponent(fieldTags, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 193, Short.MAX_VALUE)
					.addGroup(gl_panelLibraryEdit.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnApply)
						.addComponent(btnBack))
					.addContainerGap())
		);
		panelLibraryEdit.setLayout(gl_panelLibraryEdit);
		
		panelContent = new JPanel();
		panelContent.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tabs.addTab("Now Playing", null, panelContent, null);
		panelContent.setLayout(cl_panelContent);
		
		ImagePanel panelContentImage = new ImagePanel(cl_panelContent, panelContent);
		panelContentImage.setName("image");
		panelContent.add(panelContentImage, "image");
		panelContentImage.setLayout(new BorderLayout(0, 0));
		
		MusicPanel panelContentMusic = new MusicPanel(cl_panelContent, panelContent);
		panelContentMusic.setName("music");
		panelContent.add(panelContentMusic, "music");
		
		VideoPanel panelContentVideo = new VideoPanel(cl_panelContent, panelContent);
		panelContentVideo.setName("video");
		panelContent.add(panelContentVideo, "video");
		panelContentVideo.setLayout(new BoxLayout(panelContentVideo, BoxLayout.X_AXIS));
		tabs.setEnabledAt(1, true);
		
		JPanel panelControls = new JPanel();
		panelControls.setBorder(null);
		frmMediacenter.getContentPane().add(panelControls, BorderLayout.SOUTH);
		panelControls.setLayout(new BorderLayout(0, 0));
		
		JPanel panelControlsButtons = new JPanel();
		panelControlsButtons.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelControls.add(panelControlsButtons, BorderLayout.WEST);
		panelControlsButtons.setLayout(new GridLayout(0, 3, 0, 0));
		
		JButton btnControlsRW = new JButton("");
		btnControlsRW.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				playlist.skipBackward();
			}
		});
		btnControlsRW.setBorderPainted(false);
		btnControlsRW.setIcon(new ImageIcon(Main.class.getResource("/resources/images/media/skip_to_start-128.png")));
		panelControlsButtons.add(btnControlsRW);
		
		JButton btnControlsPP = new JButton("");
		btnControlsPP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PlayerPanel p = playlist.getCurrentPlayer();
				if (p.isPaused() || p.isStopped())
					playlist.play();
				else
					playlist.pause();
			}
		});
		btnControlsPP.setBorderPainted(false);
		btnControlsPP.setIcon(new ImageIcon(Main.class.getResource("/resources/images/media/play-128.png")));
		panelControlsButtons.add(btnControlsPP);
		
		JButton btnControlsFF = new JButton("");
		btnControlsFF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playlist.skipForward();
			}
		});
		btnControlsFF.setBorderPainted(false);
		btnControlsFF.setIcon(new ImageIcon(Main.class.getResource("/resources/images/media/end-128.png")));
		panelControlsButtons.add(btnControlsFF);
		
		JPanel panelControlsSeek = new JPanel();
		panelControlsSeek.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelControls.add(panelControlsSeek, BorderLayout.CENTER);
		panelControlsSeek.setLayout(new BorderLayout(0, 0));
		
		JSlider sliderControlsSeek = new JSlider();
		sliderControlsSeek.setMaximum(0);
		sliderControlsSeek.setValue(0);
		panelControlsSeek.add(sliderControlsSeek, BorderLayout.SOUTH);
		
		JLabel lblControlsTitle = new JLabel("Nothing is Playing");
		lblControlsTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblControlsTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		lblControlsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelControlsSeek.add(lblControlsTitle, BorderLayout.CENTER);
		
		JLabel lblControlsStart = new JLabel(" 0:00");
		lblControlsStart.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblControlsStart.setHorizontalTextPosition(SwingConstants.LEFT);
		lblControlsStart.setHorizontalAlignment(SwingConstants.LEFT);
		panelControlsSeek.add(lblControlsStart, BorderLayout.WEST);
		
		JLabel lblControlsEnd = new JLabel("0:00 ");
		lblControlsEnd.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblControlsEnd.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblControlsEnd.setHorizontalAlignment(SwingConstants.RIGHT);
		lblControlsEnd.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panelControlsSeek.add(lblControlsEnd, BorderLayout.EAST);
		
		final JSlider sliderControlsVolume = new JSlider();
		sliderControlsVolume.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent paramChangeEvent) {
				if (playlist != null) // Prevents NullPointerException
					playlist.setVolume(((double) sliderControlsVolume.getValue()) / 100);
			}
		});
		sliderControlsVolume.setPreferredSize(new Dimension(100, 16));
		sliderControlsVolume.setMaximumSize(new Dimension(100, 16));
		sliderControlsVolume.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		sliderControlsVolume.setValue(100);
		panelControls.add(sliderControlsVolume, BorderLayout.EAST);
		
		panelAppletsTab = new JPanel();
		panelAppletsTab.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tabs.addTab("Applets", null, panelAppletsTab, null);
		panelAppletsTab.setLayout(new BorderLayout(0, 0));
		
		JPanel panelAppletsChooser = new JPanel();
		panelAppletsChooser.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelAppletsTab.add(panelAppletsChooser, BorderLayout.NORTH);
		panelAppletsChooser.setLayout(new BorderLayout(0, 0));
		
		comboApplet = new JComboBox<String>();
		panelAppletsChooser.add(comboApplet);
		comboApplet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cl_panelApplets.show(panelApplets, comboApplet.getSelectedItem().toString());
			}
		});
		
		// Applets
		panelApplets = new JPanel();
		panelApplets.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelApplets.setLayout(cl_panelApplets);
		panelAppletsTab.add(panelApplets, BorderLayout.CENTER);
	}
	
	// listLibraryViewResults Context menu
	public class LibraryContextMenu extends JPopupMenu {
		
		private static final long serialVersionUID = 1L;
		
		public LibraryContextMenu() {
			// Play
			JMenuItem mntmOpen = new JMenuItem("Play");
			mntmOpen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					playlist.stop();
					playlist.removeAll();
					for (MediaFile m : listLibraryViewResults.getSelectedValuesList())
						playlist.add(m);
					playlist.play();
					if (!((Component) playlist.getCurrentPlayer()).getName().toLowerCase().equals("music"))
						tabs.setSelectedComponent(panelContent);
				}
			});
			add(mntmOpen);
			// Separator
			add(new JSeparator());
			// Play at time
			JMenuItem mntmPlayNext = new JMenuItem("Play Next");
			mntmPlayNext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					playlist.insert(listLibraryViewResults.getSelectedValue(), 0);
				}
			});
			add(mntmPlayNext);
			JMenuItem mntmPlayLater = new JMenuItem("Play Later");
			mntmPlayLater.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					playlist.add(listLibraryViewResults.getSelectedValue());
				}
			});
			add(mntmPlayLater);
			// Separator
			add(new JSeparator());
			// Playlist options
			JMenuItem mntmAddToList = new JMenuItem("Add to Playlist...");
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
					MediaFile m = (editingFile = listLibraryViewResults.getSelectedValue());
					if (m != null) {
						lblName.setText(m.getFilePath().getName()); // Filename
						// File tags
						HashMap<String, Object> mTags = m.getTags();
						fieldTitle.setText(m.toString());
						// Fill in author field
						Object mAuthorO = mTags.get("author");
						if (mAuthorO != null)
							fieldAuthor.setText(mAuthorO.toString());
						// Set rating spinner
						try {
							spinnerRating.setValue(Math.round(Double.parseDouble(mTags.get("rating").toString())));
						} catch (Exception e) {
							spinnerRating.setValue(0); // Default
						}
						// Fill in tags list
						Object mTagsO = mTags.get("tags");
						if (mTagsO != null && ArrayList.class.isInstance(mTagsO)) {
							@SuppressWarnings("unchecked")
							ArrayList<String> mSearchTags = (ArrayList<String>) mTagsO;
							fieldTags.setText(mSearchTags.toString().replaceAll("[\\[\\]\\s]", ""));
						}
						// Show when done
						((CardLayout) panelLibrary.getLayout()).show(panelLibrary, "edit");
					}
				}
			});
			add(mntmEdit);
			JMenuItem mntmDelete = new JMenuItem("Delete");
			mntmDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					final List<MediaFile> sel = listLibraryViewResults.getSelectedValuesList();
					// Confirm deletion
					final BooleanPrompt bp = new BooleanPrompt("Confirm Deletion", String.format(
							"%s will be permanently deleted.",
							(sel.size() == 1 ? ("\"" + sel.get(0).toString() + "\"") : (sel.size() + " files"))));
					bp.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					bp.getOKButton().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							for (MediaFile m : sel) {
								m.getFilePath().delete();
								m.getTagsPath().delete();
							}
							library.refresh();
						}
					});
					bp.getCancelButton().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							bp.dispose();
						}
					});
					bp.setAlwaysOnTop(true);
					bp.setVisible(true);
				}
			});
			add(mntmDelete);
			JMenuItem mntmGoto = new JMenuItem("Go To Folder");
			mntmGoto.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					MediaFile m;
					if ((m = listLibraryViewResults.getSelectedValue()) != null)
						if (Desktop.isDesktopSupported())
							try {
								Desktop.getDesktop().open(m.getFilePath().getParentFile());
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
					library.refresh();
				}
			});
			add(mntmRefresh);
		}
		
	}
	
}
