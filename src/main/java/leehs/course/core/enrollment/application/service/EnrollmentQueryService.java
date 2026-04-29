package leehs.course.core.enrollment.application.service;

import static leehs.course.core.user.domain.model.UserRole.STUDENT;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.enrollment.application.EnrollmentFinder;
import leehs.course.core.enrollment.application.query.EnrollmentFindQuery;
import leehs.course.core.enrollment.domain.exception.EnrollmentForbiddenException;
import leehs.course.core.enrollment.domain.exception.EnrollmentNotFoundException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.application.UserFinder;
import leehs.course.core.user.domain.model.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnrollmentQueryService implements EnrollmentFinder {

    private final EnrollmentRepository enrollmentRepository;

    private final UserFinder userFinder;

    @Override
    public Enrollment find(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
            .orElseThrow(EnrollmentNotFoundException::new);
    }

    @Override
    public Page<Enrollment> findAll(EnrollmentFindQuery query) {
        User user = userFinder.find(query.userId());
        verifyStudentRole(user);

        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "id"));

        return enrollmentRepository.findAllByStudentId(query.userId(), pageRequest);
    }

    private void verifyStudentRole(User user) {
        if (user.getRole() != STUDENT)
            throw new EnrollmentForbiddenException();
    }
}
