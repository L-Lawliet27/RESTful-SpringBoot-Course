package com.appsdeveloperblog.app.ws.exceptions;

public class UserServiceException extends RuntimeException{

    private final static long serialVersionUID = 4L;

    public UserServiceException(String message) {
        super(message);
    }
}
