package leehs.course.core.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.user.application.UserFinder;
import leehs.course.core.user.domain.exception.UserNotFoundException;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService implements UserFinder {

    private final UserRepository userRepository;

    @Override
    public User find(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);
    }
}
