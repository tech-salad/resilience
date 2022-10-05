package org.techsalad.feign.exception;

public class SaladException extends RuntimeException{
    public SaladException() {
    }

    public SaladException(String message) {
        super(message);
    }

    public SaladException(Throwable cause) {
        super(cause);
    }
}
