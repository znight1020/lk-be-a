package leehs.course.core.enrollment.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.api.request.EnrollmentApplyRequest;
import leehs.course.core.enrollment.api.response.EnrollmentApplyResponse;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.EnrollmentFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class EnrollmentApiTest {

    final MockMvcTester mvcTester;

    final ObjectMapper objectMapper;

    final EnrollmentRepository enrollmentRepository;

    final CourseRepository courseRepository;

    final UserRepository userRepository;

    @Test
    void whenApplyEnrollmentRequestIsValid_expectCreatedEnrollmentResponse() throws JsonProcessingException, UnsupportedEncodingException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        EnrollmentApplyRequest request = EnrollmentFixture.createEnrollmentApplyRequest(course.getId());
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments")
            .header("X-User-Id", student.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.enrollmentId", value -> assertThat(value).asNumber().isNotNull())
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isEqualTo(course.getId().intValue()))
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo("PENDING"));

        EnrollmentApplyResponse response =
            objectMapper.readValue(result.getResponse().getContentAsString(), EnrollmentApplyResponse.class);

        Enrollment enrollment = enrollmentRepository.findById(response.enrollmentId()).orElseThrow();
        assertThat(enrollment.getStudent().getId()).isEqualTo(student.getId());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());
        assertThat(enrollment.isPending()).isTrue();
    }

    @Test
    void whenApplyEnrollmentWithCreator_expectForbiddenResponse() throws JsonProcessingException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        EnrollmentApplyRequest request = EnrollmentFixture.createEnrollmentApplyRequest(course.getId());
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments")
            .header("X-User-Id", creator.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_FORBIDDEN"));
    }

    @Test
    void whenApplyEnrollmentWithoutUserIdHeader_expectUnauthorizedResponse() throws JsonProcessingException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        EnrollmentApplyRequest request = EnrollmentFixture.createEnrollmentApplyRequest(course.getId());
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("REQUEST_USER_ID_HEADER_MISSING"))
            .hasPathSatisfying("$.detail", value -> assertThat(value).asString().contains("X-User-Id"));
    }

    @Test
    void whenApplyEnrollmentWithActiveEnrollment_expectConflictResponse() throws JsonProcessingException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        EnrollmentApplyRequest request = EnrollmentFixture.createEnrollmentApplyRequest(course.getId());
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments")
            .header("X-User-Id", student.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_ALREADY_EXISTS"));
    }

    @Test
    void whenApplyEnrollmentWithFullCapacity_expectConflictResponse() throws JsonProcessingException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User existingStudent = userRepository.save(UserFixture.createStudent("existing@test.com"));
        User applicant = userRepository.save(UserFixture.createStudent("applicant@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 1));

        enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, existingStudent));

        EnrollmentApplyRequest request = EnrollmentFixture.createEnrollmentApplyRequest(course.getId());
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments")
            .header("X-User-Id", applicant.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_CAPACITY_EXCEEDED"));
    }

    @Test
    void whenApplyEnrollmentWithInvalidRequest_expectBadRequestResponse() throws JsonProcessingException {
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        EnrollmentApplyRequest request = EnrollmentFixture.createInvalidEnrollmentApplyRequest();
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments")
            .header("X-User-Id", student.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("VALIDATION_ERROR"));
    }

    @Test
    void whenGetMyEnrollmentsWithStudent_expectEnrollmentListResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course firstCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));
        Course secondCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment firstEnrollment = enrollmentRepository.save(
            EnrollmentFixture.createEnrollment(firstCourse, student));
        Enrollment secondEnrollment = enrollmentRepository.save(
            EnrollmentFixture.createEnrollment(secondCourse, student));
        secondEnrollment.confirm();

        Enrollment anotherEnrollment = enrollmentRepository.save(
            EnrollmentFixture.createEnrollment(secondCourse, anotherStudent));
        anotherEnrollment.confirm();
        anotherEnrollment.cancel();

        MvcTestResult result = mvcTester.get().uri("/enrollments/me")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.length()", value -> assertThat(value).asNumber().isEqualTo(2))
            .hasPathSatisfying("$[0].enrollmentId",
                value -> assertThat(value).asNumber().isEqualTo(secondEnrollment.getId().intValue()))
            .hasPathSatisfying("$[0].courseId",
                value -> assertThat(value).asNumber().isEqualTo(secondCourse.getId().intValue()))
            .hasPathSatisfying("$[0].courseTitle", value -> assertThat(value).isEqualTo(secondCourse.getTitle()))
            .hasPathSatisfying("$[0].status", value -> assertThat(value).isEqualTo("CONFIRMED"))
            .hasPathSatisfying("$[1].enrollmentId",
                value -> assertThat(value).asNumber().isEqualTo(firstEnrollment.getId().intValue()))
            .hasPathSatisfying("$[1].courseId",
                value -> assertThat(value).asNumber().isEqualTo(firstCourse.getId().intValue()))
            .hasPathSatisfying("$[1].status", value -> assertThat(value).isEqualTo("PENDING"));
    }

    @Test
    void whenGetMyEnrollmentsWithCreator_expectForbiddenResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        MvcTestResult result = mvcTester.get().uri("/enrollments/me")
            .header("X-User-Id", creator.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_FORBIDDEN"));
    }

    @Test
    void whenConfirmEnrollmentWithOwnerStudent_expectConfirmedEnrollmentResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/confirm")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.enrollmentId", value -> assertThat(value).asNumber().isEqualTo(enrollment.getId().intValue()))
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isEqualTo(course.getId().intValue()))
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo("CONFIRMED"));

        Enrollment confirmed = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertThat(confirmed.isConfirmed()).isTrue();
        assertThat(confirmed.getConfirmedAt()).isNotNull();
    }

    @Test
    void whenConfirmEnrollmentWithCreator_expectForbiddenResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/confirm")
            .header("X-User-Id", creator.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_FORBIDDEN"));
    }

    @Test
    void whenConfirmEnrollmentWithNonOwnerStudent_expectForbiddenResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User ownerStudent = userRepository.save(UserFixture.createStudent("owner@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, ownerStudent));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/confirm")
            .header("X-User-Id", anotherStudent.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_NOT_OWNER"));
    }

    @Test
    void whenConfirmEnrollmentThatDoesNotExist_expectNotFoundResponse() {
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/999/confirm")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_NOT_FOUND"));
    }

    @Test
    void whenConfirmAlreadyConfirmedEnrollment_expectBadRequestResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/confirm")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_STATUS_NOT_PENDING"));
    }

    @Test
    void whenCancelPendingEnrollmentWithOwnerStudent_expectCancelledEnrollmentResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/cancel")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.enrollmentId", value -> assertThat(value).asNumber().isEqualTo(enrollment.getId().intValue()))
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isEqualTo(course.getId().intValue()))
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo("CANCELLED"));

        Enrollment cancelled = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertThat(cancelled.isCancelled()).isTrue();
        assertThat(cancelled.getCancelledAt()).isNotNull();
    }

    @Test
    void whenCancelConfirmedEnrollmentWithOwnerStudent_expectCancelledEnrollmentResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        ReflectionTestUtils.setField(course, "startDate", LocalDate.now().plusDays(1));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/cancel")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo("CANCELLED"));

        Enrollment cancelled = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertThat(cancelled.isCancelled()).isTrue();
        assertThat(cancelled.getCancelledAt()).isNotNull();
    }

    @Test
    void whenCancelConfirmedEnrollmentAfterConfirmedWindow_expectBadRequestResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        ReflectionTestUtils.setField(course, "startDate", LocalDate.now().plusDays(1));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();
        ReflectionTestUtils.setField(enrollment, "confirmedAt", LocalDateTime.now().minusDays(8));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/cancel")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.title",
                value -> assertThat(value).isEqualTo("ENROLLMENT_CANCELLATION_PERIOD_EXPIRED"));
    }

    @Test
    void whenCancelConfirmedEnrollmentOnCourseStartDate_expectBadRequestResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        ReflectionTestUtils.setField(course, "startDate", LocalDate.now());

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/cancel")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.title",
                value -> assertThat(value).isEqualTo("ENROLLMENT_CANCELLATION_PERIOD_EXPIRED"));
    }

    @Test
    void whenCancelEnrollmentWithCreator_expectForbiddenResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/cancel")
            .header("X-User-Id", creator.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_FORBIDDEN"));
    }

    @Test
    void whenCancelEnrollmentWithNonOwnerStudent_expectForbiddenResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User ownerStudent = userRepository.save(UserFixture.createStudent("owner@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, ownerStudent));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/cancel")
            .header("X-User-Id", anotherStudent.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_NOT_OWNER"));
    }

    @Test
    void whenCancelEnrollmentThatDoesNotExist_expectNotFoundResponse() {
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        MvcTestResult result = mvcTester.patch().uri("/enrollments/999/cancel")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_NOT_FOUND"));
    }

    @Test
    void whenCancelAlreadyCancelledEnrollment_expectBadRequestResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();
        enrollment.cancel();

        MvcTestResult result = mvcTester.patch().uri("/enrollments/" + enrollment.getId() + "/cancel")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_STATUS_ALREADY_CANCELLED"));
    }
}
