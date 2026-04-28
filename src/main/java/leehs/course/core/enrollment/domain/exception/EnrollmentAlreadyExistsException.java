package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentAlreadyExistsException extends ApplicationException {

    public EnrollmentAlreadyExistsException() {
        super(
            EnrollmentError.ENROLLMENT_ALREADY_EXISTS.name(),
            EnrollmentError.ENROLLMENT_ALREADY_EXISTS.getStatus(),
            EnrollmentError.ENROLLMENT_ALREADY_EXISTS.getMessage()
        );
    }
}
