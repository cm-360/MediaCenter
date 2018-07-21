package mediacenter.lib.utils.text;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ClipboardTools {
	
	public static String getClipboardText() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static void setClipboardText(String contents) {
		StringSelection ss = new StringSelection(contents);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
	}
	
}
