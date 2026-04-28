package leehs.course.core.course.application;

import leehs.course.core.course.domain.model.Course;

public interface CourseFinder {

    Course find(Long courseId);
}
