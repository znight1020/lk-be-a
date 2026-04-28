package leehs.course.core.enrollment.domain.exception;

import leehs.course.core.enrollment.domain.exception.error.EnrollmentError;
import leehs.course.global.exception.ApplicationException;

public class EnrollmentCapacityExceededException extends ApplicationException {

    public EnrollmentCapacityExceededException() {
        super(
            EnrollmentError.ENROLLMENT_CAPACITY_EXCEEDED.name(),
            EnrollmentError.ENROLLMENT_CAPACITY_EXCEEDED.getStatus(),
            EnrollmentError.ENROLLMENT_CAPACITY_EXCEEDED.getMessage()
        );
    }
}
