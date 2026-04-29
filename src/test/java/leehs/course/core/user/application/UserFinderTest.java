package leehs.course.core.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import leehs.course.core.user.domain.exception.UserNotFoundException;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.repository.UserRepository;

@Transactional
@SpringBootTest
class UserFinderTest {

    @Autowired
    UserFinder userFinder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("사용자 ID 기반 조회 - 성공")
    void whenFindExistingUserById_expectUserReturned() {
        User user = userRepository.save(User.register("test@test.com", "name", "CREATOR"));

        em.flush();
        em.clear();

        User found = userFinder.find(user.getId());
        assertNotNull(found.getEmail());
        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getName()).isEqualTo(user.getName());
    }

    @Test
    @DisplayName("사용자 ID 기반 조회 - 실패, 존재하지 않는 사용자")
    void whenFindNonExistingUserById_expectIllegalArgumentException() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> userFinder.find(nonExistentId))
            .isInstanceOf(UserNotFoundException.class);
    }
}
