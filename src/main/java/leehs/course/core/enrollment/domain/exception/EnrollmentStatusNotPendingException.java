package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentStatusNotPendingException extends ApplicationException {

    public EnrollmentStatusNotPendingException() {
        super(
            EnrollmentError.ENROLLMENT_STATUS_NOT_PENDING.name(),
            EnrollmentError.ENROLLMENT_STATUS_NOT_PENDING.getStatus(),
            EnrollmentError.ENROLLMENT_STATUS_NOT_PENDING.getMessage()
        );
    }
}
