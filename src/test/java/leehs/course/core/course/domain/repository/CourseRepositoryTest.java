package leehs.course.core.course.domain.repository;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CANCELLED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.Test;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.core.course.domain.repository.projection.CourseDetailProjection;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.model.EnrollmentStatus;
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

    @Test
    void whenFindDetailById_expectCourseDetailProjectionReturned() {
        User creator = userRepository.save(UserFixture.createCreator("creator-detail@test.com"));
        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        entityManager.flush();
        entityManager.clear();

        CourseDetailProjection projection = courseRepository.findDetailById(
            course.getId(),
            List.of(PENDING, CONFIRMED)
        ).orElseThrow();

        assertThat(projection.getId()).isEqualTo(course.getId());
        assertThat(projection.getCreatorId()).isEqualTo(creator.getId());
        assertThat(projection.getCreatorName()).isEqualTo(creator.getName());
        assertThat(projection.getTitle()).isEqualTo(course.getTitle());
        assertThat(projection.getDescription()).isEqualTo(course.getDescription());
        assertThat(projection.getPrice()).isEqualTo(course.getPrice());
        assertThat(projection.getCapacity()).isEqualTo(course.getCapacity());
        assertThat(projection.getStartDate()).isEqualTo(course.getStartDate());
        assertThat(projection.getEndDate()).isEqualTo(course.getEndDate());
        assertThat(projection.getStatus()).isEqualTo(course.getStatus());
        assertThat(projection.getCurrentEnrollmentCount()).isEqualTo(0L);
    }

    @Test
    void whenFindDetailById_expectOnlyPendingAndConfirmedEnrollmentsCounted() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User pendingStudent = userRepository.save(UserFixture.createStudent("pending@test.com"));
        User confirmedStudent = userRepository.save(UserFixture.createStudent("confirmed@test.com"));
        User cancelledStudent = userRepository.save(UserFixture.createStudent("cancelled@test.com"));

        Course course = courseRepository.save(CourseFixture.createCourse(creator));

        // TODO : EnrollmentRepository 구현 시 삭제
        entityManager.persist(createEnrollment(course, pendingStudent, PENDING));
        entityManager.persist(createEnrollment(course, confirmedStudent, CONFIRMED));
        entityManager.persist(createEnrollment(course, cancelledStudent, CANCELLED));

        entityManager.flush();
        entityManager.clear();

        CourseDetailProjection projection = courseRepository.findDetailById(course.getId(), List.of(PENDING, CONFIRMED))
            .orElseThrow();

        assertThat(projection.getCurrentEnrollmentCount()).isEqualTo(2L);
    }

    // TODO : Enrollment 구현 시 삭제
    private Enrollment createEnrollment(Course course, User student, EnrollmentStatus status) {
        Enrollment enrollment = BeanUtils.instantiateClass(Enrollment.class);
        ReflectionTestUtils.setField(enrollment, "course", course);
        ReflectionTestUtils.setField(enrollment, "student", student);
        ReflectionTestUtils.setField(enrollment, "status", status);
        return enrollment;
    }
}
