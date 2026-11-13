# Corporate Visitor Checkout Reminder

## Requirements

- JDK 17+
- Maven 3.9+
- MySQL 8+

## MySQL setup

1. Open `database/schema.sql` in MySQL Workbench and execute it.
2. Copy `src/main/resources/db.properties.example` to
   `src/main/resources/db.properties`.
3. Set `db.user` and `db.password` to a MySQL account that has access to
   `corporate_visitors`. The application also accepts `DB_URL`, `DB_USER`, and
   `DB_PASSWORD` environment variables.

## Run

```text
mvn clean compile
mvn exec:java
```

The application checks active visits every 30 seconds. When the configured
allowed duration is exceeded, it shows one warning popup per visit and marks
the reminder in MySQL. Checking the visitor out removes it from the active
table.
