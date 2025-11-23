-- ==========================================================
--  Project: University Course & Teaching Allocation System
--  File: indexes_and_views.sql
--  DBMS: PostgreSQL
--  Description:
--     Performance tuning: Indexes, Views, and Materialized Views
-- ==========================================================

-- ==========================================================
-- Run after create_database.sql and insert_data.sql
-- ==========================================================

-- -------------------------
-- Indexes (help joins & filters)
-- -------------------------
CREATE INDEX IF NOT EXISTS idx_employee_dept ON Employee(dept_id);
CREATE INDEX IF NOT EXISTS idx_employee_email ON Employee(email);
CREATE INDEX IF NOT EXISTS idx_ci_layout ON CourseInstance(layout_id);
CREATE INDEX IF NOT EXISTS idx_ci_year_period ON CourseInstance(year, period);
CREATE INDEX IF NOT EXISTS idx_pa_instance ON PlannedActivity(instance_id);
CREATE INDEX IF NOT EXISTS idx_pa_activity ON PlannedActivity(activity_id);
CREATE INDEX IF NOT EXISTS idx_alloc_instance ON Allocation(instance_id);
CREATE INDEX IF NOT EXISTS idx_alloc_emp ON Allocation(emp_id);
CREATE INDEX IF NOT EXISTS idx_alloc_activity ON Allocation(activity_id);

-- -------------------------
-- View: v_planned_breakdown
-- Sums planned hours * factor per activity category per instance
-- -------------------------
CREATE OR REPLACE VIEW v_planned_breakdown AS
SELECT
  ci.instance_id,
  cl.course_code,
  cl.credits   AS hp,
  ci.period,
  ci.year,
  ci.num_students,
  -- pivot columns (weighted = planned_hours * factor)
  COALESCE(SUM(CASE WHEN at.activity_name = 'Lecture' THEN pa.planned_hours * at.factor END),0)    AS lecture_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Tutorial' THEN pa.planned_hours * at.factor END),0)   AS tutorial_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Lab' THEN pa.planned_hours * at.factor END),0)        AS lab_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Seminar' THEN pa.planned_hours * at.factor END),0)     AS seminar_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Overhead' THEN pa.planned_hours * at.factor END),0)    AS other_overhead_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Admin' THEN pa.planned_hours * at.factor END),0)       AS admin_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Exam' THEN pa.planned_hours * at.factor END),0)        AS exam_hours,
  COALESCE(SUM(pa.planned_hours * at.factor),0) AS total_planned_hours
FROM CourseInstance ci
JOIN CourseLayout cl ON ci.layout_id = cl.layout_id
LEFT JOIN PlannedActivity pa ON pa.instance_id = ci.instance_id
LEFT JOIN ActivityType at ON pa.activity_id = at.activity_id
GROUP BY ci.instance_id, cl.course_code, cl.credits, ci.period, ci.year, ci.num_students;

-- -------------------------
-- View: v_actual_breakdown
-- Sums allocated hours * factor per activity category per instance and per teacher
-- -------------------------
CREATE OR REPLACE VIEW v_actual_breakdown AS
SELECT
  a.instance_id,
  cl.course_code,
  cl.credits AS hp,
  ci.period,
  ci.year,
  a.emp_id,
  e.first_name || ' ' || e.last_name AS teacher_name,
  e.designation,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Lecture' THEN a.allocated_hours * at.factor END),0)   AS lecture_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Tutorial' THEN a.allocated_hours * at.factor END),0)  AS tutorial_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Lab' THEN a.allocated_hours * at.factor END),0)       AS lab_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Seminar' THEN a.allocated_hours * at.factor END),0)    AS seminar_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Overhead' THEN a.allocated_hours * at.factor END),0)   AS other_overhead_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Admin' THEN a.allocated_hours * at.factor END),0)      AS admin_hours,
  COALESCE(SUM(CASE WHEN at.activity_name = 'Exam' THEN a.allocated_hours * at.factor END),0)       AS exam_hours,
  COALESCE(SUM(a.allocated_hours * at.factor),0) AS total_allocated_hours
FROM Allocation a
JOIN CourseInstance ci ON a.instance_id = ci.instance_id
JOIN CourseLayout cl ON ci.layout_id = cl.layout_id
JOIN Employee e ON a.emp_id = e.emp_id
JOIN ActivityType at ON a.activity_id = at.activity_id
GROUP BY a.instance_id, cl.course_code, cl.credits, ci.period, ci.year, a.emp_id, teacher_name, e.designation;

-- -------------------------
-- View: v_teacher_instance_count
-- Counts distinct instances per teacher per period/year
-- -------------------------
CREATE OR REPLACE VIEW v_teacher_instance_count AS
SELECT
  a.emp_id,
  e.first_name || ' ' || e.last_name AS teacher_name,
  ci.year,
  ci.period,
  COUNT(DISTINCT a.instance_id) AS num_instances
FROM Allocation a
JOIN CourseInstance ci ON a.instance_id = ci.instance_id
JOIN Employee e ON a.emp_id = e.emp_id
GROUP BY a.emp_id, teacher_name, ci.year, ci.period;

-- -------------------------
-- View: v_plan_vs_actual
-- Compares planned_total_hours and actual_total_allocated per instance
-- -------------------------
CREATE OR REPLACE VIEW v_plan_vs_actual AS
SELECT
  pb.instance_id,
  pb.course_code,
  pb.hp,
  pb.period,
  pb.year,
  pb.num_students,
  pb.total_planned_hours,
  COALESCE(SUM(ab.total_allocated_hours),0) AS total_actual_allocated_hours,
  CASE
    WHEN pb.total_planned_hours = 0 THEN NULL
    ELSE ROUND((COALESCE(SUM(ab.total_allocated_hours),0) - pb.total_planned_hours) / pb.total_planned_hours * 100, 2)
  END AS variance_percent
FROM v_planned_breakdown pb
LEFT JOIN v_actual_breakdown ab ON pb.instance_id = ab.instance_id
GROUP BY pb.instance_id, pb.course_code, pb.hp, pb.period, pb.year, pb.num_students, pb.total_planned_hours;

-- -------------------------
-- Materialized view: mv_teacher_yearly_load (heavy aggregation)
-- -------------------------
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_teacher_yearly_load AS
SELECT
  e.emp_id,
  e.first_name || ' ' || e.last_name AS teacher_name,
  ci.year,
  SUM(a.allocated_hours * at.factor) AS total_weighted_hours
FROM Allocation a
JOIN Employee e ON a.emp_id = e.emp_id
JOIN CourseInstance ci ON a.instance_id = ci.instance_id
JOIN ActivityType at ON a.activity_id = at.activity_id
GROUP BY e.emp_id, teacher_name, ci.year;

-- -------------------------
-- Materialized view: mv_course_cost
-- Estimates cost per course instance using salary/160h
-- -------------------------
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_course_cost AS
SELECT
  ci.instance_id,
  cl.course_code,
  ci.period,
  ci.year,
  ROUND(SUM((e.salary / 160.0) * (a.allocated_hours * at.factor)), 2) AS total_cost_sek
FROM Allocation a
JOIN Employee e ON a.emp_id = e.emp_id
JOIN CourseInstance ci ON a.instance_id = ci.instance_id
JOIN CourseLayout cl ON ci.layout_id = cl.layout_id
JOIN ActivityType at ON a.activity_id = at.activity_id
GROUP BY ci.instance_id, cl.course_code, ci.period, ci.year;

-- -------------------------
-- Notes:
-- - If ActivityType factors are not the ones expected (e.g. 3.6 for Lecture),
--   update ActivityType table accordingly and refresh views/materialized views.
-- - To refresh materialized views:
--     REFRESH MATERIALIZED VIEW mv_teacher_yearly_load;
--     REFRESH MATERIALIZED VIEW mv_course_cost;
-- -------------------------
-- End
-- ==========================================================
-- Refresh example:
-- REFRESH MATERIALIZED VIEW mv_course_cost_summary;

-- ==========================================================
-- Performance Testing Helpers
-- ----------------------------------------------------------
-- Use EXPLAIN ANALYZE on the following for optimization checks
-- ==========================================================

-- EXPLAIN ANALYZE SELECT * FROM v_plan_vs_actual;
-- EXPLAIN ANALYZE SELECT * FROM mv_teacher_load_summary;

-- ==========================================================
-- End of Script
-- ==========================================================
