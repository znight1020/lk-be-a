package leehs.course.core.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.application.command.EnrollmentStatusModifyCommand;
import leehs.course.core.enrollment.domain.exception.EnrollmentCancellationPeriodExpiredException;
import leehs.course.core.enrollment.domain.exception.EnrollmentForbiddenException;
import leehs.course.core.enrollment.domain.exception.EnrollmentNotOwnerException;
import leehs.course.core.enrollment.domain.exception.EnrollmentStatusAlreadyCancelledException;
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

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(creator.getId());

        assertThatThrownBy(() -> enrollmentModifier.confirm(enrollment.getId(), command))
            .isInstanceOf(EnrollmentForbiddenException.class);
    }

    @Test
    void whenConfirmEnrollmentWithNonOwnerStudent_expectEnrollmentNotOwnerException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User ownerStudent = userRepository.save(UserFixture.createStudent("owner@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, ownerStudent));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(anotherStudent.getId());

        assertThatThrownBy(() -> enrollmentModifier.confirm(enrollment.getId(), command))
            .isInstanceOf(EnrollmentNotOwnerException.class);
    }

    @Test
    void whenConfirmAlreadyConfirmedEnrollment_expectEnrollmentStatusNotPendingException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());

        assertThatThrownBy(() -> enrollmentModifier.confirm(enrollment.getId(), command))
            .isInstanceOf(EnrollmentStatusNotPendingException.class);
    }

    @Test
    void whenCancelPendingEnrollmentWithOwnerStudent_expectCancelledEnrollment() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());
        Enrollment cancelled = enrollmentModifier.cancel(enrollment.getId(), command);

        em.flush();
        em.clear();

        Enrollment saved = enrollmentRepository.findById(cancelled.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(saved.getCancelledAt()).isNotNull();
    }

    @Test
    void whenCancelConfirmedEnrollmentWithOwnerStudent_expectCancelledEnrollment() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        ReflectionTestUtils.setField(course, "startDate", LocalDate.now().plusDays(1));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());
        Enrollment cancelled = enrollmentModifier.cancel(enrollment.getId(), command);

        em.flush();
        em.clear();

        Enrollment saved = enrollmentRepository.findById(cancelled.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(saved.getCancelledAt()).isNotNull();
    }

    @Test
    void whenCancelConfirmedEnrollmentAfterConfirmedWindow_expectEnrollmentCancellationPeriodExpiredException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        ReflectionTestUtils.setField(course, "startDate", LocalDate.now().plusDays(1));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();
        ReflectionTestUtils.setField(enrollment, "confirmedAt", LocalDateTime.now().minusDays(8));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());

        assertThatThrownBy(() -> enrollmentModifier.cancel(enrollment.getId(), command))
            .isInstanceOf(EnrollmentCancellationPeriodExpiredException.class);
    }

    @Test
    void whenCancelConfirmedEnrollmentOnCourseStartDate_expectEnrollmentCancellationPeriodExpiredException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        ReflectionTestUtils.setField(course, "startDate", LocalDate.now());

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());

        assertThatThrownBy(() -> enrollmentModifier.cancel(enrollment.getId(), command))
            .isInstanceOf(EnrollmentCancellationPeriodExpiredException.class);
    }

    @Test
    void whenCancelEnrollmentWithCreator_expectEnrollmentForbiddenException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(creator.getId());

        assertThatThrownBy(() -> enrollmentModifier.cancel(enrollment.getId(), command))
            .isInstanceOf(EnrollmentForbiddenException.class);
    }

    @Test
    void whenCancelEnrollmentWithNonOwnerStudent_expectEnrollmentNotOwnerException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User ownerStudent = userRepository.save(UserFixture.createStudent("owner@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, ownerStudent));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(anotherStudent.getId());

        assertThatThrownBy(() -> enrollmentModifier.cancel(enrollment.getId(), command))
            .isInstanceOf(EnrollmentNotOwnerException.class);
    }

    @Test
    void whenCancelAlreadyCancelledEnrollment_expectEnrollmentStatusAlreadyCancelledException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        enrollment.confirm();
        enrollment.cancel();

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());

        assertThatThrownBy(() -> enrollmentModifier.cancel(enrollment.getId(), command))
            .isInstanceOf(EnrollmentStatusAlreadyCancelledException.class);
    }
}
