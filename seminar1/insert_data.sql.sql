-- ==========================================================
--  Project: University Course & Teaching Allocation System
--  File: insert_data.sql
--  DBMS: PostgreSQL
--  Description:
--     Logical & Physical Database Model (Task 1)
-- ==========================================================
-- ==========================================================
-- Clean all tables before inserting
-- ==========================================================
TRUNCATE Allocation RESTART IDENTITY CASCADE;
TRUNCATE PlannedActivity RESTART IDENTITY CASCADE;
TRUNCATE ActivityType RESTART IDENTITY CASCADE;
TRUNCATE CourseInstance RESTART IDENTITY CASCADE;
TRUNCATE CourseLayout RESTART IDENTITY CASCADE;
TRUNCATE Employee RESTART IDENTITY CASCADE;
TRUNCATE Department RESTART IDENTITY CASCADE;

-- ==========================================================
-- Departments
-- ==========================================================
INSERT INTO Department(dept_name) VALUES
    ('Computer Science'),
    ('Electrical Engineering'),
    ('Mathematics');

-- ==========================================================
-- Employees (Teachers)
-- ==========================================================
INSERT INTO Employee(first_name, last_name, email, phone, designation, salary, dept_id)
VALUES
    ('Paris', 'Carbone', 'paris@kth.se', '070-1000001', 'Ass. Professor', 55000, 1),
    ('Leif', 'Lindbäck', 'leif@kth.se', '070-1000002', 'Lecturer', 42000, 1),
    ('Niharika', 'Gauraha', 'nih@kth.se', '070-1000003', 'Lecturer', 42000, 1),
    ('Brian', 'Karlsson', 'brian@kth.se', '070-1000004', 'PhD Student', 30000, 1),
    ('Adam', 'West', 'adam@kth.se', '070-1000005', 'TA', 25000, 1);

-- ==========================================================
-- CourseLayout (2 courses, 2 versions each)
-- ==========================================================
INSERT INTO CourseLayout(course_code, version_no, course_name, credits, min_students, max_students, valid_from)
VALUES
    ('IV1351', 1, 'Applied Cloud Computing', 7.5, 50, 300, '2020-01-01'),
    ('IV1351', 2, 'Applied Cloud Computing', 7.5, 50, 300, '2023-01-01'),

    ('IX1500', 1, 'Data Ethics & AI Regulation', 7.5, 30, 200, '2020-01-01'),
    ('IX1500', 2, 'Data Ethics & AI Regulation', 7.5, 30, 200, '2023-01-01');

-- ==========================================================
-- CourseInstance (2025)
-- ==========================================================
INSERT INTO CourseInstance(layout_id, course_code, version_no, period, year, num_students)
VALUES
    (1, 'IV1351', 1, 'P2', 2025, 200),   -- instance_id = 1
    (3, 'IX1500', 1, 'P1', 2025, 150);  -- instance_id = 2

-- ==========================================================
-- ActivityType (7 types)
-- ==========================================================
INSERT INTO ActivityType(activity_name, factor)
VALUES
    ('Lecture', 1.0),
    ('Tutorial', 1.0),
    ('Lab', 1.0),
    ('Seminar', 1.0),
    ('Overhead', 1.0),
    ('Admin', 1.0),
    ('Exam', 1.0);

-- ==========================================================
-- PlannedActivity (matching the example tables)
-- ==========================================================
-- For IV1351 instance 1
INSERT INTO PlannedActivity(instance_id, activity_id, planned_hours) VALUES
(1, 1, 72),   -- Lecture
(1, 2, 192),  -- Tutorial
(1, 3, 96),   -- Lab
(1, 4, 144),  -- Seminar
(1, 5, 650),  -- Overhead
(1, 6, 177),  -- Admin
(1, 7, 83);   -- Exam

-- For IX1500 instance 2
INSERT INTO PlannedActivity(instance_id, activity_id, planned_hours) VALUES
(2, 1, 159), -- Lecture
(2, 2, 0),
(2, 3, 0),
(2, 4, 116), -- Seminar
(2, 5, 270), -- Overhead
(2, 6, 141), -- Admin
(2, 7, 73);  -- Exam

-- ==========================================================
-- Allocation (Actual — matches assignment example)
-- ==========================================================

-- IV1351, 5 teachers
-- Paris Carbone
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
VALUES
(1, 1, 1, 72),     -- Lecture
(1, 1, 5, 100),    -- Overhead
(1, 1, 6, 43),     -- Admin
(1, 1, 7, 61);     -- Exam

-- Leif Lindbäck
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
VALUES
(2, 1, 4, 64),
(2, 1, 5, 100),
(2, 1, 7, 62);

-- Niharika Gauraha
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
VALUES
(3, 1, 4, 64),
(3, 1, 5, 100),
(3, 1, 6, 43),
(3, 1, 7, 61);

-- Brian
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
VALUES
(4, 1, 3, 50),
(4, 1, 5, 100);

-- Adam
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
VALUES
(5, 1, 3, 50),
(5, 1, 4, 50);

-- IX1500 — only Niharika Gauraha
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
VALUES
(3, 2, 1, 159),  
(3, 2, 5, 100),
(3, 2, 6, 141),
(3, 2, 7, 73);

-- ==========================================================
-- Verify data
-- ==========================================================
-- Example quick checks:
-- SELECT * FROM Department;
-- SELECT * FROM Employee;
-- SELECT * FROM CourseLayout;
-- SELECT * FROM CourseInstance;
-- SELECT * FROM ActivityType;
-- SELECT * FROM PlannedActivity;
-- SELECT * FROM Allocation;

-- ==========================================================
-- End of Script
-- ==========================================================
