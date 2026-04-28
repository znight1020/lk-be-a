package leehs.course.core.course.application;

import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.domain.model.Course;

public interface CourseCreator {

    Course create(CourseCreateCommand command);
}
