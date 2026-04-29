package leehs.course.core.enrollment.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import leehs.course.core.enrollment.api.request.EnrollmentApplyRequest;
import leehs.course.core.enrollment.api.response.EnrollmentApplyResponse;
import leehs.course.core.enrollment.api.response.EnrollmentPageResponse;
import leehs.course.core.enrollment.api.response.EnrollmentStatusModifyResponse;
import leehs.course.core.enrollment.application.EnrollmentApplier;
import leehs.course.core.enrollment.application.EnrollmentFinder;
import leehs.course.core.enrollment.application.EnrollmentModifier;
import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.application.command.EnrollmentStatusModifyCommand;
import leehs.course.core.enrollment.application.query.EnrollmentFindQuery;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.global.web.RequestUserId;

@Validated
@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentApi {

    private final EnrollmentApplier enrollmentApplier;
    private final EnrollmentFinder enrollmentFinder;
    private final EnrollmentModifier enrollmentModifier;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentApplyResponse applyEnrollment(
        @RequestUserId Long userId,
        @Valid @RequestBody EnrollmentApplyRequest request
    ) {
        EnrollmentApplyCommand command = new EnrollmentApplyCommand(userId, request.courseId());

        Enrollment enrollment = enrollmentApplier.apply(command);

        return EnrollmentApplyResponse.of(enrollment);
    }

    @GetMapping("/me")
    public EnrollmentPageResponse getMyEnrollments(
        @RequestUserId Long userId,
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다") int page,
        @RequestParam(defaultValue = "4") @Min(value = 1, message = "size는 1 이상이어야 합니다") int size
    ) {
        EnrollmentFindQuery query = new EnrollmentFindQuery(userId, page, size);

        Page<Enrollment> enrollments = enrollmentFinder.findAll(query);

        return EnrollmentPageResponse.of(enrollments);
    }

    @PatchMapping("/{enrollmentId}/confirm")
    public EnrollmentStatusModifyResponse confirmEnrollment(
        @RequestUserId Long userId,
        @PathVariable Long enrollmentId
    ) {
        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(userId);

        Enrollment enrollment = enrollmentModifier.confirm(enrollmentId, command);

        return EnrollmentStatusModifyResponse.of(enrollment);
    }

    @PatchMapping("/{enrollmentId}/cancel")
    public EnrollmentStatusModifyResponse cancelEnrollment(
        @RequestUserId Long userId,
        @PathVariable Long enrollmentId
    ) {
        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(userId);

        Enrollment enrollment = enrollmentModifier.cancel(enrollmentId, command);

        return EnrollmentStatusModifyResponse.of(enrollment);
    }
}
