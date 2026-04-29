package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentStatusNotWaitingException extends ApplicationException {

    public EnrollmentStatusNotWaitingException() {
        super(
            EnrollmentError.ENROLLMENT_STATUS_NOT_WAITING.name(),
            EnrollmentError.ENROLLMENT_STATUS_NOT_WAITING.getStatus(),
            EnrollmentError.ENROLLMENT_STATUS_NOT_WAITING.getMessage()
        );
    }
}
