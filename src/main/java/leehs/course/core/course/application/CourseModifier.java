package leehs.course.core.course.application;

import leehs.course.core.course.application.command.CourseStatusModifyCommand;
import leehs.course.core.course.domain.model.Course;

public interface CourseModifier {

    Course open(Long courseId, CourseStatusModifyCommand command);

    Course close(Long courseId, CourseStatusModifyCommand command);
}
