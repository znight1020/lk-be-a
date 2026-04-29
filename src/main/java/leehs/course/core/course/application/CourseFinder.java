package leehs.course.core.course.application;

import java.util.List;

import leehs.course.core.course.application.query.CourseFindQuery;
import leehs.course.core.course.application.result.CourseDetailResult;
import leehs.course.core.course.domain.model.Course;

/**
 * 강의 조회 기능 제공
 */
public interface CourseFinder {

    /**
     * 강의 ID 기반 강의 조회.
     *
     * @param courseId 조회할 강의 ID
     * @return 조회된 강의
     */
    Course find(Long courseId);

    /**
     * 조회 조건에 맞는 강의 목록 조회
     *
     * @param query 조회할 강의 상태({@code DRAFT}, {@code OPEN}, {@code CLOSED})를 담은 조건. 상태가 {@code null}이면 전체 강의를 조회한다.
     * @return 조회된 강의 목록
     */
    List<Course> findAll(CourseFindQuery query);

    /**
     * 현재 활성 수강 인원({@code PENDING}, {@code CONFIRMED})을 포함한 강의 상세 정보 조회
     *
     * @param courseId 조회할 강의 ID
     * @return 조회된 강의 상세 정보
     */
    CourseDetailResult findDetail(Long courseId);
}
