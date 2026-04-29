package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentCancellationPeriodExpiredException extends ApplicationException {

    public EnrollmentCancellationPeriodExpiredException() {
        super(
            EnrollmentError.ENROLLMENT_CANCELLATION_PERIOD_EXPIRED.name(),
            EnrollmentError.ENROLLMENT_CANCELLATION_PERIOD_EXPIRED.getStatus(),
            EnrollmentError.ENROLLMENT_CANCELLATION_PERIOD_EXPIRED.getMessage()
        );
    }
}
