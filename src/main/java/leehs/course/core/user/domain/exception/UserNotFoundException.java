package leehs.course.core.user.domain.exception;

import leehs.course.core.user.domain.exception.error.UserError;
import leehs.course.global.exception.ApplicationException;

public class UserNotFoundException extends ApplicationException {

    public UserNotFoundException() {
        super(
            UserError.USER_NOT_FOUND.name(),
            UserError.USER_NOT_FOUND.getStatus(),
            UserError.USER_NOT_FOUND.getMessage()
        );
    }
}
