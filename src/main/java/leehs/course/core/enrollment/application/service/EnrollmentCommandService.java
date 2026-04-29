package leehs.course.core.enrollment.application.service;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.WAITING;
import static leehs.course.core.user.domain.model.UserRole.STUDENT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.application.CourseLockFinder;
import leehs.course.core.course.domain.exception.CourseStatusNotOpenException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.enrollment.application.EnrollmentApplier;
import leehs.course.core.enrollment.application.EnrollmentFinder;
import leehs.course.core.enrollment.application.EnrollmentModifier;
import leehs.course.core.enrollment.application.EnrollmentWaitlistRegister;
import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.application.command.EnrollmentStatusModifyCommand;
import leehs.course.core.enrollment.domain.exception.EnrollmentAlreadyExistsException;
import leehs.course.core.enrollment.domain.exception.EnrollmentCancellationPeriodExpiredException;
import leehs.course.core.enrollment.domain.exception.EnrollmentCapacityExceededException;
import leehs.course.core.enrollment.domain.exception.EnrollmentForbiddenException;
import leehs.course.core.enrollment.domain.exception.EnrollmentNotOwnerException;
import leehs.course.core.enrollment.domain.exception.EnrollmentWaitlistNotAvailableException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.model.EnrollmentStatus;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.application.UserFinder;
import leehs.course.core.user.domain.model.User;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentCommandService implements EnrollmentApplier, EnrollmentModifier, EnrollmentWaitlistRegister {

    private static final int CANCELLATION_PERIOD_DAYS = 7;
    private static final List<EnrollmentStatus> ACTIVE_STATUSES = List.of(PENDING, CONFIRMED);
    private static final List<EnrollmentStatus> NON_CANCELLED_STATUSES = List.of(PENDING, WAITING, CONFIRMED);

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentFinder enrollmentFinder;

    private final CourseLockFinder courseLockFinder;

    private final UserFinder userFinder;

    @Override
    public Enrollment apply(EnrollmentApplyCommand command) {
        User requestUser = userFinder.find(command.requestUserId());
        verifyStudentRole(requestUser);

        Course course = courseLockFinder.findWithLock(command.courseId());
        verifyOpenCourse(course);

        verifyNoEnrollment(course.getId(), requestUser.getId());
        verifyCapacityAvailable(course.getId(), course.getCapacity());

        Enrollment enrollment = Enrollment.apply(course, requestUser);

        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Enrollment registerWaitlist(EnrollmentApplyCommand command) {
        User requestUser = userFinder.find(command.requestUserId());
        verifyStudentRole(requestUser);

        Course course = courseLockFinder.findWithLock(command.courseId());
        verifyOpenCourse(course);

        verifyNoEnrollment(course.getId(), requestUser.getId());
        verifyCapacityFull(course.getId(), course.getCapacity());

        Enrollment enrollment = Enrollment.waitlist(course, requestUser);

        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Enrollment confirm(Long enrollmentId, EnrollmentStatusModifyCommand command) {
        User requestUser = userFinder.find(command.requestUserId());
        verifyStudentRole(requestUser);

        Enrollment enrollment = enrollmentFinder.find(enrollmentId);
        verifyEnrollmentOwner(enrollment, requestUser.getId());

        enrollment.confirm();

        // 외부 결제 시스템 미연동

        return enrollment;
    }

    @Override
    public Enrollment cancel(Long enrollmentId, EnrollmentStatusModifyCommand command) {
        User requestUser = userFinder.find(command.requestUserId());
        verifyStudentRole(requestUser);

        Enrollment enrollment = enrollmentFinder.find(enrollmentId);
        verifyEnrollmentOwner(enrollment, requestUser.getId());

        if (enrollment.isConfirmed())
            verifyCancellationPeriod(enrollment);

        if (!enrollment.isActive()) {
            enrollment.cancel();
            return enrollment;
        }

        Course course = courseLockFinder.findWithLock(enrollment.getCourse().getId());
        enrollment.cancel();

        promoteFirstWaitingEnrollment(course.getId(), course.getCapacity());

        return enrollment;
    }

    private void verifyStudentRole(User requestUser) {
        if (requestUser.getRole() != STUDENT)
            throw new EnrollmentForbiddenException();
    }

    private static void verifyOpenCourse(Course course) {
        if (!course.isOpen())
            throw new CourseStatusNotOpenException();
    }

    private void verifyNoEnrollment(Long courseId, Long studentId) {
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatusIn(courseId, studentId, NON_CANCELLED_STATUSES))
            throw new EnrollmentAlreadyExistsException();
    }

    private void verifyCapacityAvailable(Long courseId, Integer capacity) {
        long activeEnrollmentCount = enrollmentRepository.countByCourseIdAndStatusIn(courseId, ACTIVE_STATUSES);

        if (activeEnrollmentCount >= capacity)
            throw new EnrollmentCapacityExceededException();
    }

    private void verifyCapacityFull(Long courseId, Integer capacity) {
        long activeEnrollmentCount = enrollmentRepository.countByCourseIdAndStatusIn(courseId, ACTIVE_STATUSES);

        if (activeEnrollmentCount < capacity)
            throw new EnrollmentWaitlistNotAvailableException();
    }

    private void promoteFirstWaitingEnrollment(Long courseId, Integer capacity) {
        long activeEnrollmentCount = enrollmentRepository.countByCourseIdAndStatusIn(courseId, ACTIVE_STATUSES);

        if (activeEnrollmentCount >= capacity)
            return;

        enrollmentRepository.findFirstByCourseIdAndStatusOrderByIdAsc(courseId, WAITING)
            .ifPresent(enrollment -> {
                enrollment.promoteToPending();
                // PENDING 상태로 변경 시 해당 사용자에게 이메일 발송 및 푸시 알림
            });
    }

    private void verifyEnrollmentOwner(Enrollment enrollment, Long requestUserId) {
        if (!Objects.equals(enrollment.getStudent().getId(), requestUserId))
            throw new EnrollmentNotOwnerException();
    }

    private static void verifyCancellationPeriod(Enrollment enrollment) {
        LocalDateTime confirmedAt = enrollment.getConfirmedAt();
        LocalDate courseStartDate = enrollment.getCourse().getStartDate();

        LocalDateTime now = LocalDateTime.now();
        boolean withinConfirmedAt = !now.isAfter(confirmedAt.plusDays(CANCELLATION_PERIOD_DAYS));
        boolean beforeCourseStartDate = now.toLocalDate().isBefore(courseStartDate);

        if (!withinConfirmedAt || !beforeCourseStartDate)
            throw new EnrollmentCancellationPeriodExpiredException();
    }
}
