package leehs.course.global.web.exception.error;

import lombok.Getter;

@Getter
public enum RequestUserIdError {

    REQUEST_USER_ID_HEADER_MISSING(401, "요청 헤더 [%s]는 필수입니다"),
    REQUEST_USER_ID_HEADER_INVALID(400, "요청 헤더 [%s]의 값의 형식이 유효하지 않습니다");

    private final int status;
    private final String messageTemplate;

    RequestUserIdError(int status, String messageTemplate) {
        this.status = status;
        this.messageTemplate = messageTemplate;
    }

    public String message(Object... args) {
        return String.format(messageTemplate, args);
    }
}
