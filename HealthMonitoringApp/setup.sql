TRUNCATE TABLE my_database.diskspace.server_disk_partitions RESTART IDENTITY CASCADE;

-- Step 1: Create the database
-- You must be connected to the default 'postgres' database to run this
CREATE DATABASE my_database2;

-- Step 2: Connect to the newly created database
\c my_database;

-- Step 3: Create necessary schemas
CREATE SCHEMA IF NOT EXISTS api;
CREATE SCHEMA IF NOT EXISTS db;
CREATE SCHEMA IF NOT EXISTS diskspace;
CREATE SCHEMA IF NOT EXISTS jobs;

-- Step 4: Create tables in respective schemas

-- 4.1 API Status Log Table
CREATE TABLE api.api_status_log (
                                    id BIGSERIAL PRIMARY KEY,
                                    api_name VARCHAR(255),
                                    latency_millis BIGINT,
                                    timestamp TIMESTAMP,
                                    up BOOLEAN,
                                    message TEXT
);

-- 4.2 Database Tablespace Table
CREATE TABLE db.database_tablespace (
                                        id BIGSERIAL PRIMARY KEY,
                                        timestamp TIMESTAMP,
                                        hostname VARCHAR(255),
                                        sid VARCHAR(255),
                                        tablespace_name VARCHAR(255),
                                        free_space_mb BIGINT,
                                        used_space_mb BIGINT,
                                        total_space_mb BIGINT,
                                        usage_pct BIGINT
);

-- 4.3 Server Disk Partitions Table
CREATE TABLE diskspace.server_disk_partitions (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  timestamp TIMESTAMP,
                                                  hostname VARCHAR(255),
                                                  size_mb BIGINT,
                                                  available_mb BIGINT,
                                                  used_mb BIGINT,
                                                  usage_pct BIGINT,
                                                  mounted_on VARCHAR(255),
                                                  filesystem VARCHAR(255)
);

-- 4.4 Job Logs Table
CREATE TABLE jobs.job_logs (
                               id BIGSERIAL PRIMARY KEY,
                               job_name VARCHAR(255),
                               start_time TIMESTAMP,
                               end_time TIMESTAMP,
                               status VARCHAR(255),
                               message VARCHAR(2000)
);
