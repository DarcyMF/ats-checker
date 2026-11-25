package io.github.darcymf.ats.pdf;

public class UnreadablePdfException extends RuntimeException {

    public UnreadablePdfException(String message) {
        super(message);
    }

    public UnreadablePdfException(String message, Throwable cause) {
        super(message, cause);
    }
}
