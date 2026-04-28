package leehs.course.core.course.domain.model;

import java.time.LocalDate;
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

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import leehs.course.core.AbstractEntity;
import leehs.course.core.course.domain.exception.CourseStatusNotDraftException;
import leehs.course.core.course.domain.exception.CourseStatusNotOpenException;
import leehs.course.core.user.domain.model.User;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private User creator;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Course create(User creator, String title, String description, Integer price, Integer capacity,
        LocalDate startDate, LocalDate endDate
    ) {
        Assert.notNull(creator, "creator must not be null");

        Assert.hasText(title, "title must not be blank");

        Assert.notNull(price, "price must not be null");
        Assert.isTrue(price >= 0, "price must be greater than or equal to 0");

        Assert.notNull(capacity, "capacity must not be null");
        Assert.isTrue(capacity >= 1, "capacity must be greater than or equal to 1");

        Assert.notNull(startDate, "startDate must not be null");
        Assert.notNull(endDate, "endDate must not be null");
        Assert.isTrue(!startDate.isAfter(endDate), "startDate must not be after endDate");

        Course course = new Course();
        course.creator = creator;
        course.title = title;
        course.description = description;
        course.price = price;
        course.capacity = capacity;
        course.startDate = startDate;
        course.endDate = endDate;
        course.status = CourseStatus.DRAFT;

        return course;
    }

    public void open() {
        if (!isDraft())
            throw new CourseStatusNotDraftException();

        this.status = CourseStatus.OPEN;
    }

    public void close() {
        if (!isOpen())
            throw new CourseStatusNotOpenException();

        this.status = CourseStatus.CLOSED;
    }

    public boolean isDraft() {
        return status == CourseStatus.DRAFT;
    }

    public boolean isOpen() {
        return status == CourseStatus.OPEN;
    }

    public boolean isClosed() {
        return status == CourseStatus.CLOSED;
    }
}
