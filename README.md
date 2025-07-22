# AI Suggestion Service

A Spring Boot application for managing study plans with MySQL database integration.

## Features

- RESTful API for study plan management
- MySQL database integration with Azure
- JPA/Hibernate for database operations
- JSON support for study plans and course outlines

## Database Setup

### Azure MySQL Configuration

The application is configured to connect to Azure MySQL database with the following details:

- **Hostname**: `innovation-db.mysql.database.azure.com`
- **Port**: `3306`
- **Username**: `user`
- **Password**: `{your-password}` (update in application.properties)
- **SSL Mode**: `require`

### Database Schema

The application uses the following table:

```sql
CREATE TABLE study_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_key VARCHAR(255) NOT NULL,
    user_key VARCHAR(255) NOT NULL,
    thread_id VARCHAR(255) NOT NULL,
    study_plan JSON,
    course_outline JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_thread_id (thread_id),
    INDEX idx_course_user (course_key, user_key)
);
```

## Setup Instructions

1. **Update Database Password**
   - Edit `src/main/resources/application.properties`
   - Replace `{your-password}` with your actual Azure MySQL password

2. **Create Database and Table**
   - Connect to your Azure MySQL instance
   - Run the SQL script from `src/main/resources/db/migration/init.sql`

3. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew bootRun
   ```

## API Endpoints

### Get Study Plan by Thread ID
```
GET /study-plan/{threadId}
```

**Response**: Returns the study plan for the specified thread ID

### Create Study Plan
```
POST /study-plan
Content-Type: application/json

{
    "courseKey": "course123",
    "userKey": "user456",
    "threadId": "thread789",
    "studyPlan": {...},
    "courseOutline": {...}
}
```

## Project Structure

```
src/
├── main/
│   ├── java/apex/wiley/com/demo/
│   │   ├── controller/     # REST controllers
│   │   ├── entity/         # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── service/        # Business logic
│   │   └── dto/           # Data transfer objects
│   └── resources/
│       ├── application.properties
│       ├── application-test.properties
│       └── db/migration/   # Database scripts
└── test/                   # Unit tests
```

## Testing

The application uses H2 in-memory database for testing. Tests can be run with:

```bash
./gradlew test
```
