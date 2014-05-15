
package JsJavaOpenSave;

import java.security.PrivilegedAction;

import java.io.File;

/**
 * 
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
        return null;
    }
}
