package leehs.course.core.user.application;

import leehs.course.core.user.application.command.UserRegisterCommand;
import leehs.course.core.user.domain.model.User;

public interface UserRegister {

    User register(UserRegisterCommand command);
}
