package leehs.course.core.course.application.command;

import java.time.LocalDate;

public record CourseCreateCommand(
    Long requestUserId,
    String title,
    String description,
    Integer price,
    Integer capacity,
    LocalDate startDate,
    LocalDate endDate
) {

}
