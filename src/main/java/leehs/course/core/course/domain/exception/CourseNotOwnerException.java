package leehs.course.core.course.domain.exception;

import leehs.course.core.course.domain.exception.error.CourseError;
import leehs.course.global.exception.ApplicationException;

public class CourseNotOwnerException extends ApplicationException {

    public CourseNotOwnerException() {
        super(
            CourseError.COURSE_NOT_OWNER.name(),
            CourseError.COURSE_NOT_OWNER.getStatus(),
            CourseError.COURSE_NOT_OWNER.getMessage()
        );
    }
}
