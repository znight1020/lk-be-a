package leehs.course.core.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("수강 확정 - 성공, 학생 본인")
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
    @DisplayName("수강 확정 - 실패, 강사 권한")
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
    @DisplayName("수강 확정 - 실패, 본인 아님")
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
    @DisplayName("수강 확정 - 실패, 이미 확정된 신청")
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
    @DisplayName("수강 취소 - 성공, PENDING 상태")
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
    @DisplayName("수강 취소 - 성공, 활성 신청 취소 시 가장 오래된 대기자의 신청이 PENDING 상태로 변경")
    void whenCancelActiveEnrollmentWithWaitingEnrollment_expectOldestWaitingEnrollmentPromotedToPending() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User activeStudent = userRepository.save(UserFixture.createStudent("active@test.com"));
        User firstWaitingStudent = userRepository.save(UserFixture.createStudent("waiting1@test.com"));
        User secondWaitingStudent = userRepository.save(UserFixture.createStudent("waiting2@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 1));

        Enrollment activeEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, activeStudent));
        Enrollment firstWaitingEnrollment = enrollmentRepository.save(EnrollmentFixture.createWaitlistEnrollment(course, firstWaitingStudent));
        Enrollment secondWaitingEnrollment = enrollmentRepository.save(EnrollmentFixture.createWaitlistEnrollment(course, secondWaitingStudent));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(activeStudent.getId());
        enrollmentModifier.cancel(activeEnrollment.getId(), command);

        em.flush();
        em.clear();

        Enrollment cancelled = enrollmentRepository.findById(activeEnrollment.getId()).orElseThrow();
        Enrollment promoted = enrollmentRepository.findById(firstWaitingEnrollment.getId()).orElseThrow();
        Enrollment remainingWaiting = enrollmentRepository.findById(secondWaitingEnrollment.getId()).orElseThrow();

        assertThat(cancelled.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(promoted.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(remainingWaiting.getStatus()).isEqualTo(EnrollmentStatus.WAITING);
    }

    @Test
    @DisplayName("수강 취소 - 성공, CONFIRMED 상태")
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
    @DisplayName("수강 취소 - 성공, WAITING 상태")
    void whenCancelWaitingEnrollmentWithOwnerStudent_expectCancelledEnrollment() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createWaitlistEnrollment(course, student));

        EnrollmentStatusModifyCommand command = new EnrollmentStatusModifyCommand(student.getId());
        Enrollment cancelled = enrollmentModifier.cancel(enrollment.getId(), command);

        em.flush();
        em.clear();

        Enrollment saved = enrollmentRepository.findById(cancelled.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(saved.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("수강 취소 - 실패, 결제 확정 7일 초과")
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
    @DisplayName("수강 취소 - 실패, 강의 시작일 이후 취소")
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
    @DisplayName("수강 취소 - 실패, 강사 권한")
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
    @DisplayName("수강 취소 - 실패, 본인 아님")
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
    @DisplayName("수강 취소 - 실패, 이미 취소된 신청")
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
