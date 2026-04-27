package leehs.course.core.user.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    String email,

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 1, max = 100, message = "이름은 1자 이상 100자 이하여야 합니다")
    String name,

    @NotBlank(message = "역할은 필수입니다")
    @Pattern(regexp = "CREATOR|STUDENT", message = "역할은 CREATOR 또는 STUDENT 여야 합니다")
    String role
) {

}
