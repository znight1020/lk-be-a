package leehs.course.core.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.application.command.EnrollmentStatusModifyCommand;
import leehs.course.core.enrollment.domain.exception.EnrollmentForbiddenException;
import leehs.course.core.enrollment.domain.exception.EnrollmentNotOwnerException;
import leehs.course.core.enrollment.domain.exception.EnrollmentStatusNotPendingException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.model.EnrollmentStatus;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.EnrollmentFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class EnrollmentModifierTest {

    @Autowired
    EnrollmentModifier enrollmentModifier;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void whenConfirmEnrollmentWithOwnerStudent_expectConfirmedEnrollment() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());
        Enrollment confirmed = enrollmentModifier.confirm(enrollment.getId(), command);

        em.flush();
        em.clear();

        Enrollment saved = enrollmentRepository.findById(confirmed.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(saved.getConfirmedAt()).isNotNull();
    }

    @Test
    void whenConfirmEnrollmentWithCreator_expectEnrollmentForbiddenException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        assertThatThrownBy(
            () -> enrollmentModifier.confirm(enrollment.getId(), new EnrollmentStatusModifyCommand(creator.getId())))
            .isInstanceOf(EnrollmentForbiddenException.class);
    }

    @Test
    void whenConfirmEnrollmentWithNonOwnerStudent_expectEnrollmentNotOwnerException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User ownerStudent = userRepository.save(UserFixture.createStudent("owner@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, ownerStudent));

        assertThatThrownBy(() -> enrollmentModifier.confirm(enrollment.getId(),
            new EnrollmentStatusModifyCommand(anotherStudent.getId())))
            .isInstanceOf(EnrollmentNotOwnerException.class);
    }

    @Test
    void whenConfirmAlreadyConfirmedEnrollment_expectEnrollmentStatusNotPendingException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();

        assertThatThrownBy(
            () -> enrollmentModifier.confirm(enrollment.getId(), new EnrollmentStatusModifyCommand(student.getId())))
            .isInstanceOf(EnrollmentStatusNotPendingException.class);
    }
}
