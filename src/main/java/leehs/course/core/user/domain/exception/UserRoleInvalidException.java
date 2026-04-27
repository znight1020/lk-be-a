package leehs.course.core.user.domain.exception;

import leehs.course.core.user.domain.exception.error.UserError;
import leehs.course.global.exception.ApplicationException;

public class UserRoleInvalidException extends ApplicationException {

    public UserRoleInvalidException() {
        super(
            UserError.USER_ROLE_INVALID.name(),
            UserError.USER_ROLE_INVALID.getStatus(),
            UserError.USER_ROLE_INVALID.getMessage()
        );
    }
}
