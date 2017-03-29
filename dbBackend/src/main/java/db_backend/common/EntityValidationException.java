package db_backend.common;

/**
 * Exception represents failure in validating attributes of entity.
 * (One or more attributes of entity are null or invalid value)
 *
 * Created by nayriva on 9.3.2017.
 */
public class EntityValidationException extends RuntimeException {

    public EntityValidationException() {
        super();
    }

    public EntityValidationException(String message) {
        super(message);
    }

    public EntityValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityValidationException(Throwable cause) {
        super(cause);
    }

    protected EntityValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
