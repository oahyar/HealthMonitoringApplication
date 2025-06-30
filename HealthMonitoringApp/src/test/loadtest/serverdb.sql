-- Insert 100k rows into table for stress testing for server and tablespace

INSERT INTO diskspace.server_disk_partitions (
    timestamp,
    hostname,
    filesystem,
    mounted_on,
    size_mb,
    used_mb,
    available_mb,
    usage_pct
)
SELECT
        NOW() - (interval '1 minute' * n),
        CONCAT('server-', n % 100, '-test'),
        CONCAT('/dev/sd', CHR(65 + (n % 5))),
        CONCAT('/mnt/vol', n % 10),
        100000 + n,
        50000 + n,
        50000,
        ROUND(((50000 + n) * 100.0) / (100000 + n))
FROM generate_series(1, 500000) AS n;

INSERT INTO db.database_tablespace (
    timestamp,
    hostname,
    sid,
    tablespace_name,
    free_space_mb,
    used_space_mb,
    total_space_mb,
    usage_pct
)
SELECT
        NOW() - (interval '1 minute' * n),
        CONCAT('db-server-', n % 100, '-test'),
        CONCAT('ORCL', n % 5),
        CONCAT('TS_', n % 10),
        1000 + n,
        2000 + n,
        3000 + n,
        ROUND(((2000 + n) * 100.0) / (3000 + n))
FROM generate_series(1, 500000) AS n;

DELETE FROM diskspace.server_disk_partitions WHERE hostname LIKE '%-test';
DELETE FROM db.database_tablespace WHERE hostname LIKE '%-test';

