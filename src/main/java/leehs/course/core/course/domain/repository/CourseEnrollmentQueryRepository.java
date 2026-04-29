package leehs.course.core.course.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import leehs.course.core.course.domain.repository.projection.CourseEnrollmentSummaryProjection;
import leehs.course.core.enrollment.domain.model.Enrollment;

public interface CourseEnrollmentQueryRepository extends JpaRepository<Enrollment, Long> {

    @Query("""
        SELECT
            e.id                AS enrollmentId,
            s.id                AS studentId,
            s.name              AS studentName,
            s.email.address     AS studentEmail,
            e.status            AS status,
            e.createdAt         AS createdAt,
            e.confirmedAt       AS confirmedAt,
            e.cancelledAt       AS cancelledAt
          FROM Enrollment e
          JOIN e.student s
         WHERE e.course.id = :courseId
           AND e.status = leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED
         ORDER BY e.id DESC
        """
    )
    List<CourseEnrollmentSummaryProjection> findStudentSummariesByCourseId(@Param("courseId") Long courseId);
}
