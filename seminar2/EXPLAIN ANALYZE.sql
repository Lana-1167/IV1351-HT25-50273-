EXPLAIN ANALYZE
SELECT 
    cl.course_code,
    ROUND(SUM(pa.planned_hours * at.factor), 2) AS planned_total,
    ROUND(SUM(a.allocated_hours * at.factor), 2) AS actual_total
FROM CourseInstance ci
JOIN CourseLayout cl ON ci.course_code = cl.course_code
JOIN PlannedActivity pa ON ci.instance_id = pa.instance_id
JOIN ActivityType at ON pa.activity_id = at.activity_id
LEFT JOIN Allocation a ON ci.instance_id = a.instance_id AND pa.activity_id = a.activity_id
GROUP BY cl.course_code;
