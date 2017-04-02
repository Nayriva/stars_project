package cz.muni.fi.pv168.db_backend.common;

/**
 * Created by xbucik on 29.3.17.
 */
public class ServletAssignmentException extends Exception {
    public ServletAssignmentException() {
        super();
    }

    public ServletAssignmentException(String s) {
        super(s);
    }

    public ServletAssignmentException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ServletAssignmentException(Throwable throwable) {
        super(throwable);
    }

    protected ServletAssignmentException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
