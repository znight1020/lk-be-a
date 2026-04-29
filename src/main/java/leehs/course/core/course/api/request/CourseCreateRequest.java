package leehs.course.core.course.api.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
    @NotBlank(message = "강의 제목은 필수입니다")
    @Size(max = 200, message = "강의 제목은 200자 이하여야 합니다")
    String title,

    @NotBlank(message = "강의 설명은 필수입니다")
    String description,

    @NotNull(message = "강의 가격은 필수입니다")
    @Min(value = 0, message = "강의 가격은 0 이상이어야 합니다")
    Integer price,

    @NotNull(message = "강의 정원은 필수입니다")
    @Min(value = 1, message = "강의 정원은 1 이상이어야 합니다")
    Integer capacity,

    @NotNull(message = "강의 시작일은 필수입니다")
    @FutureOrPresent(message = "강의 시작일은 오늘 이후여야 합니다")
    LocalDate startDate,

    @NotNull(message = "강의 종료일은 필수입니다")
    LocalDate endDate
) {
}
