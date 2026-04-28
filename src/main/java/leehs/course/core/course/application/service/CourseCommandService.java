package leehs.course.core.course.application.service;

import static leehs.course.core.user.domain.model.UserRole.CREATOR;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.application.CourseCreator;
import leehs.course.core.course.application.CourseFinder;
import leehs.course.core.course.application.CourseModifier;
import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.application.command.CourseStatusModifyCommand;
import leehs.course.core.course.domain.exception.CourseManagementForbiddenException;
import leehs.course.core.course.domain.exception.CourseNotOwnerException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.user.application.UserFinder;
import leehs.course.core.user.domain.model.User;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseCommandService implements CourseCreator, CourseModifier {

    private final CourseRepository courseRepository;
    private final CourseFinder courseFinder;

    private final UserFinder userFinder;

    @Override
    public Course create(CourseCreateCommand command) {
        User requestUser = userFinder.find(command.requestUserId());
        verifyRole(requestUser);

        Course course = Course.create(requestUser,
            command.title(), command.description(), command.price(), command.capacity(),
            command.startDate(), command.endDate()
        );

        return courseRepository.save(course);
    }

    @Override
    public Course open(Long courseId, CourseStatusModifyCommand command) {
        verifyRole(userFinder.find(command.requestUserId()));

        Course course = courseFinder.find(courseId);
        verifyCourseOwner(course, command.requestUserId());

        course.open();

        return course;
    }

    @Override
    public Course close(Long courseId, CourseStatusModifyCommand command) {
        verifyRole(userFinder.find(command.requestUserId()));

        Course course = courseFinder.find(courseId);
        verifyCourseOwner(course, command.requestUserId());

        course.close();

        return course;
    }

    private void verifyRole(User requestUser) {
        if (requestUser.getRole() != CREATOR)
            throw new CourseManagementForbiddenException();
    }

    private static void verifyCourseOwner(Course course, Long reqeustUserId) {
        if (!Objects.equals(course.getCreator().getId(), reqeustUserId))
            throw new CourseNotOwnerException();
    }
}
