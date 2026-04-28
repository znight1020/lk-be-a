package leehs.course.core.enrollment.application;

import leehs.course.core.enrollment.application.command.EnrollmentApplyCommand;
import leehs.course.core.enrollment.domain.model.Enrollment;

public interface EnrollmentApplier {

    Enrollment apply(EnrollmentApplyCommand command);
}
