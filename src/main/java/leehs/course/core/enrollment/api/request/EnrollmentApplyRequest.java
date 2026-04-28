package leehs.course.core.enrollment.api.request;

import jakarta.validation.constraints.NotNull;

public record EnrollmentApplyRequest(
    @NotNull(message = "강의 ID는 필수입니다.")
    Long courseId
) {

}
