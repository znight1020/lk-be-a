package leehs.course.core.user.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Email(@Column(name = "email_address", length = 150, nullable = false) String address) {

}
