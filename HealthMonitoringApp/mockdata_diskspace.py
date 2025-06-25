import random
import psycopg2
import os
import itertools  # Import cycle for repeating filesystem types
import pandas as pd
from datetime import datetime, timedelta
from dotenv import load_dotenv
from sqlalchemy import create_engine

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

# Number of servers and partitions per server
NUM_SERVERS = 3
DISK_PARTITIONS = ["/", "/home", "/var", "/tmp"]  # Simulated partitions
FILESYSTEM_TYPES = ["ext4", "xfs", "btrfs"]  # Randomly assigned filesystems
DAYS_HISTORY = 3
RECORDS_PER_DAY = 2  # Every 6 hours

# Connect to PostgreSQL and Create Schema
conn = psycopg2.connect(
    host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD, dbname=DB_NAME
)
conn.autocommit = True
cursor = conn.cursor()

# Create schemas if they don't exist
cursor.execute("CREATE SCHEMA IF NOT EXISTS diskspace;")
conn.commit()


def generate_disk_partitions():
    """Generates individual disk partition data for each server with unique timestamps."""
    disk_partitions = []
    
    start_time = datetime.now() - timedelta(days=DAYS_HISTORY)

    for day in range(DAYS_HISTORY):
        for record in range(RECORDS_PER_DAY):
            base_timestamp = start_time + timedelta(days=day, hours=record * (24 / RECORDS_PER_DAY))

            for server_id in range(1, NUM_SERVERS + 1):
                hostname = f"Server_{server_id}"

                # Ensure enough filesystems by cycling through the list
                assigned_filesystems = list(itertools.islice(itertools.cycle(FILESYSTEM_TYPES), len(DISK_PARTITIONS)))

                for index, mount_point in enumerate(DISK_PARTITIONS):
                    total_size_mb = random.randint(50000, 500000)  # MB
                    used_size_mb = random.randint(10000, total_size_mb)  # MB
                    available_size_mb = total_size_mb - used_size_mb
                    usage_pct = int((used_size_mb / total_size_mb) * 100)
                    filesystem = assigned_filesystems[index]  # Unique per mount point
                    
                    # Slight timestamp variation
                    varied_timestamp = base_timestamp + timedelta(seconds=random.randint(1, 59))

                    disk_partitions.append({
                        "timestamp": varied_timestamp,
                        "hostname": hostname,
                        "size_mb": total_size_mb,
                        "available_mb": available_size_mb,
                        "used_mb": used_size_mb,
                        "usage_pct": usage_pct,
                        "mounted_on": mount_point,
                        "filesystem": filesystem
                    })

    return disk_partitions


# Generate historical data
disk_partitions_data = generate_disk_partitions()

# Convert to DataFrame
df_disk_partitions = pd.DataFrame(disk_partitions_data)

# Store in SQL database with appropriate schema
df_disk_partitions.to_sql("server_disk_partitions", con=engine, schema="diskspace", if_exists="replace", index=False)

print(f"Server and partition disk space mock data for {DAYS_HISTORY} days generated and stored in PostgreSQL successfully!")
