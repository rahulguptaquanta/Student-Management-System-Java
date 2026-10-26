import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class StudentManagementSystem {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String DB_URL = System.getenv().getOrDefault("SMS_DB_URL", "jdbc:mysql://localhost:3306/student_management");
    private static final String DB_USER = System.getenv().getOrDefault("SMS_DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("SMS_DB_PASSWORD", "");

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            createTable(connection);
            boolean running = true;
            System.out.println("====================================");
            System.out.println("      STUDENT MANAGEMENT SYSTEM");
            System.out.println("====================================");
            while (running) {
                showMenu();
                int choice = readInt("Choose an option (1-6): ");
                try {
                    switch (choice) {
                        case 1: addStudent(connection); break;
                        case 2: viewStudents(connection); break;
                        case 3: searchStudent(connection); break;
                        case 4: updateStudent(connection); break;
                        case 5: deleteStudent(connection); break;
                        case 6: running = false; System.out.println("Thank you for using Student Management System."); break;
                        default: System.out.println("Invalid choice. Select from 1 to 6.");
                    }
                } catch (SQLException exception) {
                    System.out.println("Database operation failed: " + exception.getMessage());
                }
            }
        } catch (SQLException exception) {
            System.out.println("Unable to connect to MySQL: " + exception.getMessage());
            System.out.println("Check MySQL, database setup, credentials, and Connector/J.");
        } finally {
            SCANNER.close();
        }
    }

    private static void showMenu() {
        System.out.println("\\n--------------- MENU ---------------");
        System.out.println("1. Add student");
        System.out.println("2. View all students");
        System.out.println("3. Search student by ID");
        System.out.println("4. Update student");
        System.out.println("5. Delete student");
        System.out.println("6. Exit");
        System.out.println("------------------------------------");
    }

    private static void createTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students (id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, course VARCHAR(100) NOT NULL, email VARCHAR(150) NOT NULL UNIQUE, marks DECIMAL(5,2) NOT NULL)";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static void addStudent(Connection connection) throws SQLException {
        System.out.println("\\n--- Add Student ---");
        int id = readPositiveInt("Student ID: ");
        String name = readNonEmpty("Student name: ");
        String course = readNonEmpty("Course: ");
        String email = readNonEmpty("Email: ");
        double marks = readMarks("Marks (0-100): ");
        String sql = "INSERT INTO students (id, name, course, email, marks) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setString(3, course);
            statement.setString(4, email);
            statement.setDouble(5, marks);
            statement.executeUpdate();
            System.out.println("Student added successfully.");
        }
    }

    private static void viewStudents(Connection connection) throws SQLException {
        String sql = "SELECT id, name, course, email, marks FROM students ORDER BY id";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            printStudents(resultSet);
        }
    }

    private static void searchStudent(Connection connection) throws SQLException {
        int id = readPositiveInt("Enter student ID: ");
        String sql = "SELECT id, name, course, email, marks FROM students WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    System.out.println("No student found with ID " + id + ".");
                    return;
                }
                printHeader();
                printStudent(resultSet);
            }
        }
    }

    private static void updateStudent(Connection connection) throws SQLException {
        int id = readPositiveInt("Enter student ID to update: ");
        String findSql = "SELECT id, name, course, email, marks FROM students WHERE id = ?";
        try (PreparedStatement findStatement = connection.prepareStatement(findSql)) {
            findStatement.setInt(1, id);
            try (ResultSet resultSet = findStatement.executeQuery()) {
                if (!resultSet.next()) {
                    System.out.println("Student not found.");
                    return;
                }
                String name = readOptional("Name [" + resultSet.getString("name") + "]: ", resultSet.getString("name"));
                String course = readOptional("Course [" + resultSet.getString("course") + "]: ", resultSet.getString("course"));
                String email = readOptional("Email [" + resultSet.getString("email") + "]: ", resultSet.getString("email"));
                double marks = readOptionalMarks("Marks [" + resultSet.getDouble("marks") + "]: ", resultSet.getDouble("marks"));
                String updateSql = "UPDATE students SET name = ?, course = ?, email = ?, marks = ? WHERE id = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                    updateStatement.setString(1, name);
                    updateStatement.setString(2, course);
                    updateStatement.setString(3, email);
                    updateStatement.setDouble(4, marks);
                    updateStatement.setInt(5, id);
                    updateStatement.executeUpdate();
                    System.out.println("Student updated successfully.");
                }
            }
        }
    }

    private static void deleteStudent(Connection connection) throws SQLException {
        int id = readPositiveInt("Enter student ID to delete: ");
        System.out.print("Are you sure you want to delete student " + id + "? (yes/no): ");
        if (!SCANNER.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Delete cancelled.");
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            System.out.println(rows > 0 ? "Student deleted successfully." : "Student not found.");
        }
    }

    private static void printStudents(ResultSet resultSet) throws SQLException {
        boolean hasRows = false;
        printHeader();
        while (resultSet.next()) {
            printStudent(resultSet);
            hasRows = true;
        }
        if (!hasRows) System.out.println("No student records found.");
    }

    private static void printHeader() {
        System.out.printf("%-8s | %-22s | %-18s | %-28s | %6s%n", "ID", "Name", "Course", "Email", "Marks");
        System.out.println("---------------------------------------------------------------------------------------------");
    }

    private static void printStudent(ResultSet resultSet) throws SQLException {
        System.out.printf("%-8d | %-22s | %-18s | %-28s | %6.2f%n",
                resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("course"),
                resultSet.getString("email"), resultSet.getDouble("marks"));
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private static String readOptional(String prompt, String currentValue) {
        System.out.print(prompt);
        String value = SCANNER.nextLine().trim();
        return value.isEmpty() ? currentValue : value;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) return value;
            System.out.println("Value must be greater than zero.");
        }
    }

    private static double readMarks(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(SCANNER.nextLine().trim());
                if (Double.isFinite(value) && value >= 0 && value <= 100) return value;
                System.out.println("Marks must be between 0 and 100.");
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid numeric value.");
            }
        }
    }

    private static double readOptionalMarks(String prompt, double currentValue) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            if (input.isEmpty()) return currentValue;
            try {
                double value = Double.parseDouble(input);
                if (Double.isFinite(value) && value >= 0 && value <= 100) return value;
                System.out.println("Marks must be between 0 and 100.");
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid numeric value.");
            }
        }
    }
}
