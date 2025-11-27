-- ==========================================================
--  Project: University Course & Teaching Allocation System
--  File: check_teacher_limit.sql
--  DBMS: PostgreSQL
--  Description:
--     Enforce "no more than 4 courses per teacher per period" 
-- ==========================================================

-- ==========================================================
-- Checks function

CREATE OR REPLACE FUNCTION check_teacher_limit()
RETURNS TRIGGER AS $$
DECLARE
    cnt INT;
    inst_year INT;
    inst_period TEXT;
BEGIN

-- we get the year and period for the instance_id of the inserted record
    SELECT year, period INTO inst_year, inst_period
    FROM CourseInstance WHERE instance_id = NEW.instance_id;

    SELECT COUNT(DISTINCT a.instance_id) INTO cnt
    FROM Allocation a
    JOIN CourseInstance ci ON a.instance_id = ci.instance_id
    WHERE a.emp_id = NEW.emp_id
      AND ci.year = inst_year
      AND ci.period = inst_period;


-- if there are already 4 or more => refusal
    IF cnt >= 4 THEN
        RAISE EXCEPTION 'Teacher % already allocated to % distinct instances in % %', NEW.emp_id, cnt, inst_period, inst_year;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger, fires before insertion
CREATE TRIGGER trg_check_teacher_limit
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
