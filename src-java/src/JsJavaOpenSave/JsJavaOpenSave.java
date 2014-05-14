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

import java.security.AccessController;

import java.applet.Applet;
import java.io.*;

import java.net.URL;
import java.net.URLConnection;

// For this import to resolve include jre\lib\plugin.jar as a compile-time library of the project.
import netscape.javascript.JSObject;
import netscape.javascript.JSException;

public class JsJavaOpenSave extends Applet {

    @Override
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            {"id", "string", "The id of the applet element in the DOM."},
            {"fileName", "path", "The full path to open or save content or the downloaded file to."},
            {"data", "string", "The data to write to the specified file name, indicates a write if present."},
            {"url", "url", "The URL to download to the specified file name, indicates download if present."},
            {"bufferSize", "int", "The optional size of the buffer used when reading from the TCP socket."}
        };
        return pinfo;
    }

    /**
     * The browser window object used for calling JavaScript methods.
     */
    private JSObject window;

    private String id;
    private String fileName;
    private String data;
    private String url;
    /**
     * By default a download is started with a 2MB bytes buffer size. The largest amount of data typically read is just
     * over 1MB, so this provides ample space.
     */
    private int bufSize = 2 * 1024 * 1024;

    /**
     * The applet is initialised by retrieving a JSObject instance of the window for executing JavaScript callbacks.
     * Furthermore the HTML parameters passed into the applet are transferred to member variables.
     *
     * @throws JSException If a JSObeject for the window can't be retrieved.
     */
    @Override
    public void init() throws JSException {
        // In Java 7 IPv6 traffic is preferred which causes a download over IPv4 to hang on some operating systems.
        System.setProperty("java.net.preferIPv4Stack", "true");

        this.window = JSObject.getWindow(this);

        this.id = this.getParameter("id");
        this.fileName = this.getParameter("fileName");
        this.data = this.getParameter("data");
        this.url = this.getParameter("url");

        String bufSizeStr = this.getParameter("bufferSize");
        if (bufSizeStr != null) {
            this.bufSize = Integer.parseInt(bufSizeStr);
        }
    }

    /**
     * Execution is started checking for a data parameter which triggers an immediate save operation. If no data, but a
     * url parameter is present a download is triggered to the file name. Finally, if neither data nor a url is provided
     * the file is read and its contents returned to the callback.
     */
    @Override
    public void start() {
        if (this.data != null) {
            this.write(this.fileName, this.data);
        } else if (this.url != null) {
            this.download(this.fileName, this.url);
        } else if (this.fileName != null) {
            this.read(this.fileName);
        }
    }

    /**
     * Open a file dialogue to select a folder from the local file system. If the file dialogue is closed without
     * selecting a folder an empty string is returned.
     *
     * @param params Parameters: initialPath (in), chosenFolder (out).
     */
    public void chooseFolder(JSObject params) {
        Object initialPath = params.getMember("initialPath");
        String folder = (String)AccessController.doPrivileged(new ChooseFolderAction((String)initialPath));
        params.setMember("chosenFolder", folder);
    }

    /**
     * Download a file from a provided URL to a specified file on disk. The buffer size used when reading from the TCP
     * socket can also specified.
     *
     * @param fileName The full path to save the downloaded file to.
     * @param url The URL to download to the specified file name.
     */
    public void download(String fileName, String url) {
        URLConnection resource;
        try {
            resource = new URL(url).openConnection();
        } catch (IOException ioe) {
            this.error(ioe.getMessage());
            return;
        }
        try (
            BufferedInputStream in = new BufferedInputStream(resource.getInputStream());
            OutputStream out = new FileOutputStream(new File(fileName));
        ) {
            byte buffer[] = new byte[this.bufSize];
            int contentLength = resource.getContentLength(),
                    prevTotal = 0,
                    total = 0,
                    count;

            this.progress(0, 0, contentLength);
            int shortInterval = 500; // ms
            int longInterval = 1000; // ms
            double rate = 0.0;

            long endTime, longStartTime, shortStartTime = System.nanoTime() / 1000000;
            longStartTime = shortStartTime;

            while ((count = in.read(buffer)) != -1) {
                endTime = System.nanoTime() / 1000000;
                total += count;
                if (endTime - shortStartTime > shortInterval) {
                    if (endTime - longStartTime > longInterval) {
                        rate = (total - prevTotal) / (endTime - shortStartTime) * 1000;
                        longStartTime = endTime;
                    }
                    this.progress(rate, total, contentLength);
                    shortStartTime = endTime;
                    prevTotal = total;
                }
                out.write(buffer, 0, count);
            }

            out.close();
            in.close();
            this.complete();
        } catch (IOException ioe) {
            this.error(ioe.getMessage());
        }
    }

    /**
     * Write the data to the file at the provided file name.
     *
     * @param fileName The full path and name of the file to write to.
     * @param data The data that will be the new contents of the file.
     */
    public void write(String fileName, String data) {
        File f = new File(fileName);
        try (
                BufferedWriter out = new BufferedWriter(new FileWriter(f, false));) {
            out.write(data);
            out.close();
            this.complete();
        } catch (IOException ioe) {
            this.error(ioe.getMessage());
        }
    }

    /**
     * Read and return the contents of the file at the provided file name.
     *
     * @param fileName The full path and name of the file to read.
     */
    public void read(String fileName) {
        try (
                BufferedReader br = new BufferedReader(new FileReader(fileName));) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            br.close();
            this.complete(sb.toString());
        } catch (IOException ioe) {
            this.error(ioe.getMessage());
        }
    }

    /**
     * Call back to the JavaScript code when an operation is completed.
     */
    private void complete() {
        this.callJS("onComplete", quote(this.id));
    }

    /**
     * Call back to the JavaScript code when an operation is completed with data.
     *
     * @param data Data to pass back to the
     */
    private void complete(String data) {
        this.callJS("onComplete", quote(this.id) + "," + quote(data));
    }

    /**
     * Periodic call back to the JavaScript code to report progress on a long-running task.
     *
     * @param bps The download speed in bytes per second.
     * @param done The amount of work that has been done.
     * @param total The total amount of work to be done.
     */
    private void progress(double bps, int done, int total) {
        this.callJS("onProgress", quote(this.id) + "," + bps + "," + done + "," + total);
    }

    /**
     * Call back to the JavaScript code when an operation is completed.
     *
     * @param data Data to pass back to the
     */
    private void error(String msg) {
        this.callJS("onError", quote(this.id) + "," + quote("Java Error: " + msg));
    }

    /**
     * Add single quotes around a string so it can be used in an eval() statement as a JavaScript string.
     *
     * @param str
     * @return
     */
    private String quote(String str) {
        return "'" + str + "'";
    }

    private void log(String str) {
        this.window.eval("console.log(" + quote(str) + ")");
    }

    /**
     * Run a JavaScript method on the JsJavaOpenSave JS object without parameters.
     *
     * @param method The method name.
     * @param params A string of parameters which can be eval'ed within the brackets of a method call.
     * @return The return value of the JS method as a string.
     */
    private String callJS(String method, String params) {
        return (String) this.window.eval("JsJavaOpenSave." + method + "(" + params + ")");
    }
}
