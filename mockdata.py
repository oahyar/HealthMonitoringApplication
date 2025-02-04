import random
import psycopg2
from sqlalchemy import create_engine
import pandas as pd
from datetime import datetime, timedelta

# PostgreSQL Configuration
DB_HOST = "localhost"
DB_PORT = "5433"
DB_USER = "postgres"
DB_PASSWORD = "qwer"
DB_NAME = "my_database"

# Create PostgreSQL Connection
engine = create_engine(f"postgresql+psycopg2://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}")

# Number of servers and databases per server
NUM_SERVERS = 3
NUM_DATABASES_PER_SERVER = 2
DAYS_HISTORY = 30  # Number of past days to generate data for
RECORDS_PER_DAY = 4  # How many records per day (e.g., every 6 hours)

# Connect to PostgreSQL and Create Schemas
conn = psycopg2.connect(
    host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD, dbname=DB_NAME
)
conn.autocommit = True
cursor = conn.cursor()

# Create schemas if they don't exist
cursor.execute("CREATE SCHEMA IF NOT EXISTS db;")
cursor.execute("CREATE SCHEMA IF NOT EXISTS diskspace;")
conn.commit()

def generate_historical_data():
    diskspace_data = []
    db_data = []
    
    start_time = datetime.now() - timedelta(days=DAYS_HISTORY)

    for server_id in range(1, NUM_SERVERS + 1):
        server_name = f"Server_{server_id}"

        for day in range(DAYS_HISTORY):
            for record in range(RECORDS_PER_DAY):
                timestamp = start_time + timedelta(days=day, hours=record * (24 / RECORDS_PER_DAY))

                # Generate disk space usage (stored in diskspace schema)
                total_disk_space = random.randint(100, 1000)  # GB
                used_disk_space = random.randint(10, total_disk_space)  # GB
                free_disk_space = total_disk_space - used_disk_space

                # Generate memory usage
                total_memory = random.randint(16, 256)  # GB
                used_memory = random.randint(4, total_memory)  # GB
                available_memory = total_memory - used_memory

                # Generate CPU usage
                cpu_cores = random.randint(2, 16)
                cpu_usage = [random.uniform(1.0, 100.0) for _ in range(cpu_cores)]
                avg_cpu_usage = sum(cpu_usage) / len(cpu_usage)

                # Append to diskspace data
                diskspace_data.append({
                    "timestamp": timestamp,
                    "server_name": server_name,
                    "total_disk_space_gb": total_disk_space,
                    "used_disk_space_gb": used_disk_space,
                    "free_disk_space_gb": free_disk_space,
                    "total_memory_gb": total_memory,
                    "used_memory_gb": used_memory,
                    "available_memory_gb": available_memory,
                    "cpu_cores": cpu_cores,
                    "avg_cpu_usage": round(avg_cpu_usage, 2)
                })

                # Generate database tablespace data (stored in db schema)
                for db_id in range(1, NUM_DATABASES_PER_SERVER + 1):
                    db_name = f"Database_{server_id}_{db_id}"
                    total_tablespace = random.randint(50, 500)  # MB
                    used_tablespace = random.randint(10, total_tablespace)  # MB
                    free_tablespace = total_tablespace - used_tablespace

                    # Append to db data
                    db_data.append({
                        "timestamp": timestamp,
                        "server_name": server_name,
                        "database_name": db_name,
                        "total_tablespace_mb": total_tablespace,
                        "used_tablespace_mb": used_tablespace,
                        "free_tablespace_mb": free_tablespace
                    })

    return diskspace_data, db_data

# Generate historical data
diskspace_data, db_data = generate_historical_data()

# Convert to DataFrames
df_diskspace = pd.DataFrame(diskspace_data)
df_db = pd.DataFrame(db_data)

# Store in SQL database with different schemas
df_diskspace.to_sql("server_metrics", con=engine, schema="diskspace", if_exists="replace", index=False)
df_db.to_sql("database_tablespace", con=engine, schema="db", if_exists="replace", index=False)

print(f"Historical mock data for {DAYS_HISTORY} days generated and stored in PostgreSQL successfully!")
