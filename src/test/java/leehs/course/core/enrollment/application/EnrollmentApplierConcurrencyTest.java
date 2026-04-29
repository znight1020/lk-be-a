package leehs.course.core.enrollment.application;

import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.CONFIRMED;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.PENDING;
import static leehs.course.core.enrollment.domain.model.EnrollmentStatus.WAITING;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseRepository;
import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.application.command.EnrollmentStatusModifyCommand;
import leehs.course.core.enrollment.domain.exception.EnrollmentCapacityExceededException;
import leehs.course.core.enrollment.domain.exception.EnrollmentWaitlistNotAvailableException;
import leehs.course.core.enrollment.domain.model.Enrollment;
import leehs.course.core.enrollment.domain.repository.EnrollmentRepository;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.CourseFixture;
import leehs.course.fixture.EnrollmentFixture;
import leehs.course.fixture.UserFixture;

@SpringBootTest
class EnrollmentApplierConcurrencyTest {

    @Autowired
    EnrollmentApplier enrollmentApplier;

    @Autowired
    EnrollmentWaitlistRegister enrollmentWaitlistRegister;

    @Autowired
    EnrollmentModifier enrollmentModifier;

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
    @DisplayName("수강 신청 동시성 - 성공, 마지막 자리는 한 명만 신청 성공")
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
        AtomicReference<Throwable> unexpectedFailure = new AtomicReference<>();

        applicants.forEach(applicant -> startConcurrentAction(
            startLatch,
            doneLatch,
            unexpectedFailure,
            EnrollmentCapacityExceededException.class,
            capacityExceededCount::incrementAndGet,
            () -> enrollmentApplier.apply(new EnrollmentApplyCommand(applicant.getId(), course.getId()))
        ));

        startLatch.countDown();

        assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(unexpectedFailure.get()).isNull();
        assertThat(capacityExceededCount.get()).isEqualTo(9); // 10명 중 1명은 성공, 9명은 CapacityExceededException 발생
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(PENDING, CONFIRMED))).isEqualTo(2L);
    }

    /**
     * 문제 가정:
     * 정원 1명인 강의에 이미 1명이 수강 신청한 상태에서 기존 신청자의 취소와 다른 학생의 대기열 등록이 동시에 발생<p>
     * 예상 결과:
     * 대기열 등록은 {@code WAITING}으로 남지 않고, 성공했다면 즉시 {@code PENDING}으로 승급되거나
     * 자리 여유가 먼저 생긴 경우 {@code EnrollmentWaitlistNotAvailableException}으로 거절됨<p>
     * 최종적으로 빈 자리가 남아 있는데 {@code WAITING} 상태가 남는 일은 없어야 함
     */
    @Test
    @DisplayName("수강 취소와 대기열 등록 동시성 - 성공, 빈 자리가 남은 채 WAITING 등록되지 않음")
    void whenCancelEnrollmentAndRegisterWaitlistConcurrently_expectNoWaitingEnrollmentLeftWithAvailableSeat()
        throws InterruptedException {
        User creator = userRepository.save(UserFixture.createCreator("creator@test.com"));
        User existingStudent = userRepository.save(UserFixture.createStudent("existing@test.com"));
        User applicant = userRepository.save(UserFixture.createStudent("applicant@test.com"));

        Course course = courseRepository.save(CourseFixture.createOpenCourse(creator, 1));

        Enrollment existingEnrollment = enrollmentRepository.save(EnrollmentFixture.createEnrollment(course, existingStudent));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger waitlistSuccessCount = new AtomicInteger();
        AtomicInteger waitlistRejectedCount = new AtomicInteger();
        AtomicReference<Throwable> unexpectedFailure = new AtomicReference<>();

        startConcurrentAction(
            startLatch,
            doneLatch,
            unexpectedFailure,
            null,
            null,
            () -> enrollmentModifier.cancel(
                existingEnrollment.getId(),
                new EnrollmentStatusModifyCommand(existingStudent.getId())
            )
        );

        startConcurrentAction(
            startLatch,
            doneLatch,
            unexpectedFailure,
            EnrollmentWaitlistNotAvailableException.class,
            waitlistRejectedCount::incrementAndGet,
            () -> {
                enrollmentWaitlistRegister.registerWaitlist(
                    new EnrollmentApplyCommand(applicant.getId(), course.getId()));
                waitlistSuccessCount.incrementAndGet();
            }
        );

        startLatch.countDown();

        assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(unexpectedFailure.get()).isNull();
        assertThat(waitlistSuccessCount.get() + waitlistRejectedCount.get()).isEqualTo(1);

        // 어떤 순서로 실행되더라도 최종적으로 WAITING은 남지 않아야 함
        Enrollment cancelledEnrollment = enrollmentRepository.findById(existingEnrollment.getId()).orElseThrow();
        assertThat(cancelledEnrollment.isCancelled()).isTrue();
        assertThat(enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(WAITING))).isEqualTo(0L);

        List<Enrollment> applicantEnrollments = enrollmentRepository.findAll().stream()
            .filter(enrollment -> enrollment.getCourse().getId().equals(course.getId()))
            .filter(enrollment -> enrollment.getStudent().getId().equals(applicant.getId()))
            .toList();

        // 대기열 등록이 먼저 성공했다면 취소 시 즉시 PENDING으로 변경되어야 함
        if (waitlistSuccessCount.get() == 1) {
            assertThat(applicantEnrollments).hasSize(1);
            assertThat(applicantEnrollments.get(0).getStatus()).isEqualTo(PENDING);
            assertThat(enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(PENDING, CONFIRMED)))
                .isEqualTo(1L);
        }

        // 취소가 먼저 끝났다면 대기열 등록은 거절되고 신청자 enrollment는 생성되지 않아야 함
        if (waitlistRejectedCount.get() == 1) {
            assertThat(applicantEnrollments).isEmpty();
            assertThat(enrollmentRepository.countByCourseIdAndStatusIn(course.getId(), List.of(PENDING, CONFIRMED)))
                .isEqualTo(0L);
        }
    }

    private <T extends Throwable> void startConcurrentAction(
        CountDownLatch startLatch,
        CountDownLatch doneLatch,
        AtomicReference<Throwable> unexpectedFailure,
        Class<T> expectedExceptionType,
        Runnable expectedExceptionHandler,
        ThrowingRunnable action
    ) {
        Thread thread = new Thread(() -> {
            try {
                startLatch.await();
                action.run();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                unexpectedFailure.compareAndSet(null, ex);
            } catch (Throwable ex) {
                if (expectedExceptionType != null && expectedExceptionType.isInstance(ex)) {
                    expectedExceptionHandler.run();
                } else {
                    unexpectedFailure.compareAndSet(null, ex);
                }
            } finally {
                doneLatch.countDown();
            }
        });
        thread.start();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {

        void run() throws Exception;
    }
}
