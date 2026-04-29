package leehs.course.core.enrollment.application;

import org.springframework.data.domain.Page;

import leehs.course.core.enrollment.application.query.EnrollmentFindQuery;
import leehs.course.core.enrollment.domain.model.Enrollment;

/**
 * 수강 신청 조회 기능 제공
 */
public interface EnrollmentFinder {

    /**
     * 수강 신청 ID 기반 수강 신청 조회
     *
     * @param enrollmentId 조회할 수강 신청 ID
     * @return 조회된 수강 신청
     */
    Enrollment find(Long enrollmentId);

    /**
     * 조회 조건에 맞는 수강 신청 목록 조회
     *
     * @param query 조회할 사용자 ID와 페이지 정보를 담은 조건
     * @return 조회된 수강 신청 페이지
     */
    Page<Enrollment> findAll(EnrollmentFindQuery query);
}
