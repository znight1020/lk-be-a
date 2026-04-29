package leehs.course.core.enrollment.domain.model;

import static leehs.course.fixture.CourseFixture.createCourse;
import static leehs.course.fixture.EnrollmentFixture.createEnrollment;
import static leehs.course.fixture.UserFixture.createStudent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import leehs.course.core.enrollment.domain.exception.EnrollmentStatusAlreadyCancelledException;
import leehs.course.core.enrollment.domain.exception.EnrollmentStatusNotPendingException;
import leehs.course.core.enrollment.domain.exception.EnrollmentStatusNotWaitingException;

class EnrollmentTest {

    @Test
    @DisplayName("수강 신청 생성 - 실패, 필수 값 누락")
    void whenApplyEnrollmentWithInvalidArgument_expectNullPointerException() {
        assertThatThrownBy(() -> Enrollment.apply(null, createStudent("student@test.com")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("course must not be null");

        assertThatThrownBy(() -> Enrollment.apply(createCourse(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("student must not be null");
    }

    @Test
    @DisplayName("수강 신청 생성 - 성공, 기본 상태는 PENDING")
    void whenApplyEnrollment_expectPendingStatus() {
        Enrollment enrollment = Enrollment.apply(createCourse(), createStudent("student@test.com"));

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(enrollment.isPending()).isTrue();
        assertThat(enrollment.isActive()).isTrue();
        assertThat(enrollment.getConfirmedAt()).isNull();
        assertThat(enrollment.getCancelledAt()).isNull();
    }

    @Test
    @DisplayName("대기열 등록 - 성공, 기본 상태는 WAITING")
    void whenCreateWaitlistEnrollment_expectWaitingStatus() {
        Enrollment enrollment = Enrollment.waitlist(createCourse(), createStudent("student@test.com"));

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.WAITING);
        assertThat(enrollment.isWaiting()).isTrue();
        assertThat(enrollment.isActive()).isFalse();
        assertThat(enrollment.getConfirmedAt()).isNull();
        assertThat(enrollment.getCancelledAt()).isNull();
    }

    @Test
    @DisplayName("수강 확정 - 성공, PENDING -> CONFIRMED")
    void whenConfirmPendingEnrollment_expectConfirmedStatus() {
        Enrollment enrollment = createEnrollment();

        enrollment.confirm();

        assertThat(enrollment.isConfirmed()).isTrue();
        assertThat(enrollment.isActive()).isTrue();
        assertThat(enrollment.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("수강 확정 - 실패, PENDING 상태 아님")
    void whenConfirmNonPendingEnrollment_expectEnrollmentStatusNotPendingException() {
        Enrollment waitingEnrollment = Enrollment.waitlist(createCourse(), createStudent("student@test.com"));

        assertThatThrownBy(waitingEnrollment::confirm)
            .isInstanceOf(EnrollmentStatusNotPendingException.class);

        Enrollment confirmedEnrollment = createEnrollment();
        confirmedEnrollment.confirm();

        assertThatThrownBy(confirmedEnrollment::confirm)
            .isInstanceOf(EnrollmentStatusNotPendingException.class);

        Enrollment cancelledEnrollment = createEnrollment();
        cancelledEnrollment.confirm();
        cancelledEnrollment.cancel();

        assertThatThrownBy(cancelledEnrollment::confirm)
            .isInstanceOf(EnrollmentStatusNotPendingException.class);
    }

    @Test
    @DisplayName("대기열 상태에서 대기 상태로 변경 - 성공, WAITING -> PENDING")
    void whenPromoteWaitingEnrollment_expectPendingStatus() {
        Enrollment enrollment = Enrollment.waitlist(createCourse(), createStudent("student@test.com"));

        enrollment.promoteToPending();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(enrollment.isPending()).isTrue();
        assertThat(enrollment.isActive()).isTrue();
    }

    @Test
    @DisplayName("대기열 상태에서 대기 상태로 변경 - 실패, WAITING 상태 아님")
    void whenPromoteNonWaitingEnrollment_expectEnrollmentStatusNotWaitingException() {
        Enrollment pendingEnrollment = createEnrollment();

        assertThatThrownBy(pendingEnrollment::promoteToPending)
            .isInstanceOf(EnrollmentStatusNotWaitingException.class);

        Enrollment cancelledEnrollment = Enrollment.waitlist(createCourse(), createStudent("student@test.com"));
        cancelledEnrollment.cancel();

        assertThatThrownBy(cancelledEnrollment::promoteToPending)
            .isInstanceOf(EnrollmentStatusNotWaitingException.class);
    }

    @Test
    @DisplayName("수강 취소 - 성공, PENDING -> CANCELLED")
    void whenCancelPendingEnrollment_expectCancelledStatus() {
        Enrollment enrollment = createEnrollment();

        enrollment.cancel();

        assertThat(enrollment.isCancelled()).isTrue();
        assertThat(enrollment.isActive()).isFalse();
        assertThat(enrollment.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("수강 취소 - 성공, CONFIRMED -> CANCELLED")
    void whenCancelConfirmedEnrollment_expectCancelledStatus() {
        Enrollment enrollment = createEnrollment();
        enrollment.confirm();

        enrollment.cancel();

        assertThat(enrollment.isCancelled()).isTrue();
        assertThat(enrollment.isActive()).isFalse();
        assertThat(enrollment.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("수강 취소 - 성공, WAITING -> CANCELLED")
    void whenCancelWaitingEnrollment_expectCancelledStatus() {
        Enrollment enrollment = Enrollment.waitlist(createCourse(), createStudent("student@test.com"));

        enrollment.cancel();

        assertThat(enrollment.isCancelled()).isTrue();
        assertThat(enrollment.isActive()).isFalse();
        assertThat(enrollment.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("수강 취소 - 실패, 이미 CANCELLED")
    void whenCancelCancelledEnrollment_expectEnrollmentStatusAlreadyCancelledException() {
        Enrollment enrollment = createEnrollment();
        enrollment.cancel();

        assertThatThrownBy(enrollment::cancel)
            .isInstanceOf(EnrollmentStatusAlreadyCancelledException.class);
    }
}
