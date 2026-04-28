package leehs.course.core.enrollment.domain.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import leehs.course.core.AbstractEntity;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.enrollment.domain.exception.EnrollmentStatusAlreadyCancelledException;
import leehs.course.core.enrollment.domain.exception.EnrollmentStatusNotPendingException;
import leehs.course.core.user.domain.model.User;

@Getter
@Entity
@Table(name = "enrollments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Enrollment apply(Course course, User student) {
        Enrollment enrollment = new Enrollment();
        enrollment.course = requireNonNull(course, "course must not be null");
        enrollment.student = requireNonNull(student, "student must not be null");
        enrollment.status = EnrollmentStatus.PENDING;

        return enrollment;
    }

    public void confirm() {
        if (!isPending())
            throw new EnrollmentStatusNotPendingException();

        this.status = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (isCancelled())
            throw new EnrollmentStatusAlreadyCancelledException();

        this.status = EnrollmentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == EnrollmentStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == EnrollmentStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return status == EnrollmentStatus.CANCELLED;
    }

    public boolean isActive() {
        return isPending() || isConfirmed();
    }
}
