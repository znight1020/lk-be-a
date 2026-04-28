package leehs.course.core.course.domain.exception;

import leehs.course.core.course.domain.exception.error.CourseError;
import leehs.course.global.exception.ApplicationException;

public class CourseStatusNotOpenException extends ApplicationException {

    public CourseStatusNotOpenException() {
        super(
            CourseError.COURSE_STATUS_NOT_OPEN.name(),
            CourseError.COURSE_STATUS_NOT_OPEN.getStatus(),
            CourseError.COURSE_STATUS_NOT_OPEN.getMessage()
        );
    }
}
