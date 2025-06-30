INSERT INTO api.api_status_log (
    api_name,
    latency_millis,
    timestamp,
    up,
    message
)
SELECT
    CONCAT('api-', n, '-test'),
    100 + (n % 400),  -- Simulated latency
    NOW() - INTERVAL '1 minute' * n,
    CASE WHEN n % 5 = 0 THEN FALSE ELSE TRUE END,
    CONCAT('Test API log message for api-', n)
FROM generate_series(1, 10000) AS n;

INSERT INTO api.api_status_log (
    api_name,
    latency_millis,
    timestamp,
    up,
    message
)
SELECT
    CONCAT('api-', n, '-test'),
    100 + (n % 500),
    NOW() - INTERVAL '1 minute' * n,
    CASE WHEN n % 7 = 0 THEN FALSE ELSE TRUE END,
    CONCAT('Test API log message for api-', n)
FROM generate_series(1, 100000) AS n;

INSERT INTO api.api_status_log (
    api_name,
    latency_millis,
    timestamp,
    up,
    message
)
SELECT
    CONCAT('api-', n, '-test'),
    100 + (n % 500),
    NOW() - INTERVAL '1 minute' * n,
    CASE WHEN n % 7 = 0 THEN FALSE ELSE TRUE END,
    CONCAT('Test API log message for api-', n)
FROM generate_series(100001, 600000) AS n;

DELETE FROM api.api_status_log
WHERE api_name LIKE '%-test';
