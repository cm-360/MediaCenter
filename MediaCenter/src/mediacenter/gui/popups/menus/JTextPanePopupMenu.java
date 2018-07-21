package mediacenter.gui.popups.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextPane;

import mediacenter.lib.utils.text.ClipboardTools;

public class JTextPanePopupMenu extends JPopupMenu {
	
	private static final long serialVersionUID = 1L;

	public JTextPanePopupMenu(final JTextPane p) {
		// Cut and copy
		JMenuItem mntmCut = new JMenuItem("Cut");
		mntmCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String text = p.getText();
				if (!text.isEmpty()) // Copy to clipboard
					ClipboardTools.setClipboardText(text.substring(p.getSelectionStart(), p.getSelectionEnd()));
				// Remove from text pane
				p.setText(new StringBuilder(text).replace(p.getSelectionStart(), p.getSelectionEnd(), "").toString());
			}
		});
		add(mntmCut);
		JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String text = p.getText();
				if (!text.isEmpty()) // Copy to clipboard
					ClipboardTools.setClipboardText(text.substring(p.getSelectionStart(), p.getSelectionEnd()));
			}
		});
		add(mntmCopy);
		// Separator
		add(new JSeparator());
		// Paste
		JMenuItem mntmPaste = new JMenuItem("Paste");
		mntmPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String clip = ClipboardTools.getClipboardText();
				if (!clip.isEmpty()) // Paste from clipboard
					p.setText(new StringBuilder(p.getText()).replace(p.getSelectionStart(), p.getSelectionEnd(), "")
							.insert(p.getSelectionStart(), clip).toString());
			}
		});
		add(mntmPaste);
	}
	
}
