package leehs.course.core.course.application;

import leehs.course.core.course.application.command.CourseCreateCommand;
import leehs.course.core.course.domain.model.Course;

/**
 * 강의 생성 기능 제공
 */
public interface CourseCreator {

    /**
     * 강의 생성 정보를 바탕으로 강의 생성
     *
     * @param command 강의를 생성할 사용자 ID와 강의 생성에 필요한 제목, 설명, 가격, 정원(최대 수강 인원),
     * 수강 기간(시작일~종료일)을 포함한 정보
     * @return 생성된 강의
     */
    Course create(CourseCreateCommand command);
}
