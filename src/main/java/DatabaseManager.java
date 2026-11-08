import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static String getEnv(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static Connection connect() throws SQLException {
        String url = getEnv(
                "DB_URL",
                "jdbc:mariadb://localhost:3306/assignment6"
        );

        String user = getEnv("DB_USER", "root");
        String password = getEnv("DB_PASSWORD", "");

        return DriverManager.getConnection(url, user, password);
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS conversions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    input_value DOUBLE NOT NULL,
                    input_unit VARCHAR(20) NOT NULL,
                    output_value DOUBLE NOT NULL,
                    output_unit VARCHAR(20) NOT NULL,
                    language VARCHAR(5) NOT NULL DEFAULT 'en',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            statement.executeUpdate("""
                ALTER TABLE conversions
                ADD COLUMN IF NOT EXISTS language
                VARCHAR(5) NOT NULL DEFAULT 'en'
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS measurements (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    conversion_id INT NOT NULL,
                    distance DOUBLE NOT NULL,
                    time_seconds DOUBLE NOT NULL,
                    speed DOUBLE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (conversion_id)
                        REFERENCES conversions(id)
                )
                """);
        }

        System.out.println("MariaDB database and tables are ready.");
    }

    public static void testConnection() throws SQLException {
        try (Connection connection = connect()) {
            if (!connection.isValid(3)) {
                throw new SQLException("MariaDB connection is invalid.");
            }

            System.out.println("Connected to MariaDB successfully!");
        }
    }

    public static int saveConversion(
            double input,
            String inputUnit,
            double output,
            String outputUnit
    ) throws SQLException {
        return saveConversion(input, inputUnit, output, outputUnit, "en");
    }

    public static int saveConversion(
            double input,
            String inputUnit,
            double output,
            String outputUnit,
            String language
    ) throws SQLException {

        String sql = """
            INSERT INTO conversions
                (input_value, input_unit, output_value, output_unit, language)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setDouble(1, input);
            statement.setString(2, inputUnit);
            statement.setDouble(3, output);
            statement.setString(4, outputUnit);
            statement.setString(5, language);

            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Could not retrieve saved conversion ID.");
    }
}