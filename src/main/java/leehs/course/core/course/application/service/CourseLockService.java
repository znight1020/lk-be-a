package leehs.course.core.course.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.application.CourseLockFinder;
import leehs.course.core.course.domain.exception.CourseNotFoundException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseLockService implements CourseLockFinder {

    private final CourseRepository courseRepository;

    @Override
    public Course findWithLock(Long courseId) {
        return courseRepository.findByIdWithLock(courseId)
            .orElseThrow(CourseNotFoundException::new);
    }
}
