package leehs.course.core.course.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import leehs.course.core.course.application.query.CourseFindQuery;
import leehs.course.core.course.application.result.CourseDetailResult;
import leehs.course.core.course.domain.exception.CourseNotFoundException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
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

    @Test
    void whenFindAllWithNullStatus_expectAllCoursesReturnedInDescOrder() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course firstCourse = courseRepository.save(CourseFixture.createCourse(creator));
        Course secondCourse = courseRepository.save(CourseFixture.createCourse(creator));

        em.flush();
        em.clear();

        List<Course> courses = courseFinder.findAll(new CourseFindQuery(null));
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getId)
            .containsExactly(secondCourse.getId(), firstCourse.getId());
    }

    @Test
    void whenFindAllWithStatus_expectOnlyMatchingCoursesReturned() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course draftCourse = courseRepository.save(CourseFixture.createCourse(creator));
        Course openCourse = courseRepository.save(CourseFixture.createCourse(creator));
        openCourse.open();

        em.flush();
        em.clear();

        List<Course> courses = courseFinder.findAll(new CourseFindQuery(CourseStatus.OPEN));
        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getId()).isEqualTo(openCourse.getId());
        assertThat(courses.get(0).getStatus()).isEqualTo(CourseStatus.OPEN);
        assertThat(courses).extracting(Course::getId).doesNotContain(draftCourse.getId());
    }

    @Test
    void whenFindDetailWithExistingCourse_expectCourseDetailReturned() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        em.flush();
        em.clear();

        CourseDetailResult result = courseFinder.findDetail(course.getId());
        assertThat(result.id()).isEqualTo(course.getId());
        assertThat(result.creatorId()).isEqualTo(creator.getId());
        assertThat(result.title()).isEqualTo(course.getTitle());
        assertThat(result.description()).isEqualTo(course.getDescription());
        assertThat(result.price()).isEqualTo(course.getPrice());
        assertThat(result.capacity()).isEqualTo(course.getCapacity());
        assertThat(result.startDate()).isEqualTo(course.getStartDate());
        assertThat(result.endDate()).isEqualTo(course.getEndDate());
        assertThat(result.status()).isEqualTo(course.getStatus());
        assertThat(result.currentEnrollmentCount()).isEqualTo(0L);
    }

    @Test
    void whenFindDetailWithNonExistingCourse_expectCourseNotFoundException() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> courseFinder.findDetail(nonExistentId))
            .isInstanceOf(CourseNotFoundException.class);
    }
}
