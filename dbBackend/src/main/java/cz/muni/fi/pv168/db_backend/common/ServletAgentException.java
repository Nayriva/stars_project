package cz.muni.fi.pv168.db_backend.common;

/**
 * Created by xbucik on 29.3.17.
 */
public class ServletAgentException extends Exception {
    public ServletAgentException() {
        super();
    }

    public ServletAgentException(String s) {
        super(s);
    }

    public ServletAgentException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ServletAgentException(Throwable throwable) {
        super(throwable);
    }

    protected ServletAgentException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
