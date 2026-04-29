package leehs.course.core.enrollment.domain.repository;

import java.util.Collection;
import java.util.List;

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

    @EntityGraph(attributePaths = "course")
    List<Enrollment> findAllByStudentIdOrderByIdDesc(Long studentId);
}
