package leehs.course.core.user.application;

import leehs.course.core.user.application.command.UserRegisterCommand;
import leehs.course.core.user.domain.model.User;

/**
 * 사용자 등록 기능을 제공
 */
public interface UserRegister {

    /**
     * 사용자 등록 정보를 바탕으로 사용자를 생성
     *
     * @param command 이메일, 이름, 역할({@code CREATOR} 또는 {@code STUDENT})을 포함한 사용자 등록 정보
     * @return 생성된 사용자
     */
    User register(UserRegisterCommand command);
}
