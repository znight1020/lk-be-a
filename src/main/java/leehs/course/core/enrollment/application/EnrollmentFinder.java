package leehs.course.core.enrollment.application;

import leehs.course.core.enrollment.domain.model.Enrollment;

public interface EnrollmentFinder {

    Enrollment find(Long enrollmentId);
}
