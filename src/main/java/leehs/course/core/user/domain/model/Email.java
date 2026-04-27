package leehs.course.core.user.domain.model;

import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import leehs.course.core.user.domain.exception.EmailFormatInvalidException;

@Embeddable
public record Email(@Column(name = "email_address", length = 150, nullable = false) String address) {

    private static final Pattern EMAIL_PATTERN
        = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public Email {
        if (!EMAIL_PATTERN.matcher(address).matches())
            throw new EmailFormatInvalidException();
    }
}
