package leehs.course.core.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.user.application.UserRegister;
import leehs.course.core.user.application.command.UserRegisterCommand;
import leehs.course.core.user.domain.exception.EmailDuplicatedException;
import leehs.course.core.user.domain.model.Email;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService implements UserRegister {

    private final UserRepository userRepository;

    @Override
    public User register(UserRegisterCommand command) {
        verifyDuplicateEmail(command.email());

        User user = User.register(command.email(), command.name(), command.role());

        return userRepository.save(user);
    }

    private void verifyDuplicateEmail(String email) {
        if (userRepository.findByEmail(new Email(email)).isPresent())
            throw new EmailDuplicatedException();
    }
}
