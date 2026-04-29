package leehs.course.core.course.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

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

import leehs.course.core.course.api.request.CourseCreateRequest;
import leehs.course.core.course.api.response.CourseCreateResponse;
import leehs.course.core.course.api.response.CourseDetailResponse;
import leehs.course.core.course.api.response.CourseEnrollmentSummaryResponse;
import leehs.course.core.course.api.response.CourseStatusModifyResponse;
import leehs.course.core.course.api.response.CourseSummaryResponse;
import leehs.course.core.course.application.CourseCreator;
import leehs.course.core.course.application.CourseEnrollmentFinder;
import leehs.course.core.course.application.CourseFinder;
import leehs.course.core.course.application.CourseModifier;
import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.application.command.CourseStatusModifyCommand;
import leehs.course.core.course.application.query.CourseEnrollmentFindQuery;
import leehs.course.core.course.application.query.CourseFindQuery;
import leehs.course.core.course.application.result.CourseDetailResult;
import leehs.course.core.course.application.result.CourseEnrollmentSummaryResult;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.global.web.RequestUserId;

@Tag(name = "Course", description = "강의 API")
@Validated
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseApi {

    private final CourseCreator courseCreator;
    private final CourseFinder courseFinder;
    private final CourseModifier courseModifier;
    private final CourseEnrollmentFinder courseEnrollmentFinder;

    @Operation(summary = "강의 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseCreateResponse createCourse(
        @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, required = true) @RequestUserId Long userId,
        @Valid @RequestBody CourseCreateRequest request
    ) {
        CourseCreateCommand command = new CourseCreateCommand(userId,
            request.title(), request.description(), request.price(), request.capacity(),
            request.startDate(), request.endDate()
        );

        Course course = courseCreator.create(command);

        return CourseCreateResponse.of(course);
    }

    @Operation(summary = "강의 목록 조회")
    @GetMapping
    public List<CourseSummaryResponse> getCourses(
        @Parameter(description = "강의 상태 필터")
        @RequestParam(required = false)
        @Pattern(regexp = "(?i)(|DRAFT|OPEN|CLOSED)", message = "status는 DRAFT, OPEN, CLOSED 중 하나여야 합니다")
        String status
    ) {
        CourseFindQuery query = new CourseFindQuery(CourseStatus.from(status));

        List<Course> courses = courseFinder.findAll(query);

        return courses.stream()
            .map(CourseSummaryResponse::of)
            .toList();
    }

    @Operation(summary = "강의 상세 조회")
    @GetMapping("/{courseId}")
    public CourseDetailResponse getCourse(@Parameter(description = "강의 ID") @PathVariable Long courseId) {
        CourseDetailResult result = courseFinder.findDetail(courseId);

        return CourseDetailResponse.of(result);
    }

    @Operation(summary = "강의별 수강생 목록 조회")
    @GetMapping("/{courseId}/enrollments")
    public List<CourseEnrollmentSummaryResponse> getCourseEnrollments(
        @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, required = true) @RequestUserId Long userId,
        @Parameter(description = "강의 ID") @PathVariable Long courseId
    ) {
        CourseEnrollmentFindQuery query = new CourseEnrollmentFindQuery(userId, courseId);

        List<CourseEnrollmentSummaryResult> enrollments = courseEnrollmentFinder.findAll(query);

        return enrollments.stream()
            .map(CourseEnrollmentSummaryResponse::of)
            .toList();
    }

    @Operation(summary = "강의 모집 시작")
    @PatchMapping("/{courseId}/open")
    public CourseStatusModifyResponse openCourse(
        @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, required = true) @RequestUserId Long userId,
        @Parameter(description = "강의 ID") @PathVariable Long courseId
    ) {
        CourseStatusModifyCommand command = new CourseStatusModifyCommand(userId);

        Course course = courseModifier.open(courseId, command);

        return CourseStatusModifyResponse.of(course);
    }

    @Operation(summary = "강의 모집 마감")
    @PatchMapping("/{courseId}/close")
    public CourseStatusModifyResponse closeCourse(
        @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, required = true) @RequestUserId Long userId,
        @Parameter(description = "강의 ID") @PathVariable Long courseId
    ) {
        CourseStatusModifyCommand command = new CourseStatusModifyCommand(userId);

        Course course = courseModifier.close(courseId, command);

        return CourseStatusModifyResponse.of(course);
    }
}
