package leehs.course.core.enrollment.application;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.course.domain.exception.CourseStatusNotOpenException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.domain.exception.EnrollmentAlreadyExistsException;
import leehs.course.core.enrollment.domain.exception.EnrollmentCapacityExceededException;
import leehs.course.core.enrollment.domain.exception.EnrollmentForbiddenException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.model.EnrollmentStatus;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class EnrollmentApplierTest {

    @Autowired
    EnrollmentApplier enrollmentApplier;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void whenApplyEnrollmentWithStudentAndOpenCourse_expectPendingEnrollment() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentApplier.apply(new EnrollmentApplyCommand(student.getId(), course.getId()));

        entityManager.flush();
        entityManager.clear();

        Enrollment saved = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(saved.getStudent().getId()).isEqualTo(student.getId());
        assertThat(saved.getCourse().getId()).isEqualTo(course.getId());
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void whenApplyEnrollmentWithCreator_expectEnrollmentForbiddenException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        assertThatThrownBy(() -> enrollmentApplier.apply(new EnrollmentApplyCommand(creator.getId(), course.getId())))
            .isInstanceOf(EnrollmentForbiddenException.class);
    }

    @Test
    void whenApplyEnrollmentWithDraftCourse_expectCourseStatusNotOpenException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        assertThatThrownBy(() -> enrollmentApplier.apply(new EnrollmentApplyCommand(student.getId(), course.getId())))
            .isInstanceOf(CourseStatusNotOpenException.class);
    }

    @Test
    void whenApplyEnrollmentWithActiveEnrollment_expectEnrollmentAlreadyExistsException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        enrollmentRepository.save(Enrollment.apply(course, student));

        assertThatThrownBy(() -> enrollmentApplier.apply(new EnrollmentApplyCommand(student.getId(), course.getId())))
            .isInstanceOf(EnrollmentAlreadyExistsException.class);
    }

    @Test
    void whenApplyEnrollmentWithCancelledEnrollment_expectEnrollmentCreated() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment cancelledEnrollment = enrollmentRepository.save(Enrollment.apply(course, student));
        cancelledEnrollment.cancel();

        Enrollment enrollment = enrollmentApplier.apply(new EnrollmentApplyCommand(student.getId(), course.getId()));
        assertThat(enrollment.getId()).isNotNull();
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(PENDING, CONFIRMED)))
            .isEqualTo(1L);
    }

    @Test
    void whenApplyEnrollmentWithFullCapacity_expectEnrollmentCapacityExceededException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User pendingStudent = userRepository.save(UserFixture.createStudent("pending@test.com"));
        User confirmedStudent = userRepository.save(UserFixture.createStudent("confirmed@test.com"));
        User applicant = userRepository.save(UserFixture.createStudent("applicant@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 2));

        enrollmentRepository.save(Enrollment.apply(course, pendingStudent));

        Enrollment confirmedEnrollment = enrollmentRepository.save(Enrollment.apply(course, confirmedStudent));
        confirmedEnrollment.confirm();

        assertThatThrownBy(() -> enrollmentApplier.apply(new EnrollmentApplyCommand(applicant.getId(), course.getId())))
            .isInstanceOf(EnrollmentCapacityExceededException.class);
    }

    @Test
    void whenApplyEnrollmentWithOnlyCancelledEnrollment_expectCapacityAvailable() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User cancelledStudent = userRepository.save(UserFixture.createStudent("cancelled@test.com"));
        User applicant = userRepository.save(UserFixture.createStudent("applicant@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 1));

        Enrollment cancelledEnrollment = enrollmentRepository.save(Enrollment.apply(course, cancelledStudent));
        cancelledEnrollment.cancel();

        Enrollment enrollment = enrollmentApplier.apply(new EnrollmentApplyCommand(applicant.getId(), course.getId()));
        assertThat(enrollment.getId()).isNotNull();
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(PENDING, CONFIRMED)))
            .isEqualTo(1L);
    }
}
