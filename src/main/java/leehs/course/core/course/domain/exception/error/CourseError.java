package leehs.course.core.course.domain.exception.error;

import lombok.Getter;

@Getter
public enum CourseError {

    COURSE_STATUS_NOT_DRAFT(400, "초안 상태가 아닙니다"),
    COURSE_STATUS_NOT_OPEN(400, "모집 상태가 아닙니다");

    private final int status;
    private final String message;

    CourseError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
