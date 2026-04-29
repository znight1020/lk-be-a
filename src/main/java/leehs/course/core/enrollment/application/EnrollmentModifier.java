package leehs.course.core.enrollment.application;

import leehs.course.core.enrollment.application.command.EnrollmentStatusModifyCommand;
import leehs.course.core.enrollment.domain.model.Enrollment;

/**
 * 수강 신청 상태 변경 기능 제공
 */
public interface EnrollmentModifier {

    /**
     * 수강 신청을 결제 완료에 해당하는 {@code CONFIRMED} 상태로 변경
     *
     * @param enrollmentId 상태를 변경할 수강 신청 ID
     * @param command 상태 변경을 요청한 학생 사용자 ID를 담은 정보. 학생 권한 및 본인 소유 여부 검증에 사용
     * @return 상태가 변경된 수강 신청
     */
    Enrollment confirm(Long enrollmentId, EnrollmentStatusModifyCommand command);

    /**
     * 수강 신청을 {@code CANCELLED} 상태로 변경. {@code CONFIRMED} 상태는 취소 가능 기간을 만족해야 함
     *
     * @param enrollmentId 상태를 변경할 수강 신청 ID
     * @param command 상태 변경을 요청한 학생 사용자 ID를 담은 정보. 학생 권한 및 본인 소유 여부 검증에 사용
     * @return 상태가 변경된 수강 신청
     */
    Enrollment cancel(Long enrollmentId, EnrollmentStatusModifyCommand command);
}
