package leehs.course.core.course.api.response;

import java.time.LocalDate;

import leehs.course.core.course.application.result.CourseDetailResult;

public record CourseDetailResponse(
    Long courseId,
    Long creatorId,
    String creatorName,
    String title,
    String description,
    Integer price,
    Integer capacity,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    Long currentEnrollmentCount
) {

    public static CourseDetailResponse of(CourseDetailResult result) {
        return new CourseDetailResponse(
            result.id(),
            result.creatorId(),
            result.creatorName(),
            result.title(),
            result.description(),
            result.price(),
            result.capacity(),
            result.startDate(),
            result.endDate(),
            result.status().name(),
            result.currentEnrollmentCount()
        );
    }
}
