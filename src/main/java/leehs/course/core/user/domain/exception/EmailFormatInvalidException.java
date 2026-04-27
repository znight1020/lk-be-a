package leehs.course.core.user.domain.exception;

import leehs.course.core.user.domain.exception.error.UserError;
import leehs.course.global.exception.ApplicationException;

public class EmailFormatInvalidException extends ApplicationException {

    public EmailFormatInvalidException() {
        super(
            UserError.EMAIL_FORMAT_INVALID.name(),
            UserError.EMAIL_FORMAT_INVALID.getStatus(),
            UserError.EMAIL_FORMAT_INVALID.getMessage()
        );
    }
}
