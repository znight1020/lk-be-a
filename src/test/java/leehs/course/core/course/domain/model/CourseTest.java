package leehs.course.core.course.domain.model;

import static leehs.course.fixture.CourseFixture.createCourse;
import static leehs.course.fixture.UserFixture.createCreator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import leehs.course.core.course.domain.exception.CourseStatusNotDraftException;
import leehs.course.core.course.domain.exception.CourseStatusNotOpenException;

class CourseTest {

    @Test
    @DisplayName("강의 생성 - 실패, 잘못된 입력값")
    void whenCreateCourseWithInvalidArgument_expectIllegalArgumentException() {
        // 가격이 음수인 경우
        assertThatThrownBy(() -> Course.create(createCreator("creator@test.com"), "title", "description", -1, 30,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("price must be greater than or equal to 0");

        // 정원이 음수 or 0인 경우
        assertThatThrownBy(() -> Course.create(createCreator("creator@test.com"), "title", "description", 10000, 0,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("capacity must be greater than or equal to 1");

        // 시작일이 종료일보다 늦는 경우
        assertThatThrownBy(() -> Course.create(createCreator("creator@test.com"), "title", "description", 10000, 30,
            LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("startDate must not be after endDate");

        // 시작일이 종료일과 같은 경우
        assertDoesNotThrow(() -> Course.create(createCreator("creator@test.com"), "title", "description", 10000, 30,
            LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 31)));

        // 시작일이 과거인 경우
        assertThatThrownBy(() -> Course.create(createCreator("creator@test.com"), "title", "description", 10000, 30,
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(10)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("startDate must not be before today");

        assertDoesNotThrow(() -> Course.create(createCreator("creator@test.com"), "title", "description", 10000, 30,
            LocalDate.now(), LocalDate.now()));
    }

    @Test
    @DisplayName("강의 생성 - 성공, 기본 상태는 DRAFT")
    void whenCreateValidCourse_expectDraftStatus() {
        Course course = createCourse();

        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(course.getStartDate()).isNotNull();
        assertThat(course.getEndDate()).isNotNull();
        assertThat(course.getStartDate()).isBeforeOrEqualTo(course.getEndDate());
    }

    @Test
    @DisplayName("강의 시작 - 성공, DRAFT -> OPEN")
    void whenOpenDraftCourse_expectOpenStatus() {
        Course course = createCourse();

        course.open();

        assertThat(course.isOpen()).isTrue();
    }

    @Test
    @DisplayName("강의 시작 - 실패, DRAFT 상태 아님")
    void whenOpenNonDraftCourse_expectCourseStatusNotDraftException() {
        Course course = createCourse();
        course.open();

        assertThatThrownBy(course::open)
            .isInstanceOf(CourseStatusNotDraftException.class);
    }

    @Test
    @DisplayName("강의 마감 - 실패, OPEN 상태 아님")
    void whenCloseDraftCourse_expectCourseStatusNotOpenException() {
        Course course = createCourse();

        assertThatThrownBy(course::close)
            .isInstanceOf(CourseStatusNotOpenException.class);
    }

    @Test
    @DisplayName("강의 마감 - 성공, OPEN -> CLOSED")
    void whenCloseOpenCourse_expectClosedStatus() {
        Course course = createCourse();
        course.open();

        course.close();

        assertThat(course.isClosed()).isTrue();
    }

    @Test
    @DisplayName("강의 마감 - 실패, 이미 CLOSED")
    void whenCloseClosedCourse_expectCourseStatusNotOpenException() {
        Course course = createCourse();
        course.open();
        course.close();

        assertThatThrownBy(course::close)
            .isInstanceOf(CourseStatusNotOpenException.class);
    }
}
