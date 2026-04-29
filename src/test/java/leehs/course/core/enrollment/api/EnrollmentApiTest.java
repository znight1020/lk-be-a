package leehs.course.core.enrollment.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("수강 신청 API - 성공")
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
    @DisplayName("수강 신청 API - 실패, 강사 권한")
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
    @DisplayName("수강 신청 API - 실패, 사용자 헤더 누락")
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
    @DisplayName("수강 신청 API - 실패, 대기 혹은 확정 신청이 이미 있음")
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
    @DisplayName("수강 신청 API - 실패, 정원 초과")
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
    @DisplayName("대기열 등록 API - 성공, 정원 초과 강의")
    void whenRegisterWaitlistWithFullCapacity_expectCreatedWaitlistResponse()
        throws JsonProcessingException, UnsupportedEncodingException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User existingStudent = userRepository.save(UserFixture.createStudent("existing@test.com"));
        User applicant = userRepository.save(UserFixture.createStudent("applicant@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 1));

        enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, existingStudent));

        EnrollmentApplyRequest request = EnrollmentFixture.createEnrollmentApplyRequest(course.getId());
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments/waitlist")
            .header("X-User-Id", applicant.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isEqualTo(course.getId().intValue()))
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo("WAITING"));

        EnrollmentApplyResponse response =
            objectMapper.readValue(result.getResponse().getContentAsString(), EnrollmentApplyResponse.class);

        Enrollment enrollment = enrollmentRepository.findById(response.enrollmentId()).orElseThrow();
        assertThat(enrollment.isWaiting()).isTrue();
    }

    @Test
    @DisplayName("대기열 등록 API - 실패, 정원이 남아 있는 강의")
    void whenRegisterWaitlistWithAvailableCapacity_expectConflictResponse() throws JsonProcessingException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User applicant = userRepository.save(UserFixture.createStudent("applicant@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 2));

        EnrollmentApplyRequest request = EnrollmentFixture.createEnrollmentApplyRequest(course.getId());
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/enrollments/waitlist")
            .header("X-User-Id", applicant.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("ENROLLMENT_WAITLIST_NOT_AVAILABLE"));
    }

    @Test
    @DisplayName("수강 신청 API - 실패, 강의 id 누락")
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
    @DisplayName("내 수강 목록 조회 API - 성공")
    void whenGetMyEnrollmentsWithStudent_expectEnrollmentListResponse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course firstCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));
        Course secondCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment firstEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(firstCourse, student));
        Enrollment secondEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(secondCourse, student));
        secondEnrollment.confirm();

        Enrollment anotherEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(secondCourse, anotherStudent));
        anotherEnrollment.confirm();
        anotherEnrollment.cancel();

        MvcTestResult result = mvcTester.get().uri("/enrollments/me")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.totalElements", value -> assertThat(value).asNumber().isEqualTo(2))
            .hasPathSatisfying("$.totalPages", value -> assertThat(value).asNumber().isEqualTo(1))
            .hasPathSatisfying("$.hasNext", value -> assertThat(value).isEqualTo(false))
            .hasPathSatisfying("$.hasPrevious", value -> assertThat(value).isEqualTo(false))
            .hasPathSatisfying("$.content.length()", value -> assertThat(value).asNumber().isEqualTo(2))
            .hasPathSatisfying("$.content[0].enrollmentId", value -> assertThat(value).asNumber().isEqualTo(secondEnrollment.getId().intValue()))
            .hasPathSatisfying("$.content[0].courseId", value -> assertThat(value).asNumber().isEqualTo(secondCourse.getId().intValue()))
            .hasPathSatisfying("$.content[0].courseTitle", value -> assertThat(value).isEqualTo(secondCourse.getTitle()))
            .hasPathSatisfying("$.content[0].status", value -> assertThat(value).isEqualTo("CONFIRMED"))
            .hasPathSatisfying("$.content[1].enrollmentId", value -> assertThat(value).asNumber().isEqualTo(firstEnrollment.getId().intValue()))
            .hasPathSatisfying("$.content[1].courseId", value -> assertThat(value).asNumber().isEqualTo(firstCourse.getId().intValue()))
            .hasPathSatisfying("$.content[1].status", value -> assertThat(value).isEqualTo("PENDING"));
    }

    @Test
    @DisplayName("내 수강 목록 조회 API - 실패, 강사 권한")
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
    @DisplayName("수강 확정 API - 성공")
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
    @DisplayName("수강 확정 API - 실패, 강사 권한")
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
    @DisplayName("수강 확정 API - 실패, 본인 아님")
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
    @DisplayName("수강 확정 API - 실패, 존재하지 않는 수강 신청 내역")
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
    @DisplayName("수강 확정 API - 실패, 이미 확정된 신청")
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
    @DisplayName("수강 취소 API - 성공, PENDING 상태")
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
    @DisplayName("수강 취소 API - 성공, CONFIRMED 상태")
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
    @DisplayName("수강 취소 API - 실패, 결제 확정 7일 초과")
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
    @DisplayName("수강 취소 API - 실패, 강의 시작일 이후")
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
    @DisplayName("수강 취소 API - 실패, 강사 권한")
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
    @DisplayName("수강 취소 API - 실패, 본인 아님")
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
    @DisplayName("수강 취소 API - 실패, 존재하지 않는 신청")
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
    @DisplayName("수강 취소 API - 실패, 이미 취소된 신청")
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
