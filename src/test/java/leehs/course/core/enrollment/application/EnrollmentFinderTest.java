package leehs.course.core.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.domain.exception.EnrollmentNotFoundException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.EnrollmentFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class EnrollmentFinderTest {

    @Autowired
    EnrollmentFinder enrollmentFinder;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    void whenFindExistingEnrollmentById_expectEnrollmentReturned() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        Enrollment enrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        em.flush();
        em.clear();

        Enrollment found = enrollmentFinder.find(enrollment.getId());
        assertNotNull(found);
        assertThat(found.getId()).isEqualTo(enrollment.getId());
        assertThat(found.getCourse().getId()).isEqualTo(course.getId());
        assertThat(found.getStudent().getId()).isEqualTo(student.getId());
    }

    @Test
    void whenFindNonExistingEnrollmentById_expectEnrollmentNotFoundException() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> enrollmentFinder.find(nonExistentId))
            .isInstanceOf(EnrollmentNotFoundException.class);
    }
}
