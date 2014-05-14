
package JsJavaOpenSave;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.PrivilegedAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author marnusw
 */
public class DownloadToFileAction implements PrivilegedAction {

    private final JsJavaOpenSave jjos;
    private final String url;
    private final String fileName;
    private final int bufSize;

    public DownloadToFileAction(JsJavaOpenSave jjos, String url, String fileName, int bufferSize) {
        this.jjos = jjos;
        this.url = url;
        this.fileName = fileName;
        this.bufSize = bufferSize;
    }

    @Override
    public Object run() {
        URLConnection resource;
        try {
            resource = new URL(url).openConnection();
        } catch (IOException ioe) {
            this.jjos.error(ioe.getMessage());
            return null;
        }
        try (
            BufferedInputStream in = new BufferedInputStream(resource.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(fileName)), this.bufSize);
        ) {
            byte buffer[] = new byte[this.bufSize];
            int contentLength = resource.getContentLength(),
                    prevTotal = 0,
                    total = 0,
                    count;

            this.jjos.progress(0, 0, contentLength);
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
                    this.jjos.progress(rate, total, contentLength);
                    shortStartTime = endTime;
                    prevTotal = total;
                }
                out.write(buffer, 0, count);
            }

            out.close();
            in.close();
            this.jjos.complete();
        } catch (IOException ioe) {
            this.jjos.error(ioe.getMessage());
        }
        return null;
    }
}
