package leehs.course.core.enrollment.application;

import java.util.List;

import leehs.course.core.enrollment.application.query.EnrollmentFindQuery;
import leehs.course.core.enrollment.domain.model.Enrollment;

public interface EnrollmentFinder {

    Enrollment find(Long enrollmentId);

    List<Enrollment> findAll(EnrollmentFindQuery query);
}
