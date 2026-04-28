package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentStatusAlreadyCancelledException extends ApplicationException {

    public EnrollmentStatusAlreadyCancelledException() {
        super(
            EnrollmentError.ENROLLMENT_STATUS_ALREADY_CANCELLED.name(),
            EnrollmentError.ENROLLMENT_STATUS_ALREADY_CANCELLED.getStatus(),
            EnrollmentError.ENROLLMENT_STATUS_ALREADY_CANCELLED.getMessage()
        );
    }
}
