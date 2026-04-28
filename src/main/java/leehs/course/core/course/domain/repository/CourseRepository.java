package leehs.course.core.course.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.core.course.domain.repository.projection.CourseDetailProjection;
import leehs.course.core.enrollment.domain.model.EnrollmentStatus;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = "creator")
    List<Course> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "creator")
    List<Course> findAllByStatusOrderByIdDesc(CourseStatus status);

    @Query("""
        SELECT
            c.id            AS id,
            c.creator.id    AS creatorId,
            c.creator.name  AS creatorName,
            c.title         AS title,
            c.description   AS description,
            c.price         AS price,
            c.capacity      AS capacity,
            c.startDate     AS startDate,
            c.endDate       AS endDate,
            c.status        AS status,
            (
                SELECT COUNT(e.id)
                  FROM Enrollment e
                 WHERE e.course = c AND e.status IN :activeStatuses
            ) AS currentEnrollmentCount
         FROM Course c
        WHERE c.id = :id
        """
    )
    Optional<CourseDetailProjection> findDetailById(
        @Param("id") Long id,
        @Param("activeStatuses") Collection<EnrollmentStatus> activeStatuses
    );
}
