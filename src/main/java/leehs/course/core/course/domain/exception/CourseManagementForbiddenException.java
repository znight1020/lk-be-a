package leehs.course.core.course.domain.exception;

import leehs.course.core.course.domain.exception.error.CourseError;
import leehs.course.global.exception.ApplicationException;

public class CourseManagementForbiddenException extends ApplicationException {

    public CourseManagementForbiddenException() {
        super(
            CourseError.COURSE_MANAGEMENT_FORBIDDEN.name(),
            CourseError.COURSE_MANAGEMENT_FORBIDDEN.getStatus(),
            CourseError.COURSE_MANAGEMENT_FORBIDDEN.getMessage()
        );
    }
}
