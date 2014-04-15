/*
 JsJavaOpenSave: Client Side File Management
 JavaScript + Java Library
 
 Version: 0.1
 
 Copyright (c) 2014 Marnus Weststrate (marnusw@mwconsult.co.za)
 
 */

package JsJavaOpenSave;

import java.applet.Applet;
import java.io.*;

// For this import to resolve include jre\lib\plugin.jar as a compile-time library of the project.
import netscape.javascript.JSObject;
import netscape.javascript.JSException;

public class JsJavaOpenSave extends Applet {

    @Override
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            {"id",         "string", "The id of the applet element in the DOM."},
            {"fileName",   "path",   "The full path to save the downloaded file to."},
            {"data",       "string", "The data to write to the specified file name."},
            {"url",        "url",    "The URL to download to the specified file name."},
            {"bufferSize", "int",    "The size of the buffer used when reading from the TCP socket."}
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
     * By default a download is started with a 1500 bytes buffer size, the default Ethernet MTU.
     */
    private int bufSize = 1500;

    @Override
    public void init() throws JSException {
        this.window = JSObject.getWindow(this);
        
        this.id = this.getParameter("id");
        this.fileName = this.getParameter("fileName");
        if (this.id == null || this.fileName == null) {
            this.error("ERROR: The id and fileName parameters are required by the JsJavaOpenSave applet.");
        }
        
        this.data = this.getParameter("data");
        this.url = this.getParameter("url");
        
        String bufSizeStr = this.getParameter("bufferSize");
        if (bufSizeStr != null) {
            this.bufSize = Integer.parseInt(bufSizeStr);
        }
    }

    @Override
    public void start() {
        if (this.url != null) {
            this.download(this.fileName, this.url);
        } else if (this.data != null) {
            this.write(this.fileName, this.data);
        } else {
            this.read(this.fileName);
        }
    }

    /**
     * Download a file from a provided URL to a specified file on disk. The buffer size
     * used when reading from the TCP socket can also specified.
     *
     * @param fileName The full path to save the downloaded file to.
     * @param url The URL to download to the specified file name.
     */
    public void download(String fileName, String url) {
        java.net.URLConnection resource;
        try {
            resource = new java.net.URL(url).openConnection();
        } catch (IOException ioe) {
            this.error(ioe.getMessage());
            return;
        }
        try (
            BufferedInputStream in = new BufferedInputStream(resource.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), this.bufSize);
        ) {
            byte buffer[] = new byte[this.bufSize];
            int total = resource.getContentLength(),
                done = 0,
                read;
            this.progress(done, total);
            while ((read = in.read(buffer, 0, this.bufSize)) >= 0) {
                out.write(buffer);
                done += read;
                this.progress(done, total);
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
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
        ) {
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
            BufferedReader br = new BufferedReader(new FileReader(fileName));
        ) {
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
     * @param done The amount of work that has been done.
     * @param total The total amount of work to be done.
     */
    private void progress(int done, int total) {
        this.callJS("onProgress", quote(this.id) + "," + done + "," + total);
    }
    /**
     * Call back to the JavaScript code when an operation is completed.
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
    /**
     * Run a JavaScript method on the JsJavaOpenSave JS object without parameters.
     * @param method The method name.
     * @param params A string of parameters which can be eval'ed within the brackets of a method call.
     * @return The return value of the JS method as a string.
     */
    private String callJS(String method, String params) {
        return (String) this.window.eval("JsJavaOpenSave." + method + "(" + params + ")");
    }
}