package leehs.course.core.enrollment.api.response;

import java.util.List;

import org.springframework.data.domain.Page;

import leehs.course.core.enrollment.domain.model.Enrollment;

public record EnrollmentPageResponse(
    List<EnrollmentSummaryResponse> content,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {

    public static EnrollmentPageResponse of(Page<Enrollment> enrollments) {
        return new EnrollmentPageResponse(
            enrollments.getContent().stream().map(EnrollmentSummaryResponse::of).toList(),
            enrollments.getTotalElements(),
            enrollments.getTotalPages(),
            enrollments.hasNext(),
            enrollments.hasPrevious()
        );
    }
}
