package leehs.course.core.user.domain.model;

import leehs.course.core.user.domain.exception.UserRoleInvalidException;

public enum UserRole {
    CREATOR, STUDENT;

    public static UserRole from(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new UserRoleInvalidException();
        }
    }
}
