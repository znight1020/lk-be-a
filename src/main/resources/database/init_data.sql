-- init 프로필용 더미 데이터
-- User :
    -- user id 1 : 강사
    -- user id 101 : 수강생1
    -- user id 102 : 수강생2
    -- user id 103 : 수강생3
-- Course
    -- course id 1: OPEN 상태, 정원은 2명이며 현재 신청이 가득 찬 강의
    -- course id 2: OPEN 상태, 정원은 5명이며 아직 수강 신청이 없는 빈 강의
    -- course id 3: DRAFT 상태, 아직 모집 전인 강의
    -- course id 4: CLOSED 상태, 모집이 마감된 강의

delete from enrollments;
delete from courses;
delete from users;

insert into users (id, email_address, name, role, created_at, updated_at) values
    (1, 'creator1@example.com', '김강사', 'CREATOR', CURRENT_TIMESTAMP - INTERVAL 30 DAY, CURRENT_TIMESTAMP - INTERVAL 30 DAY),
    (101, 'student101@example.com', '수강생1', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL 20 DAY, CURRENT_TIMESTAMP - INTERVAL 20 DAY),
    (102, 'student102@example.com', '수강생2', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL 19 DAY, CURRENT_TIMESTAMP - INTERVAL 19 DAY),
    (103, 'student103@example.com', '수강생3', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL 18 DAY, CURRENT_TIMESTAMP - INTERVAL 18 DAY);

insert into courses (id, creator_id, title, description, price, capacity, start_date, end_date, status, created_at, updated_at) values
    (1, 1, 'Spring Boot 입문', '스프링 부트 기초 강의', 120000, 2, CURRENT_DATE + INTERVAL 10 DAY, CURRENT_DATE + INTERVAL 40 DAY, 'OPEN', CURRENT_TIMESTAMP - INTERVAL 9 DAY, CURRENT_TIMESTAMP - INTERVAL 9 DAY),
    (2, 1, 'MSA 설계', 'MSA 아키텍처 기초', 55000, 5, CURRENT_DATE + INTERVAL 9 DAY, CURRENT_DATE + INTERVAL 39 DAY, 'OPEN', CURRENT_TIMESTAMP - INTERVAL 8 DAY, CURRENT_TIMESTAMP - INTERVAL 8 DAY),
    (3, 1, 'Redis 활용', '레디스 활용 강의', 50000, 3, CURRENT_DATE + INTERVAL 7 DAY, CURRENT_DATE + INTERVAL 37 DAY, 'DRAFT', CURRENT_TIMESTAMP - INTERVAL 7 DAY, CURRENT_TIMESTAMP - INTERVAL 7 DAY),
    (4, 1, '데이터베이스 인덱스', '인덱스 설계와 실행 계획 분석', 60000, 2, CURRENT_DATE + INTERVAL 11 DAY, CURRENT_DATE + INTERVAL 41 DAY, 'CLOSED', CURRENT_TIMESTAMP - INTERVAL 6 DAY, CURRENT_TIMESTAMP - INTERVAL 6 DAY);

insert into enrollments (id, course_id, student_id, status, created_at, confirmed_at, cancelled_at, updated_at) values
    (1, 1, 102, 'PENDING', CURRENT_TIMESTAMP - INTERVAL 1 DAY, null, null, CURRENT_TIMESTAMP - INTERVAL 1 DAY),
    (2, 1, 103, 'CONFIRMED', CURRENT_TIMESTAMP - INTERVAL 3 DAY, CURRENT_TIMESTAMP - INTERVAL 2 DAY, null, CURRENT_TIMESTAMP - INTERVAL 2 DAY), (3, 4, 2, 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL 6 DAY, null, CURRENT_TIMESTAMP - INTERVAL 5 DAY, CURRENT_TIMESTAMP - INTERVAL 5 DAY);

alter table users auto_increment = 500;
alter table courses auto_increment = 500;
alter table enrollments auto_increment = 500;