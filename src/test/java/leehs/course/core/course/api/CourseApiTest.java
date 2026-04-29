package leehs.course.core.course.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("강의 생성 API - 성공")
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
    @DisplayName("강의 생성 API - 실패, 학생 권한")
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
    @DisplayName("강의 생성 API - 실패, 사용자 헤더 누락")
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

    @Test
    @DisplayName("강의 목록 조회 API - 성공, 상태 조건 없음")
    void whenGetCoursesWithoutStatus_expectCourseListResponse() {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));

        Course draftCourse = courseRepository.save(CourseFixture.createCourse(creator));
        Course openCourse = courseRepository.save(CourseFixture.createCourse(creator));
        openCourse.open();

        MvcTestResult result = mvcTester.get().uri("/courses")
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.length()", value -> assertThat(value).asNumber().isEqualTo(2))
            .hasPathSatisfying("$[0].courseId", value -> assertThat(value).asNumber().isEqualTo(openCourse.getId().intValue()))
            .hasPathSatisfying("$[0].status", value -> assertThat(value).isEqualTo("OPEN"))
            .hasPathSatisfying("$[1].courseId", value -> assertThat(value).asNumber().isEqualTo(draftCourse.getId().intValue()))
            .hasPathSatisfying("$[1].status", value -> assertThat(value).isEqualTo("DRAFT"));
    }

    @Test
    @DisplayName("강의 목록 조회 API - 성공, OPEN 필터")
    void whenGetCoursesWithOpenStatus_expectOpenCourseListResponse() {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));

        Course draftCourse = courseRepository.save(CourseFixture.createCourse(creator));
        Course openCourse = courseRepository.save(CourseFixture.createCourse(creator));
        openCourse.open();

        MvcTestResult result = mvcTester.get().uri("/courses?status=OPEN")
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.length()", value -> assertThat(value).asNumber().isEqualTo(1))
            .hasPathSatisfying("$[0].courseId", value -> assertThat(value).asNumber().isEqualTo(openCourse.getId().intValue()))
            .hasPathSatisfying("$[0].status", value -> assertThat(value).isEqualTo("OPEN"));

        assertThat(draftCourse.getStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    @DisplayName("강의 목록 조회 API - 실패, 잘못된 상태 값")
    void whenGetCoursesWithInvalidStatus_expectBadRequestResponse() {
        MvcTestResult result = mvcTester.get().uri("/courses?status=INVALID")
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("강의 상세 조회 API - 성공")
    void whenGetCourseWithExistingId_expectCourseDetailResponse() {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        MvcTestResult result = mvcTester.get().uri("/courses/" + course.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isEqualTo(course.getId().intValue()))
            .hasPathSatisfying("$.creatorId", value -> assertThat(value).asNumber().isEqualTo(creator.getId().intValue()))
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo(course.getTitle()))
            .hasPathSatisfying("$.description", value -> assertThat(value).isEqualTo(course.getDescription()))
            .hasPathSatisfying("$.price", value -> assertThat(value).asNumber().isEqualTo(course.getPrice()))
            .hasPathSatisfying("$.capacity", value -> assertThat(value).asNumber().isEqualTo(course.getCapacity()))
            .hasPathSatisfying("$.currentEnrollmentCount", value -> assertThat(value).asNumber().isEqualTo(0))
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo(course.getStatus().name()));
    }

    @Test
    @DisplayName("강의 상세 조회 API - 실패, 존재하지 않는 강의")
    void whenGetCourseWithNonExistingId_expectNotFoundResponse() {
        MvcTestResult result = mvcTester.get().uri("/courses/999")
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("COURSE_NOT_FOUND"));
    }

    @Test
    @DisplayName("강의 오픈 API - 성공")
    void whenOpenCourseWithOwnerCreator_expectOpenCourseResponse() {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        MvcTestResult result = mvcTester.patch().uri("/courses/" + course.getId() + "/open")
            .header("X-User-Id", creator.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isEqualTo(course.getId().intValue()))
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo("OPEN"));

        Course openedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(openedCourse.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("강의 오픈 API - 실패, 학생 권한")
    void whenOpenCourseWithStudentUserId_expectForbiddenResponse() {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));
        User student = userRegister.register(UserFixture.createStudentRegisterCommand("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        MvcTestResult result = mvcTester.patch().uri("/courses/" + course.getId() + "/open")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("COURSE_MANAGEMENT_FORBIDDEN"));
    }

    @Test
    @DisplayName("강의 오픈 API - 실패, 강의를 소유하지 않은 강사")
    void whenOpenCourseWithNonOwnerCreator_expectForbiddenResponse() {
        User owner = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));
        User anotherCreator = userRegister.register(UserFixture.createCreatorRegisterCommand("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(owner));

        MvcTestResult result = mvcTester.patch().uri("/courses/" + course.getId() + "/open")
            .header("X-User-Id", anotherCreator.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("COURSE_NOT_OWNER"));
    }

    @Test
    @DisplayName("강의 마감 API - 성공")
    void whenCloseCourseWithOwnerCreator_expectClosedCourseResponse() {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));
        course.open();

        MvcTestResult result = mvcTester.patch().uri("/courses/" + course.getId() + "/close")
            .header("X-User-Id", creator.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.courseId", value -> assertThat(value).asNumber().isEqualTo(course.getId().intValue()))
            .hasPathSatisfying("$.status", value -> assertThat(value).isEqualTo("CLOSED"));

        Course closedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(closedCourse.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    @DisplayName("강의 마감 API - 실패, 학생 권한")
    void whenCloseCourseWithStudentUserId_expectForbiddenResponse() {
        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));
        User student = userRegister.register(UserFixture.createStudentRegisterCommand("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));
        course.open();

        MvcTestResult result = mvcTester.patch().uri("/courses/" + course.getId() + "/close")
            .header("X-User-Id", student.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("COURSE_MANAGEMENT_FORBIDDEN"));
    }

    @Test
    @DisplayName("강의 마감 API - 실패, 강의를 소유하지 않은 강사")
    void whenCloseCourseWithNonOwnerCreator_expectForbiddenResponse() {
        User owner = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));
        User anotherCreator = userRegister.register(UserFixture.createCreatorRegisterCommand("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(owner));
        course.open();

        MvcTestResult result = mvcTester.patch().uri("/courses/" + course.getId() + "/close")
            .header("X-User-Id", anotherCreator.getId())
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.FORBIDDEN)
            .bodyJson()
            .hasPathSatisfying("$.title", value -> assertThat(value).isEqualTo("COURSE_NOT_OWNER"));
    }
}
