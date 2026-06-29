package com.example.gaokaoproject.exception;


import lombok.Getter;

public class ServiceException extends RuntimeException {
    @Getter
    private int code = 400;

    @Getter
    private String msg;

    public ServiceException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ServiceException(String msg) {
        this.msg = msg;
    }
}
