package leehs.course.core.enrollment.application;

import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.domain.model.Enrollment;

/**
 * 수강 신청 기능을 제공
 */
public interface EnrollmentApplier {

    /**
     * 수강 신청 정보를 바탕으로 수강 신청을 생성
     *
     * @param command 수강 신청을 요청한 학생 사용자 ID와 신청 대상 강의 ID를 포함한 정보
     * @return 생성된 수강 신청
     */
    Enrollment apply(EnrollmentApplyCommand command);
}
