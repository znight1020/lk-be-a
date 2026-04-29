package leehs.course.core.enrollment.domain.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.model.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countByCourseIdAndStatusIn(Long courseId, Collection<EnrollmentStatus> statuses);

    boolean existsByCourseIdAndStudentIdAndStatusIn(
        Long courseId,
        Long studentId,
        Collection<EnrollmentStatus> statuses
    );

    Optional<Enrollment> findFirstByCourseIdAndStatusOrderByIdAsc(Long courseId, EnrollmentStatus status);

    @EntityGraph(attributePaths = "course")
    Page<Enrollment> findAllByStudentId(Long studentId, Pageable pageable);
}
