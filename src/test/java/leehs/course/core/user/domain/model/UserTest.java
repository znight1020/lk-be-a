package leehs.course.core.user.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import leehs.course.core.user.domain.exception.EmailFormatInvalidException;
import leehs.course.core.user.domain.exception.UserRoleInvalidException;

class UserTest {

    @Test
    void whenRegisterUserWithInvalidEmail_expectEmailFormatInvalidException() {
        assertThatThrownBy(() -> User.register("invalid.com", "수강생", "STUDENT"))
            .isInstanceOf(EmailFormatInvalidException.class);
    }

    @Test
    void whenRegisterUserWithInvalidRole_expectUserRoleInvalidException() {
        assertThatThrownBy(() -> User.register("test@test.com", "수강생", "INVALID"))
            .isInstanceOf(UserRoleInvalidException.class);
    }
}
