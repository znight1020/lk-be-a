package leehs.course.core.course.application.service;

import static leehs.course.core.user.domain.model.UserRole.CREATOR;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import leehs.course.core.course.application.CourseEnrollmentFinder;
import leehs.course.core.course.application.CourseFinder;
import leehs.course.core.course.application.query.CourseEnrollmentFindQuery;
import leehs.course.core.course.application.result.CourseEnrollmentSummaryResult;
import leehs.course.core.course.domain.exception.CourseManagementForbiddenException;
import leehs.course.core.course.domain.exception.CourseNotOwnerException;
import leehs.course.core.course.domain.model.Course;
import leehs.course.core.course.domain.repository.CourseEnrollmentQueryRepository;
import leehs.course.core.user.application.UserFinder;
import leehs.course.core.user.domain.model.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseEnrollmentQueryService implements CourseEnrollmentFinder {

    private final CourseEnrollmentQueryRepository courseEnrollmentQueryRepository;
    private final CourseFinder courseFinder;

    private final UserFinder userFinder;

    @Override
    public List<CourseEnrollmentSummaryResult> findAll(CourseEnrollmentFindQuery query) {
        User user = userFinder.find(query.requestUserId());
        verifyCreatorRole(user);

        Course course = courseFinder.find(query.courseId());
        verifyCourseOwner(course, user.getId());

        return courseEnrollmentQueryRepository.findStudentSummariesByCourseId(query.courseId()).stream()
            .map(CourseEnrollmentSummaryResult::of)
            .toList();
    }

    private void verifyCreatorRole(User user) {
        if (user.getRole() != CREATOR)
            throw new CourseManagementForbiddenException();
    }

    private void verifyCourseOwner(Course course, Long requestUserId) {
        if (!Objects.equals(course.getCreator().getId(), requestUserId))
            throw new CourseNotOwnerException();
    }
}
