package leehs.course.core.course.api;

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

import leehs.course.core.course.api.response.CourseCreateResponse;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.user.application.UserRegister;
import leehs.course.core.user.domain.model.User;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class CourseApiTest {

    final MockMvcTester mvcTester;

    final CourseRepository courseRepository;

    final UserRegister userRegister;

    final ObjectMapper objectMapper;

    @Test
    void whenCreateCourseRequestIsValid_expectCreatedCourseResponse() throws JsonProcessingException, UnsupportedEncodingException {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));

        var request = CourseFixture.createCourseCreateRequest();
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/courses")
            .header("X-User-Id", creator.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isNotNull());

        CourseCreateResponse response =
            objectMapper.readValue(result.getResponse().getContentAsString(), CourseCreateResponse.class);

        Course course = courseRepository.findById(response.courseId()).orElseThrow();
        assertThat(course.getCreator().getId()).isEqualTo(creator.getId());
        assertThat(course.getTitle()).isEqualTo(request.title());
        assertThat(course.getDescription()).isEqualTo(request.description());
        assertThat(course.getPrice()).isEqualTo(request.price());
        assertThat(course.getCapacity()).isEqualTo(request.capacity());
        assertThat(course.getStartDate()).isEqualTo(request.startDate());
        assertThat(course.getEndDate()).isEqualTo(request.endDate());
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    void whenCreateCourseWithStudentUserId_expectForbiddenResponse() throws JsonProcessingException {
        User student = userRegister.register(UserFixture.createStudentRegisterCommand("student@test.com"));

        var request = CourseFixture.createCourseCreateRequest();
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/courses")
            .header("X-User-Id", student.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("COURSE_MANAGEMENT_FORBIDDEN"));
    }

    @Test
    void whenCreateCourseWithoutUserIdHeader_expectUnauthorizedResponse() throws JsonProcessingException {
        var request = CourseFixture.createCourseCreateRequest();
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/courses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("REQUEST_USER_ID_HEADER_MISSING"))
            .hasPathSatisfying("$.detail", value -> assertThat(value).asString().contains("X-User-Id"));
    }
}
