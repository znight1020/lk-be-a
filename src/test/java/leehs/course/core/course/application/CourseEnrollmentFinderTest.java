package leehs.course.core.course.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.course.application.query.CourseEnrollmentFindQuery;
import leehs.course.core.course.application.result.CourseEnrollmentSummaryResult;
import leehs.course.core.course.domain.exception.CourseManagementForbiddenException;
import leehs.course.core.course.domain.exception.CourseNotOwnerException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.EnrollmentFixture;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class CourseEnrollmentFinderTest {

    @Autowired
    CourseEnrollmentFinder courseEnrollmentFinder;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("강의별 수강생 목록 조회 - 성공")
    void whenFindAllWithOwnerCreator_expectConfirmedStudentsReturnedInDescOrder() {
        User owner = userRepository.save(UserFixture.createCreator("owner@test.com"));
        User anotherCreator = userRepository.save(UserFixture.createCreator("another@test.com"));
        User firstStudent = userRepository.save(UserFixture.createStudent("first@test.com"));
        User secondStudent = userRepository.save(UserFixture.createStudent("second@test.com"));
        User pendingStudent = userRepository.save(UserFixture.createStudent("pending@test.com"));
        User cancelledStudent = userRepository.save(UserFixture.createStudent("cancelled@test.com"));
        User otherCourseStudent = userRepository.save(UserFixture.createStudent("other@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(owner));
        Course otherCourse = courseRepository.save(CourseFixture.createOpenCourse(anotherCreator));

        Enrollment firstEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, firstStudent));
        firstEnrollment.confirm();

        Enrollment secondEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, secondStudent));
        secondEnrollment.confirm();

        enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, pendingStudent));

        Enrollment cancelledEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, cancelledStudent));
        cancelledEnrollment.cancel();

        Enrollment otherCourseEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(otherCourse, otherCourseStudent));
        otherCourseEnrollment.confirm();

        em.flush();
        em.clear();

        List<CourseEnrollmentSummaryResult> results =
            courseEnrollmentFinder.findAll(new CourseEnrollmentFindQuery(owner.getId(), course.getId()));

        assertThat(results).hasSize(2);
        assertThat(results).extracting(CourseEnrollmentSummaryResult::enrollmentId)
            .containsExactly(secondEnrollment.getId(), firstEnrollment.getId());
        assertThat(results).extracting(CourseEnrollmentSummaryResult::studentId)
            .containsExactly(secondStudent.getId(), firstStudent.getId());
        assertThat(results).extracting(CourseEnrollmentSummaryResult::status)
            .containsExactly("CONFIRMED", "CONFIRMED");
    }

    @Test
    @DisplayName("강의별 수강생 목록 조회 - 실패, 학생 권한")
    void whenFindAllWithStudent_expectCourseManagementForbiddenException() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User student = userRepository.save(UserFixture.createStudent("student@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));

        assertThatThrownBy(() -> courseEnrollmentFinder.findAll(new CourseEnrollmentFindQuery(student.getId(), course.getId())))
            .isInstanceOf(CourseManagementForbiddenException.class);
    }

    @Test
    @DisplayName("강의별 수강생 목록 조회 - 실패, 다른 강사의 강의")
    void whenFindAllWithNonOwnerCreator_expectCourseNotOwnerException() {
        User owner = userRepository.save(UserFixture.createCreator("owner@test.com"));
        User anotherCreator = userRepository.save(UserFixture.createCreator("another@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(owner));

        assertThatThrownBy(() -> courseEnrollmentFinder.findAll(new CourseEnrollmentFindQuery(anotherCreator.getId(), course.getId())))
            .isInstanceOf(CourseNotOwnerException.class);
    }
}
