/*
 JsJavaOpenSave: File system access in JavaScript via a Java applet
 JavaScript + Java Library

 Version: 0.1

 Copyright (C) 2014 Marnus Weststrate (marnusw@gmail.com)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package JsJavaOpenSave;

import java.applet.Applet;
import netscape.javascript.JSObject;

import javax.swing.JFileChooser;
import java.io.File;

public class JsJavaFileChooser extends Applet {

    /**
     * Open a file dialogue to select a folder from the local file system. If the file
     * dialogue is closed without selecting a folder an empty string is returned.
     *
     * @param params Parameters: initialPath (in), chosenFolder (out).
     */
    public void chooseFolder(JSObject params) {
        Object member = params.getMember("initialPath");
        String initialPath = member != null ? (String) member : "";

        File currentDirectory = new File(initialPath);
        JFileChooser chooser = new JFileChooser(currentDirectory);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select a download target folder");

        int result = chooser.showDialog(null, "Choose folder");
        params.setMember("chosenFolder",
                result == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile().getAbsolutePath() : ""
        );
    }
}
