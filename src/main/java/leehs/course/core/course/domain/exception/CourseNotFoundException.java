package leehs.course.core.course.domain.exception;

import leehs.course.core.course.domain.exception.error.CourseError;
import leehs.course.global.exception.ApplicationException;

public class CourseNotFoundException extends ApplicationException {

    public CourseNotFoundException() {
        super(
            CourseError.COURSE_NOT_FOUND.name(),
            CourseError.COURSE_NOT_FOUND.getStatus(),
            CourseError.COURSE_NOT_FOUND.getMessage()
        );
    }
}
