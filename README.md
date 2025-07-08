# HealthMonitoringApplication

**🛠️ PostgreSQL Database Setup**
This project uses PostgreSQL for data persistence. A single SQL script (setup.sql) is provided to initialize the database, schemas, and all necessary tables for the system to function properly.

**📄 Setup Script: setup.sql**
The setup.sql script performs the following tasks:

1. Creates a new database: my_database
2. Switches context to my_database
3. Creates the required schemas:
- api
- db
- diskspace
- jobs

4. Creates the following tables under respective schemas:
- api.api_status_log
- db.database_tablespace
- diskspace.server_disk_partitions
- jobs.job_logs

**⚙️ How to Run the Script**
1. Connect to PostgreSQL
Make sure PostgreSQL is running and accessible.

You must run the script from the default postgres database to create a new database:

```psql -U your_username -d postgres -f setup.sql```

If you encounter permission issues, ensure:
- Your user has CREATEDB privileges.
- You are not already connected to a database you’re trying to recreate.

**🔧 Spring Boot Configuration (application.properties)**
Update your src/main/resources/application.properties to point to the right database:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/my_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=never
```

**✅ Verifying the Setup**
After running the script:
- Tables should be present in their respective schemas.
- You can verify using a GUI like pgAdmin or via psql:

\dt api.*;
\dt db.*;
\dt diskspace.*;
\dt jobs.*;
