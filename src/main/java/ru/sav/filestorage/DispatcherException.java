package ru.sav.filestorage;

public class DispatcherException extends Exception {
    public DispatcherException(String message) {
        super(message);
    }

    public DispatcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
