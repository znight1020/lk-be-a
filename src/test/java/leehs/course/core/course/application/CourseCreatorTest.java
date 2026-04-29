package leehs.course.core.course.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.domain.exception.CourseManagementForbiddenException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.core.user.domain.exception.UserNotFoundException;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class CourseCreatorTest {

    @Autowired
    CourseCreator courseCreator;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("강의 생성 - 성공")
    void whenCreateCourseWithCreator_expectCreatedCourse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        CourseCreateCommand command = CourseFixture.createCourseCreateCommand(creator.getId());

        Course course = courseCreator.create(command);
        assertThat(course.getId()).isNotNull();
        assertThat(course.getCreator().getId()).isEqualTo(command.requestUserId());
        assertThat(course.getTitle()).isEqualTo(command.title());
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(course.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("강의 생성 - 실패, 학생 권한")
    void whenCreateCourseWithStudent_expectCourseManagementForbiddenException() {
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        CourseCreateCommand command = CourseFixture.createCourseCreateCommand(student.getId());

        assertThatThrownBy(() -> courseCreator.create(command))
            .isInstanceOf(CourseManagementForbiddenException.class);
    }

    @Test
    @DisplayName("강의 생성 - 실패, 존재하지 않는 사용자")
    void whenCreateCourseWithNonExistentUser_expectIllegalArgumentException() {
        Long nonExistentUserId = 999L;

        CourseCreateCommand command = CourseFixture.createCourseCreateCommand(nonExistentUserId);

        assertThatThrownBy(() -> courseCreator.create(command))
            .isInstanceOf(UserNotFoundException.class);
    }
}
