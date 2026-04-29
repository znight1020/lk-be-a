package leehs.course.core.user.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import leehs.course.core.user.api.request.UserRegisterRequest;
import leehs.course.core.user.api.response.UserRegisterResponse;
import leehs.course.core.user.application.UserRegister;
import leehs.course.core.user.application.command.UserRegisterCommand;
import leehs.course.core.user.domain.model.User;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserApi {

    private final UserRegister userRegister;

    @Operation(summary = "회원 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegisterResponse createUser(@Valid @RequestBody UserRegisterRequest request) {
        UserRegisterCommand command = new UserRegisterCommand(request.email(), request.name(), request.role());

        User user = userRegister.register(command);

        return UserRegisterResponse.of(user);
    }
}
