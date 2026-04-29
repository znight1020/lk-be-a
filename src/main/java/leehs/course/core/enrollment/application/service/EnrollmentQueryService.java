package leehs.course.core.enrollment.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.enrollment.application.EnrollmentFinder;
import leehs.course.core.enrollment.domain.exception.EnrollmentNotFoundException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnrollmentQueryService implements EnrollmentFinder {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Enrollment find(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
            .orElseThrow(EnrollmentNotFoundException::new);
    }
}
