package cz.muni.fi.pv168.db_backend.common;

/**
 * Created by xbucik on 29.3.17.
 */
public class ServletMissionException extends Exception {
    public ServletMissionException() {
        super();
    }

    public ServletMissionException(String s) {
        super(s);
    }

    public ServletMissionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ServletMissionException(Throwable throwable) {
        super(throwable);
    }

    protected ServletMissionException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
