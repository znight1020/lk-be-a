package leehs.course.core.course.application.result;

import java.time.LocalDate;

import leehs.course.core.course.domain.model.CourseStatus;
import leehs.course.core.course.domain.repository.projection.CourseDetailProjection;

public record CourseDetailResult(
    Long id,
    Long creatorId,
    String creatorName,
    String title,
    String description,
    Integer price,
    Integer capacity,
    LocalDate startDate,
    LocalDate endDate,
    CourseStatus status,
    Long currentEnrollmentCount
) {

    public static CourseDetailResult of(CourseDetailProjection projection) {
        return new CourseDetailResult(
            projection.getId(),
            projection.getCreatorId(),
            projection.getCreatorName(),
            projection.getTitle(),
            projection.getDescription(),
            projection.getPrice(),
            projection.getCapacity(),
            projection.getStartDate(),
            projection.getEndDate(),
            projection.getStatus(),
            projection.getCurrentEnrollmentCount()
        );
    }
}
