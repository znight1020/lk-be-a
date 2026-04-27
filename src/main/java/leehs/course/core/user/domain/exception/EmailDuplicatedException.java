package leehs.course.core.user.domain.exception;

import leehs.course.core.user.domain.exception.error.UserError;
import leehs.course.global.exception.ApplicationException;

public class EmailDuplicatedException extends ApplicationException {

    public EmailDuplicatedException() {
        super(
            UserError.EMAIL_DUPLICATE.name(),
            UserError.EMAIL_DUPLICATE.getStatus(),
            UserError.EMAIL_DUPLICATE.getMessage()
        );
    }
}
