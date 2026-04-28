package leehs.course.core.course.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.application.CourseFinder;
import leehs.course.core.course.application.query.CourseFindQuery;
import leehs.course.core.course.domain.exception.CourseNotFoundException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseQueryService implements CourseFinder {

    private final CourseRepository courseRepository;

    @Override
    public Course find(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(CourseNotFoundException::new);
    }

    @Override
    public List<Course> findAll(CourseFindQuery query) {
        if (query.status() == null)
            return courseRepository.findAllByOrderByIdDesc();

        return courseRepository.findAllByStatusOrderByIdDesc(query.status());
    }
}
