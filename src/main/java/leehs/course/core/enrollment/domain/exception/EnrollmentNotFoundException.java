package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentNotFoundException extends ApplicationException {

    public EnrollmentNotFoundException() {
        super(
            EnrollmentError.ENROLLMENT_NOT_FOUND.name(),
            EnrollmentError.ENROLLMENT_NOT_FOUND.getStatus(),
            EnrollmentError.ENROLLMENT_NOT_FOUND.getMessage()
        );
    }
}
