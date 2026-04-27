package leehs.course.core.user.application;

import leehs.course.core.user.domain.model.User;

public interface UserFinder {

    User find(Long userId);
}
