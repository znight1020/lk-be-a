package leehs.course.fixture;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.enrollment.api.request.EnrollmentApplyRequest;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.user.domain.model.User;

public class EnrollmentFixture {

    public static Enrollment createEnrollment(Course course, User user) {
        return Enrollment.apply(course, user);
    }

    public static Enrollment createEnrollment() {
        return createEnrollment(CourseFixture.createCourse(), UserFixture.createStudent("student@test.com"));
    }

    public static EnrollmentApplyRequest createEnrollmentApplyRequest(Long courseId) {
        return new EnrollmentApplyRequest(courseId);
    }

    public static EnrollmentApplyRequest createInvalidEnrollmentApplyRequest() {
        return new EnrollmentApplyRequest(null);
    }
}
