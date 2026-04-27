package leehs.course.core.user.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import leehs.course.core.user.api.request.UserRegisterRequest;
import leehs.course.core.user.api.response.UserRegisterResponse;
import leehs.course.core.user.application.UserRegister;
import leehs.course.core.user.domain.model.User;
import leehs.course.core.user.domain.model.UserRole;
import leehs.course.core.user.domain.repository.UserRepository;
import leehs.course.fixture.UserFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class UserApiTest {

    final MockMvcTester mvcTester;

    final UserRegister userRegister;
    final UserRepository userRepository;

    final ObjectMapper objectMapper;

    @Test
    void whenRegisterRequestIsValid_expectCreatedUserResponse() throws JsonProcessingException, UnsupportedEncodingException {
        var request = UserFixture.createUserRegisterRequest("test@test.com");
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.userId", value -> assertThat(value).asNumber().isNotNull())
            .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo(request.email()));

        UserRegisterResponse response =
            objectMapper.readValue(result.getResponse().getContentAsString(), UserRegisterResponse.class);

        User user = userRepository.findById(response.userId()).orElseThrow();
        assertThat(user.getEmail().address()).isEqualTo(request.email());
        assertThat(user.getName()).isEqualTo(request.name());
        assertThat(user.getRole()).isEqualTo(UserRole.from(request.role()));
    }

    @Test
    void whenRegisterRequestHasDuplicateEmail_expectClientErrorResponse() throws JsonProcessingException {
        userRegister.register(UserFixture.createCreatorRegisterCommand("duplicate@test.com"));

        var request = UserFixture.createUserRegisterRequest("duplicate@test.com");
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .apply(print())
            .hasStatus4xxClientError();
    }

    @Test
    void whenRegisterRequestHasInvalidRole_expectClientErrorResponse() throws JsonProcessingException {
        var request = new UserRegisterRequest("invalid-role@test.com", "수강생", "INVALID");
        String json = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .exchange();

        assertThat(result)
            .apply(print())
            .hasStatus4xxClientError();
    }
}
