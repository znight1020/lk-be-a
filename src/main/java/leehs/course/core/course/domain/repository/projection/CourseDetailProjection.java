package leehs.course.core.course.domain.repository.projection;

import java.time.LocalDate;

import leehs.course.core.course.domain.model.CourseStatus;

public interface CourseDetailProjection {

    Long getId();

    Long getCreatorId();

    String getCreatorName();

    String getTitle();

    String getDescription();

    Integer getPrice();

    Integer getCapacity();

    LocalDate getStartDate();

    LocalDate getEndDate();

    CourseStatus getStatus();

    Long getCurrentEnrollmentCount();
}
