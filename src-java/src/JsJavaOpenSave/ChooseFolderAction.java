
package JsJavaOpenSave;

import java.security.PrivilegedAction;

import java.io.File;
import javax.swing.JFileChooser;

/**
 * Open a file dialogue to select a folder from the local file system.
 * If the file dialogue is closed without selecting a folder an empty 
 * string is returned.
 *
 * @author marnusw
 */
public class ChooseFolderAction implements PrivilegedAction<String> {

    private final String initialPath;

    public ChooseFolderAction() {
        this.initialPath = "";
    }

    public ChooseFolderAction(String initialPath) {
        this.initialPath = initialPath;
    }

    @Override
    public String run() {
        JFileChooser chooser = initialPath == null ? new JFileChooser() 
                                                   : new JFileChooser(new File((String)initialPath));
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select a download target folder");

        int result = chooser.showDialog(null, "Choose folder");
        return result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile().getAbsolutePath() : new String();
    }
}
