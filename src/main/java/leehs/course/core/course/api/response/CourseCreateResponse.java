package leehs.course.core.course.api.response;

import leehs.course.core.course.domain.model.Course;

public record CourseCreateResponse(Long courseId) {

    public static CourseCreateResponse of(Course course) {
        return new CourseCreateResponse(course.getId());
    }
}
