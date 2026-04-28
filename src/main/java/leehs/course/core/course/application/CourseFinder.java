package leehs.course.core.course.application;

import java.util.List;

import leehs.course.core.course.application.query.CourseFindQuery;
import leehs.course.core.course.domain.model.Course;

public interface CourseFinder {

    Course find(Long courseId);

    List<Course> findAll(CourseFindQuery query);
}
