package leehs.course.core.enrollment.api.response;

import leehs.course.core.enrollment.domain.model.Enrollment;

public record EnrollmentApplyResponse(Long enrollmentId, Long courseId, String status) {

    public static EnrollmentApplyResponse of(Enrollment enrollment) {
        return new EnrollmentApplyResponse(
            enrollment.getId(),
            enrollment.getCourse().getId(),
            enrollment.getStatus().name()
        );
    }
}
