package leehs.course.core.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import leehs.course.core.user.application.command.UserRegisterCommand;
import leehs.course.core.user.domain.exception.EmailDuplicatedException;
import leehs.course.core.user.domain.exception.UserRoleInvalidException;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.model.UserRole;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
class UserRegisterTest {

    @Autowired
    UserRegister userRegister;

    @Test
    void whenRegisterUser_expectUserCreated() {
        User student = userRegister.register(UserFixture.createStudentRegisterCommand("student@test.com"));
        assertThat(student.getId()).isNotNull();
        assertThat(student.getRole()).isEqualTo(UserRole.STUDENT);

        User creator = userRegister.register(UserFixture.createCreatorRegisterCommand("creator@test.com"));
        assertThat(creator.getId()).isNotNull();
        assertThat(creator.getRole()).isEqualTo(UserRole.CREATOR);
    }

    @Test
    void whenRegisterUserWithDuplicateEmail_expectEmailDuplicatedException() {
        var duplicatedEmailCommand = UserFixture.createStudentRegisterCommand("test@test.com");
        userRegister.register(duplicatedEmailCommand);

        assertThatThrownBy(() -> userRegister.register(duplicatedEmailCommand))
            .isInstanceOf(EmailDuplicatedException.class);
    }

    @Test
    void whenRegisterUserWithInvalidRole_expectUserRoleInvalidException() {
        var invalidRoleCommand = new UserRegisterCommand("test@test.com", "수강생", "INVALID");

        assertThatThrownBy(() -> userRegister.register(invalidRoleCommand))
            .isInstanceOf(UserRoleInvalidException.class);
    }
}
