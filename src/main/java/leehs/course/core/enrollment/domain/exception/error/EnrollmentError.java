package leehs.course.core.enrollment.domain.exception.error;

import lombok.Getter;

@Getter
public enum EnrollmentError {

    ENROLLMENT_STATUS_NOT_PENDING(400, "신청 대기 상태가 아닙니다"),
    ENROLLMENT_STATUS_ALREADY_CANCELLED(400, "이미 취소된 상태입니다"),

    ENROLLMENT_FORBIDDEN(403, "수강 신청 권한이 없습니다"),
    ENROLLMENT_NOT_OWNER(403, "수강 신청 소유자가 아닙니다"),

    ENROLLMENT_NOT_FOUND(404, "수강 신청을 찾을 수 없습니다"),

    ENROLLMENT_ALREADY_EXISTS(409, "이미 수강 신청한 강의입니다"),
    ENROLLMENT_CAPACITY_EXCEEDED(409, "정원이 가득 차 수강 신청할 수 없습니다");

    private final int status;
    private final String message;

    EnrollmentError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
