package cz.muni.fi.pv168.db_backend.common;

/**
 * Exception represents collision in ID attribute of entity.
 * (Entity with existing ID is tried to be created).
 *
 * Created by nayriva on 8.3.2017.
 */
public class IllegalEntityException extends RuntimeException {

    public IllegalEntityException() {
        super();
    }

    public IllegalEntityException(String message) {
        super(message);
    }

    public IllegalEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
