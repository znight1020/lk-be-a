package leehs.course.core.user.domain.exception.error;

import lombok.Getter;

@Getter
public enum UserError {

    // USER
    USER_NOT_FOUND(404, "회원을 찾을 수 없습니다"),
    USER_ROLE_INVALID(400, "역할은 CREATOR 또는 STUDENT 여야 합니다"),

    // EMAIL
    EMAIL_FORMAT_INVALID(400, "이메일 형식이 올바르지 않습니다"),
    EMAIL_DUPLICATE(409, "해당 이메일로 가입된 계정이 존재합니다");

    private final int status;
    private final String message;

    UserError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
