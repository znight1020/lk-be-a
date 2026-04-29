package leehs.course.global.web.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Course Enrollment API",
        version = "v1",
        description = "강의 관리, 수강 관리를 제공하는 API 문서입니다."
    ),
    servers = @Server(url = "http://localhost:8080", description = "Local server")
)
public class OpenApiConfig {
}
