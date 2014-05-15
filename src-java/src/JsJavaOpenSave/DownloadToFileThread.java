
package JsJavaOpenSave;

import java.io.BufferedInputStream;
import java.io.OutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author marnusw
 */
public class DownloadToFileThread implements Runnable {

    private final int bufSize = 2*1024*1024;
    private final DownloadStatus status;

    public DownloadToFileThread(DownloadStatus status) {
        this.status = status;
    }

    @Override
    public void run() {
        URLConnection resource;
        try {
            resource = new URL(this.status.getUrl()).openConnection();
        } catch (IOException ioe) {
            this.status.error(ioe.getMessage());
            return;
        }
        try (
            BufferedInputStream in = new BufferedInputStream(resource.getInputStream());
            OutputStream out = new FileOutputStream(new File(this.status.getFileName()));
        ) {
            byte buffer[] = new byte[this.bufSize];
            int shortInterval = 500, // ms
                longInterval = 1000, // ms
                prevTotal = 0,
                total = 0,
                count;

            this.status.setTotalSize(resource.getContentLength());
            double rate = 0.0;

            long endTime, longStartTime, shortStartTime = System.nanoTime() / 1000000;
            longStartTime = shortStartTime;

            while ((count = in.read(buffer)) != -1 && this.status.notCancelled()) {
                endTime = System.nanoTime() / 1000000;
                total += count;
                if (endTime - shortStartTime > shortInterval) {
                    if (endTime - longStartTime > longInterval) {
                        rate = (total - prevTotal) / (endTime - shortStartTime) * 1000;
                        longStartTime = endTime;
                    }
                    this.status.progress(rate, total);
                    shortStartTime = endTime;
                    prevTotal = total;
                }
                out.write(buffer, 0, count);
            }

            out.close();
            in.close();
            
            if (this.status.notCancelled()) {
                this.status.completed();
            }
        } catch (IOException ioe) {
            this.status.error(ioe.getMessage());
        }
    }
}
