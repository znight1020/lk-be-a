package leehs.course.core.course.application;

import leehs.course.core.course.domain.model.Course;

public interface CourseLockFinder {

    Course findWithLock(Long courseId);
}
