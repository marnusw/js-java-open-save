
package JsJavaOpenSave;

/**
 * 
 * 
 * @author marnusw
 */
public class DownloadStatus {
    
    private final String fileName;
    private final String url;
    
    private double bps = 0;
    private int prog = 0;
    private volatile int total = 0;

    private boolean done = false;
    private boolean cancelled = false;
    private String error = null;
    
    /**
     * 
     * @param fileName
     * @param url 
     */
    public DownloadStatus(String fileName, String url) {
        this.fileName = fileName;
        this.url = url;
    }
    /**
     * 
     * @param total 
     */
    public void setTotalSize(int total) {
        this.total = total;
    }
    
    /**
     * 
     */
    public synchronized void completed() {
        this.prog = this.total;
        this.bps = 0;
        this.done = true;
    }
    /**
     * Periodic call 
     *
     * @param bps The download speed in bytes per second.
     * @param prog The amount of work that has been done.
     */
    public synchronized void progress(double bps, int prog) {
        this.prog = prog;
        this.bps = bps;
    }
    /**
     * 
     *
     * @param msg An error message.
     */
    public synchronized void error(String msg) {
        this.error = msg;
    }
    /**
     * 
     */
    public synchronized void cancel() {
        this.prog = 0;
        this.bps = 0;
        this.cancelled = true;
    }
    
    
    /**
     * 
     * @return 
     */
    public boolean isDone() {
        return done;
    }
    /**
     * 
     * @return 
     */
    public boolean isCancelled() {
        return this.cancelled;
    }
    /**
     * 
     * @return 
     */
    public boolean notCancelled() {
        return !this.cancelled;
    }
    /**
     * 
     * @return 
     */
    public boolean hasError() {
        return error != null;
    }
    /**
     * 
     * @return 
     */
    public String getError() {
        return error;
    }
    
    /**
     * 
     * @return 
     */
    public double getBps() {
        return bps;
    }
    /**
     * 
     * @return 
     */
    public int getProgress() {
        return prog;
    }
    /**
     * 
     * @return 
     */
    public int getTotalSize() {
        return total;
    }
    
    /**
     * 
     * @return 
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * 
     * @return 
     */
    public String getUrl() {
        return url;
    }
}
