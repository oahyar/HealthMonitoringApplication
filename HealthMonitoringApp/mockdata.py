import random
import psycopg2
from sqlalchemy import create_engine
import pandas as pd
from datetime import datetime, timedelta
from dotenv import load_dotenv
import os

# Load environment variables from .env
load_dotenv()

# PostgreSQL Configuration
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_NAME = os.getenv("DB_NAME")

# Create PostgreSQL Connection
engine = create_engine(f"postgresql+psycopg2://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}")

# Number of servers and tablespaces per server
NUM_SERVERS = 3
TABLESPACE_NAMES = ["SYSTEM", "SYSAUX", "USERS", "TEMP", "UNDO", "DATA_TS", "INDEX_TS", "APP_TS", "LOGGING_TS"]
DAYS_HISTORY = 30
RECORDS_PER_DAY = 4  # Every 6 hours
SIDS_PER_SERVER = 2  # Each server has 2 different SIDs

# Connect to PostgreSQL and Create Schemas
conn = psycopg2.connect(
    host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD, dbname=DB_NAME
)
conn.autocommit = True
cursor = conn.cursor()

# Create schemas if they don't exist
cursor.execute("CREATE SCHEMA IF NOT EXISTS db;")
conn.commit()

def generate_historical_data():
    tablespace_data = []
    
    start_time = datetime.now() - timedelta(days=DAYS_HISTORY)

    for day in range(DAYS_HISTORY):
        for record in range(RECORDS_PER_DAY):
            timestamp = start_time + timedelta(days=day, hours=record * (24 / RECORDS_PER_DAY))
            
            for server_id in range(1, NUM_SERVERS + 1):
                hostname = f"Server_{server_id}"
                sids = [f"SID_{server_id}_1", f"SID_{server_id}_2"]  # Two SIDs per server

                for sid in sids:
                    # Generate tablespace data per server and SID
                    for tablespace_name in TABLESPACE_NAMES:
                        total_space_mb = random.randint(50, 500)  # MB
                        used_space_mb = random.randint(10, total_space_mb)  # MB
                        free_space_mb = total_space_mb - used_space_mb
                        usage_pct = int((used_space_mb / total_space_mb) * 100)  # Ensuring whole number

                        tablespace_data.append({
                            "timestamp": timestamp,
                            "hostname": hostname,
                            "sid": sid,
                            "tablespace_name": tablespace_name,
                            "free_space_mb": free_space_mb,
                            "used_space_mb": used_space_mb,
                            "total_space_mb": total_space_mb,
                            "usage_pct": usage_pct  # Now a whole number
                        })

    return tablespace_data

# Generate historical data
tablespace_data = generate_historical_data()

# Convert to DataFrame
df_tablespace = pd.DataFrame(tablespace_data)

# Store in SQL database with new schema
df_tablespace.to_sql("database_tablespace", con=engine, schema="db", if_exists="replace", index=False)

print(f"Tablespace mock data for {DAYS_HISTORY} days generated and stored in PostgreSQL successfully!")
