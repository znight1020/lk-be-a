package leehs.course.core.user.application;

import leehs.course.core.user.domain.model.User;

/**
 * 사용자 조회 기능을 제공
 */
public interface UserFinder {

    /**
     * 사용자 ID 기반 사용자 조회
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자
     */
    User find(Long userId);
}
