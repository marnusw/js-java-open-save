
package JsJavaOpenSave;

import java.security.PrivilegedAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Save the contents of _data_ to a specified file on disk. If an error
 * occurs the error message is returned.
 *
 * @author marnusw
 */
public class SaveToFileAction implements PrivilegedAction<String> {

    private final String fileName;
    private final String data;

    public SaveToFileAction(String fileName, String data) {
        this.fileName = fileName;
        this.data = data;
    }

    @Override
    public String run() {
        try (
            FileWriter out = new FileWriter(new File(fileName), false);
        ) {
            out.write(data);
            out.close();
        } catch (IOException ioe) {
            return ioe.getMessage();
        }
        return null;
    }
}
