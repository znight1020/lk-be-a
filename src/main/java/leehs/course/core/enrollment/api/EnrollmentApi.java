package leehs.course.core.enrollment.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import leehs.course.core.enrollment.api.request.EnrollmentApplyRequest;
import leehs.course.core.enrollment.api.response.EnrollmentApplyResponse;
import leehs.course.core.enrollment.application.EnrollmentApplier;
import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.global.web.RequestUserId;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentApi {

    private final EnrollmentApplier enrollmentApplier;

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
}
