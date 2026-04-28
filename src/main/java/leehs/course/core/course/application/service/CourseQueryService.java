package leehs.course.core.course.application.service;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.application.CourseFinder;
import leehs.course.core.course.application.query.CourseFindQuery;
import leehs.course.core.course.application.result.CourseDetailResult;
import leehs.course.core.course.domain.exception.CourseNotFoundException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.course.domain.repository.projection.CourseDetailProjection;

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

    @Override
    public CourseDetailResult findDetail(Long courseId) {
        CourseDetailProjection projection = courseRepository.findDetailById(courseId, List.of(PENDING, CONFIRMED))
            .orElseThrow(CourseNotFoundException::new);

        return CourseDetailResult.of(projection);
    }
}
