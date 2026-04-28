package leehs.course.core.course.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = "creator")
    List<Course> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "creator")
    List<Course> findAllByStatusOrderByIdDesc(CourseStatus status);
}
