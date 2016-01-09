package com.dsu.dev4fun.doublepotato.model.util;

public class ConnectionException extends Exception {

    public ConnectionException(String message, int code) {
        super("Connection Exception " + message + " code " + code);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
