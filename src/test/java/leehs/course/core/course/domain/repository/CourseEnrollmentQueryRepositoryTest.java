package leehs.course.core.course.domain.repository;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.projection.CourseEnrollmentSummaryProjection;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.EnrollmentFixture;
import leehs.course.fixture.UserFixture;

@DataJpaTest
class CourseEnrollmentQueryRepositoryTest {

    @Autowired
    CourseEnrollmentQueryRepository courseEnrollmentQueryRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("강의별 수강생 목록 조회 - 성공, 확정된 신청의 학생만 최신순 반환")
    void whenFindStudentSummariesByCourseId_expectConfirmedStudentsReturned() {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User firstStudent = userRepository.save(UserFixture.createStudent("first@test.com"));
        User secondStudent = userRepository.save(UserFixture.createStudent("second@test.com"));
        User pendingStudent = userRepository.save(UserFixture.createStudent("pending@test.com"));
        User cancelledStudent = userRepository.save(UserFixture.createStudent("cancelled@test.com"));
        User otherCourseStudent = userRepository.save(UserFixture.createStudent("other@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator));
        Course otherCourse = courseRepository.save(CourseFixture.createOpenCourse(creator));

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

        List<CourseEnrollmentSummaryProjection> students =
            courseEnrollmentQueryRepository.findStudentSummariesByCourseId(course.getId());

        assertThat(students).hasSize(2);
        assertThat(students).extracting(CourseEnrollmentSummaryProjection::getEnrollmentId)
            .containsExactly(secondEnrollment.getId(), firstEnrollment.getId());
        assertThat(students).extracting(CourseEnrollmentSummaryProjection::getStudentId)
            .containsExactly(secondStudent.getId(), firstStudent.getId());
        assertThat(students).extracting(CourseEnrollmentSummaryProjection::getStudentName)
            .containsExactly(secondStudent.getName(), firstStudent.getName());
        assertThat(students).extracting(CourseEnrollmentSummaryProjection::getStudentEmail)
            .containsExactly(secondStudent.getEmail().address(), firstStudent.getEmail().address());
        assertThat(students).extracting(projection -> projection.getStatus().name())
            .containsExactly("CONFIRMED", "CONFIRMED");
    }
}
