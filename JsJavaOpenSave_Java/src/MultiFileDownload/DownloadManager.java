package MultiFileDownload;

import java.applet.Applet;
import java.io.*;

public class DownloadManager extends Applet {

    @Override
    public void init() {
        this.write("C:/Users/marnusw/Downloads/output.txt", "Output file contents");
    }
    
    public void write(String fileName, String data) {
        File f = new File(fileName);
        try (
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
        ) {
            out.write(data);
            out.close();
        } catch (IOException ioe) {
            // Handle the exception.
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
            // Handle the exception.
        }
        return "Return read data";
    }
}