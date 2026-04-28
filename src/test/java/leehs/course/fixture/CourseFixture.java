package leehs.course.fixture;

import java.time.LocalDate;

import leehs.course.core.course.api.request.CourseCreateRequest;
import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.user.domain.model.User;

public class CourseFixture {

    public static Course createCourse() {
        return createCourse(UserFixture.createCreator("creator@test.com"));
    }

    public static Course createCourse(User user) {
        return createCourse(user, 30);
    }

    public static Course createCourse(User user, int capacity) {
        return Course.create(user,
            "title", "description", 10000, capacity,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)
        );
    }

    public static Course createOpenCourse(User user) {
        return createOpenCourse(user, 30);
    }

    public static Course createOpenCourse(User user, int capacity) {
        Course course = createCourse(user, capacity);
        course.open();

        return course;
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
