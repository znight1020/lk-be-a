package leehs.course.core.enrollment.application.service;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;
import static leehs.course.core.user.domain.model.UserRole.STUDENT;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.application.CourseLockFinder;
import leehs.course.core.course.domain.exception.CourseStatusNotOpenException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.enrollment.application.EnrollmentApplier;
import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.domain.exception.EnrollmentAlreadyExistsException;
import leehs.course.core.enrollment.domain.exception.EnrollmentCapacityExceededException;
import leehs.course.core.enrollment.domain.exception.EnrollmentForbiddenException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.model.EnrollmentStatus;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.application.UserFinder;
import leehs.course.core.user.domain.model.User;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentCommandService implements EnrollmentApplier {

    private final EnrollmentRepository enrollmentRepository;

    private final CourseLockFinder courseLockFinder;

    private final UserFinder userFinder;

    @Override
    public Enrollment apply(EnrollmentApplyCommand command) {
        User requestUser = userFinder.find(command.requestUserId());
        verifyStudentRole(requestUser);

        Course course = courseLockFinder.findWithLock(command.courseId());
        verifyOpenCourse(course);

        List<EnrollmentStatus> activeStatuses = List.of(PENDING, CONFIRMED);
        verifyNoActiveEnrollment(course.getId(), requestUser.getId(), activeStatuses);
        verifyCapacity(course.getId(), course.getCapacity(), activeStatuses);

        Enrollment enrollment = Enrollment.apply(course, requestUser);

        return enrollmentRepository.save(enrollment);
    }

    private void verifyStudentRole(User requestUser) {
        if (requestUser.getRole() != STUDENT)
            throw new EnrollmentForbiddenException();
    }

    private static void verifyOpenCourse(Course course) {
        if (!course.isOpen())
            throw new CourseStatusNotOpenException();
    }

    private void verifyNoActiveEnrollment(Long courseId, Long studentId, List<EnrollmentStatus> activeStatuses) {
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatusIn(courseId, studentId, activeStatuses))
            throw new EnrollmentAlreadyExistsException();
    }

    private void verifyCapacity(Long courseId, Integer capacity, List<EnrollmentStatus> activeStatuses) {
        long activeEnrollmentCount = enrollmentRepository.countByCourseIdAndStatusIn(courseId, activeStatuses);

        if (activeEnrollmentCount >= capacity)
            throw new EnrollmentCapacityExceededException();
    }
}
