package leehs.course.core.enrollment.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
}
