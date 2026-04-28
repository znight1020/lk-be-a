package leehs.course.core.course.api.response;

import java.time.LocalDate;

import leehs.course.core.course.domain.model.Course;

public record CourseSummaryResponse(
    Long courseId,
    Long creatorId,
    String creatorName,
    String title,
    Integer price,
    Integer capacity,
    LocalDate startDate,
    LocalDate endDate,
    String status
) {

    public static CourseSummaryResponse of(Course course) {
        return new CourseSummaryResponse(
            course.getId(),
            course.getCreator().getId(),
            course.getCreator().getName(),
            course.getTitle(),
            course.getPrice(),
            course.getCapacity(),
            course.getStartDate(),
            course.getEndDate(),
            course.getStatus().name()
        );
    }
}
