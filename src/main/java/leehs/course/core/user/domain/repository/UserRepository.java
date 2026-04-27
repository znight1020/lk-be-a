package leehs.course.core.user.domain.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import leehs.course.core.user.domain.model.Email;
import leehs.course.core.user.domain.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(Email email);
}
