package leehs.course.fixture;

import java.time.LocalDate;

import leehs.course.core.course.api.request.CourseCreateRequest;
import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.domain.model.Course;

public class CourseFixture {

    public static Course createCourse() {
        return Course.create(
            UserFixture.createCreator("creator@test.com"),
            "title",
            "description",
            10000,
            30,
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 31)
        );
    }

    public static CourseCreateCommand createCourseCreateCommand(Long userId) {
        return new CourseCreateCommand(userId,
            "title", "description", 10000, 30,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
    }

    public static CourseCreateRequest createCourseCreateRequest() {
        return new CourseCreateRequest(
            "title", "description", 10000, 30,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)
        );
    }
}
