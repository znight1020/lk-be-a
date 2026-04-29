package leehs.course.core.course.application.result;

import java.time.LocalDateTime;

import leehs.course.core.course.domain.repository.projection.CourseEnrollmentSummaryProjection;

public record CourseEnrollmentSummaryResult(
    Long enrollmentId,
    Long studentId,
    String studentName,
    String studentEmail,
    String status,
    LocalDateTime createdAt,
    LocalDateTime confirmedAt,
    LocalDateTime cancelledAt
) {

    public static CourseEnrollmentSummaryResult of(CourseEnrollmentSummaryProjection projection) {
        return new CourseEnrollmentSummaryResult(
            projection.getEnrollmentId(),
            projection.getStudentId(),
            projection.getStudentName(),
            projection.getStudentEmail(),
            projection.getStatus().name(),
            projection.getCreatedAt(),
            projection.getConfirmedAt(),
            projection.getCancelledAt()
        );
    }
}
