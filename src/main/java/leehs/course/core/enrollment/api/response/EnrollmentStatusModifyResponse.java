package leehs.course.core.enrollment.api.response;

import leehs.course.core.enrollment.domain.model.Enrollment;

public record EnrollmentStatusModifyResponse(Long enrollmentId, Long courseId, String status) {

    public static EnrollmentStatusModifyResponse of(Enrollment enrollment) {
        return new EnrollmentStatusModifyResponse(
            enrollment.getId(),
            enrollment.getCourse().getId(),
            enrollment.getStatus().name()
        );
    }
}
