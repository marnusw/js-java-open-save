/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JsJavaOpenSave;

import java.security.PrivilegedAction;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author marnusw
 */
public class ChooseFolderAction implements PrivilegedAction {

    private final String initialPath;

    public ChooseFolderAction() {
        this.initialPath = "";
    }

    public ChooseFolderAction(String initialPath) {
        this.initialPath = initialPath;
    }

    @Override
    public Object run() {
        JFileChooser chooser = initialPath == null ? new JFileChooser() 
                                                   : new JFileChooser(new File((String)initialPath));
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select a download target folder");

        int result = chooser.showDialog(null, "Choose folder");
        return result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile().getAbsolutePath() : new String();
    }
}
