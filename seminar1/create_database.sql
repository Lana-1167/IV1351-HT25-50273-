-- ==========================================================
--  Project: University Course & Teaching Allocation System
--  File: create_database.sql
--  DBMS: PostgreSQL
--  Description:
--     Logical & Physical Database Model (Task 1)
-- ==========================================================
-- Drop existing tables
-- ==========================================================
DROP TABLE IF EXISTS Allocation CASCADE;
DROP TABLE IF EXISTS PlannedActivity CASCADE;
DROP TABLE IF EXISTS ActivityType CASCADE;
DROP TABLE IF EXISTS CourseInstance CASCADE;
DROP TABLE IF EXISTS CourseLayout CASCADE;
DROP TABLE IF EXISTS Employee CASCADE;
DROP TABLE IF EXISTS Department CASCADE;

-- ==========================================================
-- Department
-- ==========================================================
CREATE TABLE Department (
    dept_id        SERIAL PRIMARY KEY,
    dept_name      VARCHAR(100) NOT NULL UNIQUE,
    manager_id     INT
);

-- ==========================================================
-- Employee
-- ==========================================================
CREATE TABLE Employee (
    emp_id         SERIAL PRIMARY KEY,
    first_name     VARCHAR(50) NOT NULL,
    last_name      VARCHAR(50) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    phone          VARCHAR(30),
    designation    VARCHAR(50) NOT NULL,
    salary         NUMERIC(10,2) CHECK (salary > 0),
    dept_id        INT,
    manager_id     INT,
    FOREIGN KEY (dept_id) REFERENCES Department(dept_id) ON DELETE SET NULL,
    FOREIGN KEY (manager_id) REFERENCES Employee(emp_id) ON DELETE SET NULL
);

ALTER TABLE Department
    ADD CONSTRAINT fk_dept_manager FOREIGN KEY (manager_id)
        REFERENCES Employee(emp_id) ON DELETE SET NULL;

-- ==========================================================
-- CourseLayout
-- ==========================================================
CREATE TABLE CourseLayout (
    layout_id      SERIAL PRIMARY KEY,
    course_code    VARCHAR(10) NOT NULL,
    version_no     INT NOT NULL,
    course_name    VARCHAR(100) NOT NULL,
    credits        NUMERIC(4,1) NOT NULL CHECK (credits > 0),
    min_students   INT CHECK (min_students >= 0),
    max_students   INT CHECK (max_students >= min_students),
    valid_from     DATE NOT NULL,
    valid_to       DATE,
    UNIQUE(course_code, version_no)
);

-- ==========================================================
-- CourseInstance
-- ==========================================================
CREATE TABLE CourseInstance (
    instance_id    SERIAL PRIMARY KEY,
    layout_id      INT NOT NULL,
    course_code    VARCHAR(10) NOT NULL,
    version_no     INT NOT NULL,
    period         VARCHAR(2) CHECK (period IN ('P1','P2','P3','P4')),
    year           INT CHECK (year >= 2000),
    num_students   INT CHECK (num_students >= 0),
    FOREIGN KEY (layout_id) REFERENCES CourseLayout(layout_id)
);

-- ==========================================================
-- ActivityType
-- ==========================================================
CREATE TABLE ActivityType (
    activity_id    SERIAL PRIMARY KEY,
    activity_name  VARCHAR(50) NOT NULL UNIQUE,
    factor         NUMERIC(4,2) NOT NULL CHECK (factor > 0)
);

-- ==========================================================
-- PlannedActivity
-- ==========================================================
CREATE TABLE PlannedActivity (
    pa_id          SERIAL PRIMARY KEY,
    instance_id    INT NOT NULL,
    activity_id    INT NOT NULL,
    planned_hours  NUMERIC(6,2) CHECK (planned_hours >= 0),
    FOREIGN KEY (instance_id) REFERENCES CourseInstance(instance_id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES ActivityType(activity_id)
);

-- ==========================================================
-- Allocation
-- ==========================================================
CREATE TABLE Allocation (
    alloc_id        SERIAL PRIMARY KEY,
    emp_id          INT NOT NULL,
    instance_id     INT NOT NULL,
    activity_id     INT NOT NULL,
    allocated_hours NUMERIC(6,2) CHECK (allocated_hours >= 0),
    FOREIGN KEY (emp_id) REFERENCES Employee(emp_id) ON DELETE CASCADE,
    FOREIGN KEY (instance_id) REFERENCES CourseInstance(instance_id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES ActivityType(activity_id)
);

-- ==========================================================
	ALTER TABLE PlannedActivity
	ADD CONSTRAINT uq_activity UNIQUE(instance_id, activity_id);
-- ==========================================================
-- End
-- ==========================================================
