package leehs.course.core.enrollment.api.response;

import java.time.LocalDateTime;

import leehs.course.core.enrollment.domain.model.Enrollment;

public record EnrollmentSummaryResponse(
    Long enrollmentId,
    Long courseId,
    String courseTitle,
    Integer coursePrice,
    String status,
    LocalDateTime createdAt,
    LocalDateTime confirmedAt,
    LocalDateTime cancelledAt
) {

    public static EnrollmentSummaryResponse of(Enrollment enrollment) {
        return new EnrollmentSummaryResponse(
            enrollment.getId(),
            enrollment.getCourse().getId(),
            enrollment.getCourse().getTitle(),
            enrollment.getCourse().getPrice(),
            enrollment.getStatus().name(),
            enrollment.getCreatedAt(),
            enrollment.getConfirmedAt(),
            enrollment.getCancelledAt()
        );
    }
}
