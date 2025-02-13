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

# Number of servers
NUM_SERVERS = 3
DAYS_HISTORY = 30
RECORDS_PER_DAY = 4  # Every 6 hours

# Connect to PostgreSQL and Create Schema
conn = psycopg2.connect(
    host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD, dbname=DB_NAME
)
conn.autocommit = True
cursor = conn.cursor()

# Create schema if it doesn't exist
cursor.execute("CREATE SCHEMA IF NOT EXISTS diskspace;")
conn.commit()

def generate_diskspace_data():
    diskspace_data = []
    
    start_time = datetime.now() - timedelta(days=DAYS_HISTORY)

    for day in range(DAYS_HISTORY):
        for record in range(RECORDS_PER_DAY):
            timestamp = start_time + timedelta(days=day, hours=record * (24 / RECORDS_PER_DAY))
            
            for server_id in range(1, NUM_SERVERS + 1):
                hostname = f"Server_{server_id}"

                # Generate random values for disk space and memory usage
                total_diskspace = random.randint(100000, 1000000)  # MB
                used_diskspace = random.randint(50000, total_diskspace)  # MB
                available_diskspace = total_diskspace - used_diskspace
                usage_percentage_diskspace = int((used_diskspace / total_diskspace) * 100)

                total_memory = random.randint(16000, 256000)  # MB
                used_memory = random.randint(4000, total_memory)  # MB
                available_memory = total_memory - used_memory
                cache_memory = random.randint(2000, 32000)  # MB
                free_memory = available_memory - cache_memory if available_memory > cache_memory else 0

                user_process = random.randint(50, 500)
                system_process = random.randint(20, 200)

                diskspace_data.append({
                    "timestamp": timestamp,
                    "hostname": hostname,
                    "total_diskspace_mb": total_diskspace,
                    "available_diskspace_mb": available_diskspace,
                    "used_diskspace_mb": used_diskspace,
                    "usage_percentage_diskspace": usage_percentage_diskspace,
                    "available_memory_mb": available_memory,
                    "cache_memory_mb": cache_memory,
                    "free_memory_mb": free_memory,
                    "total_memory_mb": total_memory,
                    "used_memory_mb": used_memory,
                })

    return diskspace_data

# Generate historical data
diskspace_data = generate_diskspace_data()

# Convert to DataFrame
df_diskspace = pd.DataFrame(diskspace_data)

# Store in SQL database with new schema
df_diskspace.to_sql("server_metrics", con=engine, schema="diskspace", if_exists="replace", index=False)

print(f"Server disk space mock data for {DAYS_HISTORY} days generated and stored in PostgreSQL successfully!")
