package org.techsalad.feign.exception;

public class DressingException extends RuntimeException{
    public DressingException() {
    }

    public DressingException(String message) {
        super(message);
    }

    public DressingException(Throwable cause) {
        super(cause);
    }
}
