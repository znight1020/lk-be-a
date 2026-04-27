package leehs.course.core.user.api.response;

import leehs.course.core.user.domain.model.User;

public record UserRegisterResponse(Long userId, String email) {

    static public UserRegisterResponse of(User user) {
        return new UserRegisterResponse(user.getId(), user.getEmail().address());
    }
}
