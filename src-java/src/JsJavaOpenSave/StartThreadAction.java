
package JsJavaOpenSave;

import java.security.PrivilegedAction;

/**
 *
 * @author marnusw
 */
public class StartThreadAction implements PrivilegedAction {

    private final Runnable runnable;

    public StartThreadAction(Runnable runnable) {
        this.runnable = runnable;
    }
    
    @Override
    public Object run() {
        new Thread(this.runnable).start();
        return null;
    }
}
