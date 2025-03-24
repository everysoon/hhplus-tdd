package io.hhplus.tdd.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final String code;

    public CustomException(String code, String message) {
        super(message);
        this.code = code;
    }

    public CustomException(String code) {
        super();
        this.code = code;
    }
}
