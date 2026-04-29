package leehs.course.core.enrollment.application;

import leehs.course.core.enrollment.application.command.EnrollmentStatusModifyCommand;
import leehs.course.core.enrollment.domain.model.Enrollment;

public interface EnrollmentModifier {

    Enrollment confirm(Long enrollmentId, EnrollmentStatusModifyCommand command);

    Enrollment cancel(Long enrollmentId, EnrollmentStatusModifyCommand command);
}
