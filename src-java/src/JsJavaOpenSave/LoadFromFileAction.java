
package JsJavaOpenSave;

import java.security.PrivilegedAction;

import java.io.File;

/**
 * 
 *
 * @author marnusw
 */
public class LoadFromFileAction implements PrivilegedAction<String> {

    private final String returnParams[];
    private final String fileName;

    public LoadFromFileAction(String fileName, String returnParams[]) {
        this.returnParams = returnParams;
        this.fileName = fileName;
    }

    @Override
    public String run() {
        return null;
    }
}
