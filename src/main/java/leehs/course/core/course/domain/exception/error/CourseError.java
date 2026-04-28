package leehs.course.core.course.domain.exception.error;

import lombok.Getter;

@Getter
public enum CourseError {

    COURSE_STATUS_NOT_DRAFT(400, "초안 상태가 아닙니다"),
    COURSE_STATUS_NOT_OPEN(400, "모집 상태가 아닙니다"),

    COURSE_MANAGEMENT_FORBIDDEN(403, "강의를 등록, 관리할 권한이 없습니다"),
    COURSE_NOT_OWNER(403, "강의 소유자가 아닙니다"),

    COURSE_NOT_FOUND(404, "강의를 찾을 수 없습니다");

    private final int status;
    private final String message;

    CourseError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
