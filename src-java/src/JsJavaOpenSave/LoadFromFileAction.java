
package JsJavaOpenSave;

import java.security.PrivilegedAction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 *
 * @author marnusw
 */
public class LoadFromFileAction implements PrivilegedAction {

    private final String returnParams[];
    private final String fileName;

    public LoadFromFileAction(String fileName, String returnParams[]) {
        this.returnParams = returnParams;
        this.fileName = fileName;
    }

    @Override
    public Object run() {
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
            returnParams[0] = sb.toString();
        } catch (IOException ioe) {
            returnParams[1] = ioe.getMessage();
        }
        return null;
    }
}
