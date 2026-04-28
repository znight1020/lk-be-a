package leehs.course.core.course.domain.repository;

import org.springframework.data.repository.CrudRepository;

import leehs.course.core.course.domain.model.Course;

public interface CourseRepository extends CrudRepository<Course, Long> {

}
