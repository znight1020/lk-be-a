package leehs.course.core.course.domain.model;

import java.util.Locale;

import org.springframework.util.StringUtils;

public enum CourseStatus {
    DRAFT, OPEN, CLOSED;

    public static CourseStatus from(String status) {
        if (!StringUtils.hasText(status))
            return null;

        return CourseStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }
}
