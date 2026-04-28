package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentForbiddenException extends ApplicationException {

    public EnrollmentForbiddenException() {
        super(
            EnrollmentError.ENROLLMENT_FORBIDDEN.name(),
            EnrollmentError.ENROLLMENT_FORBIDDEN.getStatus(),
            EnrollmentError.ENROLLMENT_FORBIDDEN.getMessage()
        );
    }
}
