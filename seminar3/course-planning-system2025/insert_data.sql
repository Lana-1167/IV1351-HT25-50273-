-- insert_data.sql
-- Assumes create_database.sql already executed

-- Departments
INSERT INTO Department(dept_name) VALUES ('Computer Science'), ('Mathematics');

-- Employees (explicit emp_id to control values)
INSERT INTO Employee(emp_id, first_name, last_name, email, phone, designation, salary, dept_id) VALUES
(500001,'Paris','Carbone','paris.carbone@uni.se','070-111','Ass. Professor',550000,1),
(500004,'Leif','Lindbäck','leif.linback@uni.se','070-222','Lecturer',480000,1),
(500009,'Niharika','Gauraha','niharika.g@uni.se','070-333','Lecturer',440000,1),
(500010,'Brian','Karlsson','brian.k@uni.se','070-444','PhD Student',240000,1),
(500011,'Adam','West','adam.w@uni.se','070-555','TA',200000,1),
(500100,'Test','TeacherOverload','test.overload@uni.se','070-999','Lecturer',420000,1);

-- Department managers
UPDATE Department SET manager_id = 500001 WHERE dept_id = 1;
UPDATE Department SET manager_id = 500004 WHERE dept_id = 2;

-- Course layouts (versions)
INSERT INTO CourseLayout(course_code, version_no, course_name, credits, min_students, max_students, valid_from)
VALUES
('IV1351', 1, 'Data Storage Paradigms', 7.5, 50, 250, '2023-01-01'),
('IX1500', 1, 'Discrete Mathematics', 7.5, 50, 150, '2023-01-01');

-- Course instances (year 2025 examples)
INSERT INTO CourseInstance(layout_id, course_code, version_no, period, year, num_students)
VALUES
((SELECT layout_id FROM CourseLayout WHERE course_code='IV1351' LIMIT 1),'IV1351',1,'P2',2025,200),
((SELECT layout_id FROM CourseLayout WHERE course_code='IX1500' LIMIT 1),'IX1500',1,'P1',2025,150),
((SELECT layout_id FROM CourseLayout WHERE course_code='IV1351' LIMIT 1),'IV1351',1,'P1',2025,50),
((SELECT layout_id FROM CourseLayout WHERE course_code='IX1500' LIMIT 1),'IX1500',1,'P2',2025,40);

-- Activity types (Lecture, Lab, Tutorial, Seminar, Exam, Admin)
INSERT INTO ActivityType(activity_name, factor) VALUES
('Lecture', 3.6), ('Lab', 2.4), ('Tutorial', 2.4), ('Seminar', 1.8), ('Exam', 1.0), ('Admin', 1.0);

-- PlannedActivity (planned hours for each activity on instances)
-- We'll retrieve instance ids for the two first instances to use in examples
-- find instance id ordering if needed; here we use SELECT to insert properly

-- find instance ids
-- For clarity assume:
-- instance_id 1 -> IV1351 P2 (200 students)
-- instance_id 2 -> IX1500 P1 (150 students)
-- (if serial generated differently, adjust - but insert uses SELECTs below for safety)

-- Insert planned activities for IV1351 (instance 1)
INSERT INTO PlannedActivity(instance_id, activity_id, planned_hours)
SELECT ci.instance_id, at.activity_id, vals.h
FROM CourseInstance ci
CROSS JOIN ActivityType at
JOIN (VALUES ('Lecture',20.0), ('Lab',80.0), ('Tutorial',40.0), ('Seminar',80.0), ('Other',650.0)) AS vals(name,h) ON (vals.name = at.activity_name OR (vals.name='Other' AND at.activity_name='Admin')) 
WHERE ci.course_code='IV1351' AND ci.period='P2' AND ci.year=2025
  AND at.activity_name IN ('Lecture','Lab','Tutorial','Seminar','Admin');

-- Insert planned activities for IX1500 (instance 2)
INSERT INTO PlannedActivity(instance_id, activity_id, planned_hours)
SELECT ci.instance_id, at.activity_id, vals.h
FROM CourseInstance ci
CROSS JOIN ActivityType at
JOIN (VALUES ('Lecture',44.0), ('Seminar',64.0), ('Other',200.0)) AS vals(name,h) ON (vals.name = at.activity_name OR (vals.name='Other' AND at.activity_name='Admin'))
WHERE ci.course_code='IX1500' AND ci.period='P1' AND ci.year=2025
  AND at.activity_name IN ('Lecture','Seminar','Admin');

-- Allocations (actual allocations) - create some example allocations
-- Map employees to instance 1 (IV1351 P2)
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
SELECT 500001, ci.instance_id, at.activity_id, CASE at.activity_name
    WHEN 'Lecture' THEN 20
    WHEN 'Seminar' THEN 0
    WHEN 'Lab' THEN 0
    WHEN 'Tutorial' THEN 0
    ELSE 0 END
FROM CourseInstance ci JOIN ActivityType at ON at.activity_name='Lecture'
WHERE ci.course_code='IV1351' AND ci.period='P2' AND ci.year=2025
LIMIT 1;

-- add more allocations (explicit)
-- For IV1351 P2: Leif allocated to Seminar 64h, Niharika also 64h, Brian lab 50, Adam tutorial 50
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
SELECT 500004, ci.instance_id, at.activity_id, 64
FROM CourseInstance ci JOIN ActivityType at ON at.activity_name='Seminar'
WHERE ci.course_code='IV1351' AND ci.period='P2' AND ci.year=2025;

INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
SELECT 500009, ci.instance_id, at.activity_id, 64
FROM CourseInstance ci JOIN ActivityType at ON at.activity_name='Seminar'
WHERE ci.course_code='IV1351' AND ci.period='P2' AND ci.year=2025;

INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
SELECT 500010, ci.instance_id, at.activity_id, 50
FROM CourseInstance ci JOIN ActivityType at ON at.activity_name='Lab'
WHERE ci.course_code='IV1351' AND ci.period='P2' AND ci.year=2025;

INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
SELECT 500011, ci.instance_id, at.activity_id, 50
FROM CourseInstance ci JOIN ActivityType at ON at.activity_name='Tutorial'
WHERE ci.course_code='IV1351' AND ci.period='P2' AND ci.year=2025;

-- For IX1500 P1 allocations (Niharika does lectures)
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
SELECT 500009, ci.instance_id, at.activity_id, 44
FROM CourseInstance ci JOIN ActivityType at ON at.activity_name='Lecture'
WHERE ci.course_code='IX1500' AND ci.period='P1' AND ci.year=2025;

-- Make emp_id 500100 already allocated to 4 distinct instances to demonstrate exceed-limit
-- pick four existing instance_ids (first four created)
INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours)
SELECT 500100, instance_id, (SELECT activity_id FROM ActivityType WHERE activity_name='Lecture'), 10
FROM CourseInstance
WHERE year=2025
ORDER BY instance_id
LIMIT 4;
