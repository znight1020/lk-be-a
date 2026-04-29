package leehs.course.core.course.application;

import leehs.course.core.course.domain.model.Course;

/**
 * 동시성 제어를 위해 락을 걸어 강의를 조회하는 기능 제공
 */
public interface CourseLockFinder {

    /**
     * 락을 획득하며 강의 조회
     *
     * @param courseId 락과 함께 조회할 강의 ID
     * @return 락이 적용된 강의
     */
    Course findWithLock(Long courseId);
}
