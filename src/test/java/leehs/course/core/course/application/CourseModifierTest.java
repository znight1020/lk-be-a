package leehs.course.core.course.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.course.application.command.CourseStatusModifyCommand;
import leehs.course.core.course.domain.exception.CourseManagementForbiddenException;
import leehs.course.core.course.domain.exception.CourseNotOwnerException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class CourseModifierTest {

    @Autowired
    CourseModifier courseModifier;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void whenOpenCourseWithOwnerCreator_expectOpenStatus() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);

        Course modified = courseModifier.open(course.getId(), new CourseStatusModifyCommand(creator.getId()));

        assertThat(modified.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    void whenCloseCourseWithOwnerCreator_expectClosedStatus() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));
        course.open();
        assertThat(course.getStatus()).isEqualTo(CourseStatus.OPEN);

        Course modified = courseModifier.close(course.getId(), new CourseStatusModifyCommand(creator.getId()));

        assertThat(modified.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    void whenOpenCourseWithStudent_expectCourseManagementForbiddenException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        assertThatThrownBy(() -> courseModifier.open(course.getId(), new CourseStatusModifyCommand(student.getId())))
            .isInstanceOf(CourseManagementForbiddenException.class);
    }

    @Test
    void whenOpenCourseWithNonOwnerCreator_expectCourseNotOwnerException() {
        User owner = userRepository.save(UserFixture.createCreator("owner@test.com"));
        User anotherCreator = userRepository.save(UserFixture.createCreator("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(owner));

        assertThatThrownBy(
            () -> courseModifier.open(course.getId(), new CourseStatusModifyCommand(anotherCreator.getId())))
            .isInstanceOf(CourseNotOwnerException.class);
    }
}
