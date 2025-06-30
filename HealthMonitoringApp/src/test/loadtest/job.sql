INSERT INTO jobs.job_logs (
    job_name,
    start_time,
    end_time,
    status,
    message
)
SELECT
    CONCAT('job-', n, '-test'),  -- clearly marked as test job
    NOW() - INTERVAL '1 hour' * (n % 24),
    NOW() - INTERVAL '1 hour' * (n % 24) + INTERVAL '5 minutes',
    CASE
    WHEN n % 5 = 0 THEN 'FAILED'
    WHEN n % 5 = 1 THEN 'COMPLETED'
    WHEN n % 5 = 2 THEN 'RUNNING'
    ELSE 'PENDING'
END,
    CONCAT('This is a mock log for job-', n, ' -test')
FROM generate_series(1, 10000) AS n;

INSERT INTO jobs.job_logs (
    job_name,
    start_time,
    end_time,
    status,
    message
)
SELECT
    CONCAT('job-', n, '-test'),  -- clearly marked test data
    NOW() - INTERVAL '1 hour' * (n % 48),
    NOW() - INTERVAL '1 hour' * (n % 48) + INTERVAL '5 minutes',
    CASE
        WHEN n % 5 = 0 THEN 'FAILED'
        WHEN n % 5 = 1 THEN 'COMPLETED'
        WHEN n % 5 = 2 THEN 'RUNNING'
        ELSE 'PENDING'
        END,
    CONCAT('This is a mock log for job-', n, ' -test')
FROM generate_series(1, 100000) AS n;

INSERT INTO jobs.job_logs (
    job_name,
    start_time,
    end_time,
    status,
    message
)
SELECT
    CONCAT('job-', n, '-test'),  -- test marker in job_name
    NOW() - INTERVAL '1 hour' * (n % 48),
    NOW() - INTERVAL '1 hour' * (n % 48) + INTERVAL '5 minutes',
    CASE
        WHEN n % 5 = 0 THEN 'FAILED'
        WHEN n % 5 = 1 THEN 'COMPLETED'
        WHEN n % 5 = 2 THEN 'RUNNING'
        ELSE 'PENDING'
        END,
    CONCAT('This is a mock log for job-', n, ' -test')
FROM generate_series(1, 500000) AS n;


DELETE FROM jobs.job_logs
WHERE job_name LIKE '%-test';
