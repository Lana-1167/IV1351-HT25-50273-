-- ==========================================================
--  Project: University Course & Teaching Allocation System
--  File: check_queries.sql
--  DBMS: PostgreSQL
--  Description:
--     OLAP queries for Task 2 – Analytical Reports
-- ==========================================================
/* =========================================================
   QUERY 1 — Planned hours for course instances (Table 4)
   Uses: v_planned_breakdown
========================================================= */

SELECT
    pb.course_code            AS "Course Code",
    pb.instance_id            AS "Course Instance ID",
    pb.hp                     AS "HP",
    pb.period                 AS "Period",
    pb.num_students           AS "# Students",
    pb.lecture_hours          AS "Lecture Hours",
    pb.tutorial_hours         AS "Tutorial Hours",
    pb.lab_hours              AS "Lab Hours",
    pb.seminar_hours          AS "Seminar Hours",
    pb.other_overhead_hours   AS "Other Overhead Hours",
    pb.admin_hours            AS "Admin",
    pb.exam_hours             AS "Exam",
    pb.total_planned_hours    AS "Total Hours"
FROM v_planned_breakdown pb
WHERE pb.year = 2025
ORDER BY pb.course_code, pb.instance_id;


/* =========================================================
   QUERY 2 — Actual allocated hours per teacher (Table 5)
   Uses: v_actual_breakdown
========================================================= */

SELECT
    ab.course_code            AS "Course Code",
    ab.instance_id            AS "Course Instance ID",
    ab.hp                     AS "HP",
    ab.teacher_name           AS "Teacher's Name",
    ab.designation            AS "Designation",
    ab.lecture_hours          AS "Lecture Hours",
    ab.tutorial_hours         AS "Tutorial Hours",
    ab.lab_hours              AS "Lab Hours",
    ab.seminar_hours          AS "Seminar Hours",
    ab.other_overhead_hours   AS "Other Overhead Hours",
    ab.admin_hours            AS "Admin",
    ab.exam_hours             AS "Exam",
    ab.total_allocated_hours  AS "Total"
FROM v_actual_breakdown ab
WHERE ab.year = 2025
ORDER BY ab.course_code, ab.instance_id, ab.teacher_name;


/* =========================================================
   QUERY 3 — Total allocated hours for ONE teacher (Table 6)
   Uses: v_actual_breakdown
========================================================= */

-- CHANGE THIS VALUE manually
-- teacher_id=500009 corresponds to Niharika Gauraha
SELECT
    ab.course_code            AS "Course Code",
    ab.instance_id            AS "Course Instance ID",
    ab.hp                     AS "HP",
    ab.period                 AS "Period",
    ab.teacher_name           AS "Teacher's Name",
    ab.lecture_hours          AS "Lecture Hours",
    ab.tutorial_hours         AS "Tutorial Hours",
    ab.lab_hours              AS "Lab Hours",
    ab.seminar_hours          AS "Seminar Hours",
    ab.other_overhead_hours   AS "Other Overhead Hours",
    ab.admin_hours            AS "Admin",
    ab.exam_hours             AS "Exam",
    ab.total_allocated_hours  AS "Total"
FROM v_actual_breakdown ab
WHERE ab.year = 2025
  AND ab.emp_id = 500001     -- <==== EDIT HERE
ORDER BY ab.period, ab.course_code;


/* =========================================================
   QUERY 4 — Teachers allocated in MORE than N courses (Table 7)
   Uses: v_teacher_instance_count
========================================================= */

-- CHANGE VALUE manually:
-- Example N = 1
SELECT
    tic.emp_id           AS "Employment ID",
    tic.teacher_name     AS "Teacher's Name",
    tic.period           AS "Period",
    tic.num_instances    AS "No of courses"
FROM v_teacher_instance_count tic
WHERE tic.year = 2025
  AND tic.num_instances = 1    -- <=== EDIT HERE
ORDER BY tic.num_instances DESC, tic.teacher_name;

-- ==========================================================
-- End of Script
-- ==========================================================
