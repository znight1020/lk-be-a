package leehs.course.core.course.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.api.request.CourseCreateRequest;
import leehs.course.core.course.api.response.CourseCreateResponse;
import leehs.course.core.course.application.CourseCreator;
import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.domain.model.Course;
import leehs.course.global.web.RequestUserId;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseApi {

    private final CourseCreator courseCreator;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseCreateResponse createCourse(
        @RequestUserId Long userId,
        @Valid @RequestBody CourseCreateRequest request
    ) {
        CourseCreateCommand command = new CourseCreateCommand(userId,
            request.title(), request.description(), request.price(), request.capacity(),
            request.startDate(), request.endDate()
        );

        Course course = courseCreator.create(command);

        return CourseCreateResponse.of(course);
    }
}
