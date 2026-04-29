package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentNotOwnerException extends ApplicationException {

    public EnrollmentNotOwnerException() {
        super(
            EnrollmentError.ENROLLMENT_NOT_OWNER.name(),
            EnrollmentError.ENROLLMENT_NOT_OWNER.getStatus(),
            EnrollmentError.ENROLLMENT_NOT_OWNER.getMessage()
        );
    }
}
