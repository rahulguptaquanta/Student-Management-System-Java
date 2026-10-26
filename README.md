# Student Management System (Java + MySQL)

Week 4 capstone project using Java, OOP, JDBC, MySQL, CRUD operations, input validation, and exception handling.

## Features
- Add, view, search, update, and delete students
- Store student ID, name, course, email, and marks
- Persistent storage using MySQL
- Input validation for IDs and marks
- Prepared statements for database operations

## Requirements
- JDK 8 or newer
- MySQL Server
- MySQL Connector/J

## Database setup
Run the commands in `database.sql` using MySQL. The Java application creates the `students` table if it does not exist.

Default connection:
- URL: `jdbc:mysql://localhost:3306/student_management`
- User: `root`
- Password: empty

You can configure `SMS_DB_URL`, `SMS_DB_USER`, and `SMS_DB_PASSWORD` environment variables for your local setup. Do not upload real passwords to a public repository.

## Run from terminal
Place the MySQL Connector/J JAR in this folder.

Windows:
```bash
javac -cp ".;mysql-connector-j-<version>.jar" StudentManagementSystem.java
java -cp ".;mysql-connector-j-<version>.jar" StudentManagementSystem
```

macOS/Linux:
```bash
javac -cp ".:mysql-connector-j-<version>.jar" StudentManagementSystem.java
java -cp ".:mysql-connector-j-<version>.jar" StudentManagementSystem
```

Replace `<version>` with the version in your JAR filename.

## Test checklist
1. Add a student with a unique ID and email.
2. View all students.
3. Search for that student.
4. Update a field.
5. Delete a test record.
6. Restart the program and confirm records remain in MySQL.

## Concepts
JDBC, `Connection`, `PreparedStatement`, `ResultSet`, CRUD, OOP, input validation, and exception handling.

This is an educational console application and requires a running MySQL server plus MySQL Connector/J.
