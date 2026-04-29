package leehs.course.core.course.application;

import java.util.List;

import leehs.course.core.course.application.query.CourseEnrollmentFindQuery;
import leehs.course.core.course.application.result.CourseEnrollmentSummaryResult;

/**
 * 강의별 수강생 목록 조회 기능 제공
 */
public interface CourseEnrollmentFinder {

    /**
     * 조회 조건에 맞는 강의별 수강생 목록 조회
     *
     * @param query 조회할 강의 ID와 요청 사용자 ID를 담은 조건
     * @return 조회된 강의별 수강생 목록
     */
    List<CourseEnrollmentSummaryResult> findAll(CourseEnrollmentFindQuery query);
}
