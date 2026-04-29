package leehs.course.core.course.domain.repository.projection;

import java.time.LocalDateTime;

import leehs.course.core.enrollment.domain.model.EnrollmentStatus;

public interface CourseEnrollmentSummaryProjection {

    Long getEnrollmentId();

    Long getStudentId();

    String getStudentName();

    String getStudentEmail();

    EnrollmentStatus getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getConfirmedAt();

    LocalDateTime getCancelledAt();
}
