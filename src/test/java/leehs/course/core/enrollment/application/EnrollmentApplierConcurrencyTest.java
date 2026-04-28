package leehs.course.core.enrollment.application;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.domain.exception.EnrollmentCapacityExceededException;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.UserFixture;

@SpringBootTest
class EnrollmentApplierConcurrencyTest {

    @Autowired
    EnrollmentApplier enrollmentApplier;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void tearDown() {
        enrollmentRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
        userRepository.deleteAll();
    }

    /**
     * 문제 가정:
     * 정원 2명인 강의에 1명이 수강 신청한 상태에서 10명의 학생이 마지막 1자리에 동시에 신청<p>
     * 예상 결과:
     * 1명만 수강 신청에 성공하고 9명은 {@code EnrollmentCapacityExceededException}이 발생<p>
     * 최종 active enrollment 수는 정원 2명을 넘지 않음
     */
    @Test
    void whenApplyEnrollmentConcurrentlyToLastSeat_expectOnlyOneEnrollmentCreated() throws InterruptedException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User existingStudent = userRepository.save(UserFixture.createStudent("existing@test.com"));
        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 2));

        enrollmentApplier.apply(new EnrollmentApplyCommand(existingStudent.getId(), course.getId()));

        List<User> applicants = IntStream.range(0, 10)
            .mapToObj(index -> userRepository.save(UserFixture.createStudent("student" + index + "@test.com")))
            .toList();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(applicants.size());
        AtomicInteger capacityExceededCount = new AtomicInteger();

        applicants.forEach(applicant -> {
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    enrollmentApplier.apply(new EnrollmentApplyCommand(applicant.getId(), course.getId()));
                } catch (EnrollmentCapacityExceededException ex) {
                    capacityExceededCount.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
            thread.start();
        });

        startLatch.countDown();

        assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(capacityExceededCount.get()).isEqualTo(9); // 10명 중 1명은 성공, 9명은 CapacityExceededException 발생
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(PENDING, CONFIRMED)))
            .isEqualTo(2L);
    }
}
