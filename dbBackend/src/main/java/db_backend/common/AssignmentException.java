package db_backend.common;

/**
 * Thrown when assignment for agent whose rank is less than mission's minAgentRank is tried to be created.
 * Thrown when assignment for dead agent is tried to be created.
 * Thrown when assignment for agent who is already on mission is tried to be created.
 * Thrown when assignment for mission which is already marked ass successful or finished is tried to be created.
 *
 * Created by nayriva on 22.3.2017.
 */
public class AssignmentException extends RuntimeException {
    public AssignmentException() {
        super();
    }

    public AssignmentException(String message) {
        super(message);
    }

    public AssignmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssignmentException(Throwable cause) {
        super(cause);
    }

    protected AssignmentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
