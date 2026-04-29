package leehs.course.core.enrollment.domain.repository;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.EnrollmentFixture;
import leehs.course.fixture.UserFixture;

@DataJpaTest
class EnrollmentRepositoryTest {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    @Test
    void whenCountByCourseIdAndStatusIn_expectOnlyMatchingStatusesCounted() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User pendingStudent = userRepository.save(UserFixture.createStudent("pending@test.com"));
        User confirmedStudent = userRepository.save(UserFixture.createStudent("confirmed@test.com"));
        User cancelledStudent = userRepository.save(UserFixture.createStudent("cancelled@test.com"));
        User otherCourseStudent = userRepository.save(UserFixture.createStudent("other@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        Course otherCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));

        enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, pendingStudent));

        Enrollment confirmedEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, confirmedStudent));
        confirmedEnrollment.confirm();

        Enrollment cancelledEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, cancelledStudent));
        cancelledEnrollment.cancel();

        enrollmentRepository.save(EnrollmentFixture.createEnrollment(otherCourse, otherCourseStudent));

        em.flush();
        em.clear();

        long count = enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(PENDING, CONFIRMED));

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void whenExistsByCourseIdAndStudentIdAndStatusInWithActiveEnrollment_expectTrue() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));

        em.flush();
        em.clear();

        boolean exists = enrollmentRepository.existsByCourseIdAndStudentIdAndStatusIn(
            course.getId(),
            student.getId(),
            List.of(PENDING, CONFIRMED)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByCourseIdAndStudentIdAndStatusInWithCancelledEnrollment_expectFalse() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment cancelledEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, student));
        cancelledEnrollment.cancel();

        em.flush();
        em.clear();

        boolean exists = enrollmentRepository.existsByCourseIdAndStudentIdAndStatusIn(
            course.getId(),
            student.getId(),
            List.of(PENDING, CONFIRMED)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void whenFindAllByStudentIdOrderByIdDesc_expectOwnEnrollmentsReturnedAndCourseLoaded() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));
        User anotherStudent = userRepository.save(UserFixture.createStudent("another@test.com"));

        Course firstCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));
        Course secondCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));

        Enrollment firstEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(firstCourse, student));
        Enrollment secondEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(secondCourse, student));
        enrollmentRepository.save(EnrollmentFixture.createEnrollment(secondCourse, anotherStudent));

        em.flush();
        em.clear();

        List<Enrollment> enrollments = enrollmentRepository.findAllByStudentIdOrderByIdDesc(student.getId());

        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).extracting(Enrollment::getId)
            .containsExactly(secondEnrollment.getId(), firstEnrollment.getId());
        assertThat(enrollments).extracting(enrollment -> enrollment.getStudent().getId())
            .containsOnly(student.getId());
        assertThat(enrollments)
            .allSatisfy(enrollment -> assertThat(emf.getPersistenceUnitUtil().isLoaded(enrollment, "course")).isTrue());
    }
}
