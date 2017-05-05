package guiApp;

/**
 * Exception thrown when error while getting data for assignment dialogs tables occurred.
 *
 * @author Dominik Frantisek Bucik
 */
public class TableDataException extends RuntimeException {
    public TableDataException() {
        super();
    }

    public TableDataException(String message) {
        super(message);
    }

    public TableDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableDataException(Throwable cause) {
        super(cause);
    }

    protected TableDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
