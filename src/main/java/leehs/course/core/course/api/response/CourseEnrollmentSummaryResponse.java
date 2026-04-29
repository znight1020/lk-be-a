package leehs.course.core.course.api.response;

import java.time.LocalDateTime;

import leehs.course.core.course.application.result.CourseEnrollmentSummaryResult;

public record CourseEnrollmentSummaryResponse(
    Long enrollmentId,
    Long studentId,
    String studentName,
    String studentEmail,
    String status,
    LocalDateTime createdAt,
    LocalDateTime confirmedAt,
    LocalDateTime cancelledAt
) {

    public static CourseEnrollmentSummaryResponse of(CourseEnrollmentSummaryResult enrollment) {
        return new CourseEnrollmentSummaryResponse(
            enrollment.enrollmentId(),
            enrollment.studentId(),
            enrollment.studentName(),
            enrollment.studentEmail(),
            enrollment.status(),
            enrollment.createdAt(),
            enrollment.confirmedAt(),
            enrollment.cancelledAt()
        );
    }
}
