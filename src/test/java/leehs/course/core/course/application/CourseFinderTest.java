package leehs.course.core.course.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import leehs.course.core.course.domain.exception.CourseNotFoundException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class CourseFinderTest {

    @Autowired
    CourseFinder courseFinder;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void whenFindExistingCourseById_expectCourseReturned() {
        User creator = UserFixture.createCreator("creator@test.com");
        userRepository.save(creator);

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        em.flush();
        em.clear();

        Course found = courseFinder.find(course.getId());
        assertNotNull(found);
        assertThat(found.getId()).isEqualTo(course.getId());
    }

    @Test
    void whenFindNonExistingCourseById_expectCourseNotFoundException() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> courseFinder.find(nonExistentId))
            .isInstanceOf(CourseNotFoundException.class);
    }
}