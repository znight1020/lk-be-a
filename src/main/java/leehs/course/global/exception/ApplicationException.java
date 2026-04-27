package leehs.course.global.exception;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    private final String code;
    private final int status;

    public ApplicationException(String code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
