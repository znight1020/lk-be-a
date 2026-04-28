package leehs.course.core.course.domain.exception;

import leehs.course.core.course.domain.exception.error.CourseError;
import leehs.course.global.exception.ApplicationException;

public class CourseStatusNotDraftException extends ApplicationException {

    public CourseStatusNotDraftException() {
        super(
            CourseError.COURSE_STATUS_NOT_DRAFT.name(),
            CourseError.COURSE_STATUS_NOT_DRAFT.getStatus(),
            CourseError.COURSE_STATUS_NOT_DRAFT.getMessage()
        );
    }
}
