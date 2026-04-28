package leehs.course.core.course.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.UserFixture;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    void whenFindAllByOrderByIdDesc_expectCreatorLoaded() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        courseRepository.save(CourseFixture.createCourse(creator));
        courseRepository.save(CourseFixture.createCourse(creator));

        entityManager.flush();
        entityManager.clear();

        List<Course> courses = courseRepository.findAllByOrderByIdDesc();
        assertThat(courses).isNotEmpty();
        assertThat(courses)
            .allSatisfy(course -> assertThat(entityManagerFactory.getPersistenceUnitUtil()
                .isLoaded(course, "creator")).isTrue());
    }

    @Test
    void whenFindAllByStatusOrderByIdDesc_expectCreatorLoaded() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));

        Course draftCourse = courseRepository.save(CourseFixture.createCourse(creator));
        Course openCourse = courseRepository.save(CourseFixture.createCourse(creator));
        openCourse.open();

        entityManager.flush();
        entityManager.clear();

        List<Course> courses = courseRepository.findAllByStatusOrderByIdDesc(CourseStatus.OPEN);
        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getId()).isEqualTo(openCourse.getId());
        assertThat(courses.get(0).getStatus()).isEqualTo(CourseStatus.OPEN);
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(courses.get(0), "creator")).isTrue();
        assertThat(courses).extracting(Course::getId).doesNotContain(draftCourse.getId());
    }
}
