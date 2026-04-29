package leehs.course.core.course.domain.model;

import static leehs.course.fixture.CourseFixture.createCourse;
import static leehs.course.fixture.UserFixture.createCreator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import leehs.course.core.course.domain.exception.CourseStatusNotDraftException;
import leehs.course.core.course.domain.exception.CourseStatusNotOpenException;

class CourseTest {

    @Test
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
    }

    @Test
    void whenCreateValidCourse_expectDraftStatus() {
        Course course = createCourse();

        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(course.getStartDate()).isNotNull();
        assertThat(course.getEndDate()).isNotNull();
        assertThat(course.getStartDate()).isBeforeOrEqualTo(course.getEndDate());
    }

    @Test
    void whenOpenDraftCourse_expectOpenStatus() {
        Course course = createCourse();

        course.open();

        assertThat(course.isOpen()).isTrue();
    }

    @Test
    void whenOpenNonDraftCourse_expectCourseStatusNotDraftException() {
        Course course = createCourse();
        course.open();

        assertThatThrownBy(course::open)
            .isInstanceOf(CourseStatusNotDraftException.class);
    }

    @Test
    void whenCloseDraftCourse_expectCourseStatusNotOpenException() {
        Course course = createCourse();

        assertThatThrownBy(course::close)
            .isInstanceOf(CourseStatusNotOpenException.class);
    }

    @Test
    void whenCloseOpenCourse_expectClosedStatus() {
        Course course = createCourse();
        course.open();

        course.close();

        assertThat(course.isClosed()).isTrue();
    }

    @Test
    void whenCloseClosedCourse_expectCourseStatusNotOpenException() {
        Course course = createCourse();
        course.open();
        course.close();

        assertThatThrownBy(course::close)
            .isInstanceOf(CourseStatusNotOpenException.class);
    }
}
