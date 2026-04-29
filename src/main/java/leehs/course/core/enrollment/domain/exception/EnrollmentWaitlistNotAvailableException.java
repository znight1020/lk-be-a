package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentWaitlistNotAvailableException extends ApplicationException {

    public EnrollmentWaitlistNotAvailableException() {
        super(
            EnrollmentError.ENROLLMENT_WAITLIST_NOT_AVAILABLE.name(),
            EnrollmentError.ENROLLMENT_WAITLIST_NOT_AVAILABLE.getStatus(),
            EnrollmentError.ENROLLMENT_WAITLIST_NOT_AVAILABLE.getMessage()
        );
    }
}
