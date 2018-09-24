package at.ac.ait.sac;

/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by EdeggerK on 07.02.2018.   ¯\_(ツ)_/¯
 */

class SymbIoTeException extends Exception {

    public SymbIoTeException() {
    }

    public SymbIoTeException(String message) {
        super(message);
    }

    public SymbIoTeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SymbIoTeException(Throwable cause) {
        super(cause);
    }

    public SymbIoTeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
