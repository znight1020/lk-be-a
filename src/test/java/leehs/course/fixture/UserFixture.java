package leehs.course.fixture;

import leehs.course.core.user.api.request.UserRegisterRequest;
import leehs.course.core.user.application.command.UserRegisterCommand;
import leehs.course.core.user.domain.model.User;

public class UserFixture {

    public static UserRegisterRequest createUserRegisterRequest(String email) {
        return new UserRegisterRequest(email, "수강생", "STUDENT");
    }

    public static UserRegisterCommand createStudentRegisterCommand(String email) {
        return new UserRegisterCommand(email, "수강생", "STUDENT");
    }

    public static UserRegisterCommand createCreatorRegisterCommand(String email) {
        return new UserRegisterCommand(email, "김강사", "CREATOR");
    }

    public static User createCreator(String email) {
        return User.register(email, "강사", "CREATOR");
    }

    public static User createStudent(String email) {
        return User.register(email, "수강생", "STUDENT");
    }
}
