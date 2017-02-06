package com.conference.exceptions;

public class InvalidTalkException extends Exception{
    @SuppressWarnings("compatibility:-140331834793898838")
    private static final long serialVersionUID = 1L;

    public InvalidTalkException(String msg) {
        super(msg);
    }

}