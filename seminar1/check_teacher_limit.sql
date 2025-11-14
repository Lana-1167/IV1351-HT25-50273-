-- ==========================================================
--  Project: University Course & Teaching Allocation System
--  File: check_teacher_limit.sql
--  DBMS: PostgreSQL
--  Author: Lana Ryzhova
--  Date: 2025-11-15
--  Description:
--     Enforce "no more than 4 courses per teacher per period" 
-- ==========================================================

-- ==========================================================
CREATE FUNCTION check_teacher_limit() RETURNS TRIGGER AS $$
BEGIN
  IF (SELECT COUNT(DISTINCT ci.instance_id)
      FROM Allocation a
      JOIN CourseInstance ci ON a.instance_id = ci.instance_id
      WHERE a.emp_id = NEW.emp_id AND ci.period = (SELECT period FROM CourseInstance WHERE instance_id = NEW.instance_id)) >= 4 THEN
      RAISE EXCEPTION 'Teacher cannot be allocated to more than 4 course instances per period';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_teacher_limit
BEFORE INSERT ON Allocation
FOR EACH ROW EXECUTE FUNCTION check_teacher_limit();

-- ==========================================================
-- End of Script
-- ==========================================================
-- ==========================================================
-- DROP FUNCTION and all its dependencies 
-- (including the trigger attached to it) or just a trigger
-- ==========================================================
-- DROP FUNCTION IF EXISTS check_teacher_limit() CASCADE;
-- DROP TRIGGER trg_teacher_limit ON Allocation;