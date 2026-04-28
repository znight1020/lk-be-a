package leehs.course.global.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.time.LocalDateTime;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleUnexpectedException(RuntimeException ex) {
        log.error("Unhandled exception occurred.", ex);

        String detailMessage = "예기치 못한 오류가 발생했습니다. 나중에 다시 시도해 주세요.";

        return createProblemDetail(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", detailMessage);
    }

    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handleApplicationException(ApplicationException ex) {
        return createProblemDetail(HttpStatusCode.valueOf(ex.getStatus()), ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
        String detailMessage = ex.getConstraintViolations().stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse("잘못된 요청 값입니다");

        return createProblemDetail(BAD_REQUEST, "VALIDATION_ERROR", detailMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();

        String detailMessage = bindingResult.getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("잘못된 요청 값입니다");

        return createProblemDetail(BAD_REQUEST, "VALIDATION_ERROR", detailMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detailMessage = String.format("[%s] 값이 올바르지 않습니다: %s", ex.getName(), ex.getValue());

        return createProblemDetail(BAD_REQUEST, "TYPE_MISMATCH", detailMessage);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingRequestHeader(MissingRequestHeaderException ex) {
        String detailMessage = String.format("[%s] 헤더는 필수입니다", ex.getHeaderName());

        return createProblemDetail(BAD_REQUEST, "MISSING_REQUEST_HEADER", detailMessage);
    }

    private ProblemDetail createProblemDetail(HttpStatusCode status, String title, String detailMessage) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return problemDetail;
    }
}
