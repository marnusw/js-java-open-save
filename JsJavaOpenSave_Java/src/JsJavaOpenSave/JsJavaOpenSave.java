package JsJavaOpenSave;

import java.applet.Applet;
import java.io.*;

public class JsJavaOpenSave extends Applet {

    @Override
    public void init() {
        this.download("C:/Users/marnusw/Downloads/google.html", "http://www.google.com");
        this.write("C:/Users/marnusw/Downloads/output.txt", "Output file contents");
    }
    
    public void download(String fileName, String url) {
        this.download(fileName, url, 1024);
    }
    
    public void download(String fileName, String url, int bufSize) {
        try (
            BufferedInputStream in = new BufferedInputStream(new java.net.URL(url).openStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), bufSize);
        ) {
            byte data[] = new byte[bufSize];
            while (in.read(data, 0, bufSize) >= 0) {
                out.write(data);
            }
            
            out.close();
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void write(String fileName, String data) {
        File f = new File(fileName);
        try (
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
        ) {
            out.write(data);
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public String read(String fileName) {
        File f = new File(fileName);
        try (
            FileInputStream in = new FileInputStream(f);
        ) {
            // Read from the file
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return "Return read data";
    }
}