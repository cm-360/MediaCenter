package internal.swing.guis.applets.grabber;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import internal.swing.guis.popups.TextPrompt;
import internal.utils.library.Library;
import utils.applets.MediaCenterApplet;
import utils.io.DownloadManager;
import utils.io.file.Download;
import utils.io.url.MediaGrabber;
import utils.io.url.MediaURL;

public class GrabberGUI extends MediaCenterApplet {
	
	private static final long serialVersionUID = 1L;
	
	private JPanel panelCards;
	private CardLayout cl_panelCards = new CardLayout(0, 0);
	
	private DownloadManager downloads = new DownloadManager();
	private MediaGrabber mediaGrabber = new MediaGrabber(null, downloads,
			new File(System.getProperty("user.dir") + "/applets/grabber/addons"), true);
	
	private JLabel lblName;
	private JTextField fieldTitle;
	private JTextField fieldAuthor;
	private JSpinner spinnerRating;
	private JTextField fieldTags;
	
	private JList<MediaURL> listOutput;
	private JList<MediaURL> listDownloads;
	
	private JTextArea textInput;
	
	/**
	 * Create the applet.
	 */
	public GrabberGUI() {
		setSize(new Dimension(600, 400));
		setMinimumSize(new Dimension(400, 300));
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabs, BorderLayout.CENTER);
		
		panelCards = new JPanel();
		tabs.addTab("Search", null, panelCards, null);
		tabs.setEnabledAt(0, true);
		panelCards.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelCards.setLayout(cl_panelCards);
		
		JSplitPane cardSearch = new JSplitPane();
		cardSearch.setBorder(null);
		panelCards.add(cardSearch, "search");
		cardSearch.setResizeWeight(0.5);
		
		JPanel panelInput = new JPanel();
		cardSearch.setLeftComponent(panelInput);
		panelInput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollInput = new JScrollPane();
		scrollInput.setBorder(null);
		scrollInput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panelInput.add(scrollInput, BorderLayout.CENTER);
		
		textInput = new JTextArea();
		scrollInput.setViewportView(textInput);
		
		JLabel lblInput = new JLabel("Input URLs to search below.");
		scrollInput.setColumnHeaderView(lblInput);
		
		JPanel panelInputButtons = new JPanel();
		panelInput.add(panelInputButtons, BorderLayout.SOUTH);
		panelInputButtons.setLayout(new BorderLayout(0, 0));
		
		JButton btnInput = new JButton("Search");
		panelInputButtons.add(btnInput);
		
		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final TextPrompt tp = new TextPrompt("Import From Files");
				tp.getOKButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ArrayList<MediaURL> results = new ArrayList<MediaURL>();
						String[] fileStrings = tp.getText().split("\\n"); // Separate by line
						for (String fs : fileStrings) {
							File f = new File(fs);
							results.addAll(mediaGrabber.search(f));
						}
						listOutput.setListData(results.toArray(new MediaURL[results.size()]));
						tp.dispose();
					}
				});
				tp.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				tp.setVisible(true);
			}
		});
		panelInputButtons.add(btnImport, BorderLayout.WEST);
		btnInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				ArrayList<MediaURL> results = new ArrayList<MediaURL>();
				String[] urlStrings = textInput.getText().split("\\n"); // Separate by line
				for (String urlString : urlStrings)
					try {
						results.addAll(mediaGrabber.search(new URL(urlString)));
					} catch (IOException e) {
						e.printStackTrace();
					}
				listOutput.setListData(results.toArray(new MediaURL[results.size()]));
			}
		});
		
		JPanel panelOutput = new JPanel();
		cardSearch.setRightComponent(panelOutput);
		panelOutput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollOutput = new JScrollPane();
		scrollOutput.setBorder(null);
		scrollOutput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panelOutput.add(scrollOutput, BorderLayout.CENTER);
		
		listOutput = new JList<MediaURL>();
		scrollOutput.setViewportView(listOutput);
		
		JLabel lblOutput = new JLabel("Select the files to download below.");
		scrollOutput.setColumnHeaderView(lblOutput);
		
		JButton btnOutput = new JButton("Download");
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				List<MediaURL> s = listOutput.getSelectedValuesList();
				listDownloads.setListData(s.toArray(new MediaURL[s.size()]));
				listDownloads.setSelectedIndex(0);
				cl_panelCards.show(panelCards, "edit");
			}
		});
		panelOutput.add(btnOutput, BorderLayout.SOUTH);
		
		JPanel cardEdit = new JPanel();
		cardEdit.setBorder(null);
		panelCards.add(cardEdit, "edit");
		cardEdit.setLayout(new BorderLayout(0, 0));
		
		JSplitPane paneEdit = new JSplitPane();
		paneEdit.setResizeWeight(0.5);
		paneEdit.setBorder(null);
		cardEdit.add(paneEdit, BorderLayout.CENTER);
		
		JScrollPane scrollDownloads = new JScrollPane();
		scrollDownloads.setBorder(null);
		paneEdit.setLeftComponent(scrollDownloads);
		scrollDownloads.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		listDownloads = new JList<MediaURL>();
		listDownloads.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent paramListSelectionEvent) {
				MediaURL m = listDownloads.getSelectedValue();
				if (m != null) {
					lblName.setText(m.getFilename());
					HashMap<String, Object> mTags = m.getTags();
					// Fill in name field
					Object mNameO = mTags.get("title");
					String mFilename = m.getFilename(), mName = mFilename.substring(0, mFilename.lastIndexOf("."));
					if (mNameO != null)
						mName = mNameO.toString();
					else
						mTags.put("title", mName);
					fieldTitle.setText(mName);
					// Fill in author field
					Object mAuthorO = mTags.get("author");
					if (mAuthorO != null)
						fieldAuthor.setText(mAuthorO.toString());
					// Set rating spinner
					try {
						spinnerRating.setValue(Integer.parseInt(mTags.get("rating").toString()));
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
				}
			}
		});
		listDownloads.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollDownloads.setViewportView(listDownloads);
		
		JPanel panelInfo = new JPanel();
		panelInfo.setBorder(null);
		paneEdit.setRightComponent(panelInfo);
		
		lblName = new JLabel("filename");
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblTitle = new JLabel("Title");
		
		JLabel lblAuthor = new JLabel("Author");
		
		JLabel lblRating = new JLabel("Rating");
		
		JLabel lblTags = new JLabel("Tags");
		
		fieldTitle = new JTextField();
		fieldTitle.setColumns(10);
		
		fieldAuthor = new JTextField();
		fieldAuthor.setColumns(10);
		
		spinnerRating = new JSpinner();
		spinnerRating.setModel(new SpinnerNumberModel(0, 0, 5, 1));
		
		fieldTags = new JTextField();
		fieldTags.setColumns(10);
		
		JButton btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MediaURL m = listDownloads.getSelectedValue();
				HashMap<String, Object> mTags = m.getTags();
				mTags.put("title", fieldTitle.getText());
				String oldName = m.getFilename();
				m.setFilename(fieldTitle.getText().replaceAll("[^\\w\\s\\.-]", "_") + oldName.substring(oldName.lastIndexOf(".")));
				mTags.put("author", fieldAuthor.getText());
				mTags.put("rating", Integer.parseInt(spinnerRating.getValue().toString()));
				mTags.put("tags", new ArrayList<String>(Arrays.asList(fieldTags.getText().split(","))));
			}
		});
		GroupLayout gl_panelInfo = new GroupLayout(panelInfo);
		gl_panelInfo.setHorizontalGroup(
			gl_panelInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelInfo.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelInfo.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelInfo.createSequentialGroup()
							.addComponent(btnApply, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(gl_panelInfo.createSequentialGroup()
							.addGroup(gl_panelInfo.createParallelGroup(Alignment.LEADING)
								.addComponent(lblName, GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
								.addGroup(gl_panelInfo.createSequentialGroup()
									.addGroup(gl_panelInfo.createParallelGroup(Alignment.LEADING)
										.addComponent(lblAuthor)
										.addComponent(lblTitle))
									.addGap(24)
									.addGroup(gl_panelInfo.createParallelGroup(Alignment.LEADING)
										.addComponent(fieldTitle, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
										.addComponent(fieldAuthor, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
										.addComponent(spinnerRating, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
										.addComponent(fieldTags, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
									.addGap(15)))
							.addGap(0))
						.addComponent(lblRating)
						.addGroup(gl_panelInfo.createSequentialGroup()
							.addComponent(lblTags)
							.addContainerGap(303, Short.MAX_VALUE))))
		);
		gl_panelInfo.setVerticalGroup(
			gl_panelInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelInfo.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblName)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTitle)
						.addComponent(fieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAuthor)
						.addComponent(fieldAuthor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRating)
						.addComponent(spinnerRating, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTags)
						.addComponent(fieldTags, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 176, Short.MAX_VALUE)
					.addComponent(btnApply)
					.addContainerGap())
		);
		panelInfo.setLayout(gl_panelInfo);
		
		JPanel panelEditButtons = new JPanel();
		cardEdit.add(panelEditButtons, BorderLayout.SOUTH);
		panelEditButtons.setLayout(new BorderLayout(0, 0));
		
		JButton btnConfirm = new JButton("Confirm");
		panelEditButtons.add(btnConfirm);
		
		JButton btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cl_panelCards.show(panelCards, "search");
				// Empty the downloads list
				listDownloads.clearSelection();
				listDownloads.setListData(new MediaURL[] {});
			}
		});
		panelEditButtons.add(btnBack, BorderLayout.WEST);
		
		JPanel panelDownloads = new JPanel();
		panelDownloads.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tabs.addTab("Downloads", null, panelDownloads, null);
		tabs.setEnabledAt(1, true);
		panelDownloads.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollDownloading = new JScrollPane();
		scrollDownloading.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollDownloading.setBorder(null);
		panelDownloads.add(scrollDownloading, BorderLayout.CENTER);
		
		final JList<Download> listDownloading = new JList<Download>();
		scrollDownloading.setViewportView(listDownloading);
		btnConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				ListModel<MediaURL> listModel = listDownloads.getModel();
				for (int i = 0; i < listModel.getSize(); i++)
					mediaGrabber.download(listModel.getElementAt(i), new File(System.getProperty("user.dir") + "/applets/grabber/downloads")); // TODO read from options
				Collection<Download> dc = downloads.getDownloads();
				listDownloading.setListData(dc.toArray(new Download[dc.size()]));
				cl_panelCards.show(panelCards, "search");
				// Empty the downloads list
				listDownloads.clearSelection();
				listDownloads.setListData(new MediaURL[] {});
			}
		});
	}
	
	// Access method
	public void setLibrary(Library lib) {
		mediaGrabber.setLibrary(lib);
	}
}
