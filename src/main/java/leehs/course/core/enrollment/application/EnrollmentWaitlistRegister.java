package leehs.course.core.enrollment.application;

import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.domain.model.Enrollment;

/**
 * 수강 대기열 등록 기능을 제공
 */
public interface EnrollmentWaitlistRegister {

    /**
     * 수강 신청 정보를 바탕으로 대기열에 등록
     *
     * @param command 대기열 등록을 요청한 학생 사용자 ID와 대상 강의 ID를 포함한 정보
     * @return 생성된 수강 신청
     */
    Enrollment registerWaitlist(EnrollmentApplyCommand command);
}
