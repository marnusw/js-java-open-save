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
import java.security.AccessController;

import java.util.Map;
import java.util.HashMap;

// For this import to resolve include jre\lib\plugin.jar as a compile-time library of the project.
import netscape.javascript.JSObject;

public class JsJavaOpenSave extends Applet {

    private final Map<Integer, DownloadStatus> downloads = new HashMap<>();
    private int idCounter = 0;

    /**
     * In Java 7 IPv6 traffic is preferred which causes a download over IPv4 to hang on some operating systems.
     */
    @Override
    public void init() {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /**
     * Open a file dialogue to select a folder from the local file system. 
     * If the file dialogue is closed without selecting a folder an empty 
     * string is returned.
     *
     * @param params Parameters: initialPath (in), chosenFolder (out).
     */
    public void chooseFolder(JSObject params) {
        String folder = AccessController.doPrivileged(new ChooseFolderAction((String)params.getMember("initialPath")));
        params.setMember("chosenFolder", folder);
    }

    /**
     * Download a file from a provided URL to a specified file on disk. A unique
     * id for referencing this download is returned.
     *
     * @param params Parameters: fileName (in), url (in), id (out).
     */
    public void newDownload(JSObject params) {
        String fileName = (String)params.getMember("fileName");
        String url = (String)params.getMember("url");
        int id = this.nextId();
        this.downloads.put(id, new DownloadStatus(fileName, url));
        params.setMember("id", id);
    }
    
    /**
     * Download a file from a provided URL to a specified file on disk. A unique
     * id for referencing this download is returned.
     *
     * @param params Parameters: id (in), started (out)
     */
    public void startDownload(JSObject params) {
        int id = (int)params.getMember("id");
        DownloadStatus download = this.downloads.get(id);
        if (download == null) {
            params.setMember("started", false);
        } else {
            AccessController.doPrivileged(new StartThread(new DownloadToFileAction(download)));
            params.setMember("started", true);
        }
    }
    
    /**
     * Cancel an active download by providing its id. If the id doesn't exist
     * no action is taken.
     *
     * @param params Parameters: id (in), 
     *      bps (out), progress (out), total (out), isDone (out), isCancelled (out), error (out)
     */
    public void getDownloadStatus(JSObject params) {
        int id = (int)params.getMember("id");
        
        DownloadStatus download = this.downloads.get(id);
        if (download == null) {
            download = new DownloadStatus("", "");
            download.error("Not found.");
        }
        
        if (download.isDone() || download.hasError() || download.isCancelled()) {
            this.downloads.remove(id);
        }
        
        params.setMember("isDone", download.isDone());
        params.setMember("isCancelled", download.isCancelled());
        params.setMember("error", download.getError());
        params.setMember("bps", download.getBps());
        params.setMember("progress", download.getProgress());
        params.setMember("total", download.getTotalSize());
    }
    
    /**
     * Cancel an active download by providing its id. If the id doesn't exist
     * no action is taken.
     *
     * @param params Parameters: id (in)
     */
    public void cancelDownload(JSObject params) {
        int id = (int)params.getMember("id");
        DownloadStatus download = this.downloads.get(id);
        if (download != null) {
            download.cancel();
            this.downloads.remove(id);
        }
    }
    
    /**
     * @return A unique id.
     */
    private int nextId() {
        return ++this.idCounter;
    }
}
