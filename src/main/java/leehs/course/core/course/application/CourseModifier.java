package leehs.course.core.course.application;

import leehs.course.core.course.application.command.CourseStatusModifyCommand;
import leehs.course.core.course.domain.model.Course;

/**
 * 강의 상태 변경 기능 제공
 */
public interface CourseModifier {

    /**
     * 강의를 모집 가능 상태로 변경
     *
     * @param courseId 상태를 변경할 강의 ID
     * @param command 상태 변경을 요청한 사용자 ID를 담은 정보. 강사 권한 및 강의 소유자 검증에 사용
     * @return 상태가 변경된 강의
     */
    Course open(Long courseId, CourseStatusModifyCommand command);

    /**
     * 강의를 모집 마감 상태로 변경
     *
     * @param courseId 상태를 변경할 강의 ID
     * @param command 상태 변경을 요청한 사용자 ID를 담은 정보. 강사 권한 및 강의 소유자 검증에 사용된다.
     * @return 상태가 변경된 강의
     */
    Course close(Long courseId, CourseStatusModifyCommand command);
}
