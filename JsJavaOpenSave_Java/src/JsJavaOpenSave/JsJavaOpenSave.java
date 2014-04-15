package JsJavaOpenSave;

import java.lang.IllegalArgumentException;
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
            this.log("ERROR: The id and fileName parameters are required by the JsJavaOpenSave applet.");
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
        try (
            BufferedInputStream in = new BufferedInputStream(new java.net.URL(url).openStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), this.bufSize);
        ) {
            this.log("Starting the downloaded of " + url);
            byte data[] = new byte[this.bufSize];
            int read, count = 0;
            while ((read = in.read(data, 0, this.bufSize)) >= 0) {
                this.log(++count + ") Downloaded " + this.fileName + " " + read + " bytes");
                out.write(data);
            }
            out.close();
            in.close();
            this.runJS("removeApplet", quote(this.id));
        } catch (IOException ioe) {
            this.log("download() IO error: " + ioe.getMessage());
        }
    }

    /**
     * Write the data to the file at the provided file name.
     * 
     * @param fileName The full path and name of the file to write to.
     * @param data The data that will be the new contents of the file.
     */
    public void write(String fileName, String data) {
        this.log("Save to: " + fileName);
        this.log("Data: " + data);
        File f = new File(fileName);
        try (
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
        ) {
            out.write(data);
            out.close();
        } catch (IOException ioe) {
            this.log("write() IO error: " + ioe.getMessage());
        }
        this.log("Wrote " + fileName);
    }

    /**
     * Read and return the contents of the file at the provided file name.
     * 
     * @param fileName The full path and name of the file to read.
     * @return The contents read from the file.
     */
    public String read(String fileName) {
        File f = new File(fileName);
        try (
            FileInputStream in = new FileInputStream(f);
        ) {
            // Read from the file
            in.close();
        } catch (IOException ioe) {
            this.log("read() IO error: " + ioe.getMessage());
        }
        return "Return read data";
    }

    /**
     * Log text to the JavaScript console.
     * 
     * @param text 
     */
    private void log(String text) {
        this.runJS("log", this.quote(text));
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
     * @return The return value of the JS method as a string.
     */
    private String runJS(String method) {
        return this.runJS(method, "");
    }
    /**
     * Run a JavaScript method on the JsJavaOpenSave JS object without parameters.
     * @param method The method name.
     * @param params A string of parameters which can be eval'ed within the brackets of a method call.
     * @return The return value of the JS method as a string.
     */
    private String runJS(String method, String params) {
        return (String) this.window.eval("JsJavaOpenSave." + method + "(" + params + ")");
    }
}