package mediacenter.gui.popups.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import mediacenter.lib.utils.text.ClipboardTools;

public class JTextFieldPopupMenu extends JPopupMenu {
	
	private static final long serialVersionUID = 1L;

	public JTextFieldPopupMenu(final JTextField f) {
		// Cut and copy
		JMenuItem mntmCut = new JMenuItem("Cut");
		mntmCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String text = f.getText();
				if (!text.isEmpty()) // Copy to clipboard
					ClipboardTools.setClipboardText(text.substring(f.getSelectionStart(), f.getSelectionEnd()));
				// Remove from text pane
				f.setText(new StringBuilder(text).replace(f.getSelectionStart(), f.getSelectionEnd(), "").toString());
			}
		});
		add(mntmCut);
		JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String text = f.getText();
				if (!text.isEmpty()) // Copy to clipboard
					ClipboardTools.setClipboardText(text.substring(f.getSelectionStart(), f.getSelectionEnd()));
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
					f.setText(new StringBuilder(f.getText()).replace(f.getSelectionStart(), f.getSelectionEnd(), "")
							.insert(f.getSelectionStart(), clip).toString());
			}
		});
		add(mntmPaste);
	}
	
}
