package leehs.course.core.course.api.response;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;

public record CourseStatusModifyResponse(Long courseId, CourseStatus status) {

    public static CourseStatusModifyResponse of(Course course) {
        return new CourseStatusModifyResponse(course.getId(), course.getStatus());
    }
}
