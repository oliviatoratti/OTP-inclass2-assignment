import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MeasurementDAO {

    public static class UnitInfo {
        private final int id;
        private final String unitKey;
        private final String category;
        private final String name;
        private final String description;

        public UnitInfo(
                int id,
                String unitKey,
                String category,
                String name,
                String description
        ) {
            this.id = id;
            this.unitKey = unitKey;
            this.category = category;
            this.name = name;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getUnitKey() {
            return unitKey;
        }

        public String getCategory() {
            return category;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public List<UnitInfo> getLocalizedUnits(String language)
            throws SQLException {

        String sql = """
                SELECT u.id,
                       u.unit_key,
                       u.category,
                       COALESCE(t.name, en.name) AS localized_name,
                       COALESCE(t.description, en.description)
                           AS localized_description
                FROM units u
                LEFT JOIN unit_translation t
                    ON u.id = t.unit_id AND t.lang = ?
                LEFT JOIN unit_translation en
                    ON u.id = en.unit_id AND en.lang = 'en'
                ORDER BY u.id
                """;

        List<UnitInfo> units = new ArrayList<>();

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, language);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    units.add(new UnitInfo(
                            result.getInt("id"),
                            result.getString("unit_key"),
                            result.getString("category"),
                            result.getString("localized_name"),
                            result.getString("localized_description")
                    ));
                }
            }
        }

        return units;
    }

    public void saveConversion(
            double inputValue,
            String inputUnit,
            double outputValue,
            String outputUnit,
            String language
    ) throws SQLException {

        if (!Double.isFinite(inputValue)
                || !Double.isFinite(outputValue)) {
            throw new IllegalArgumentException(
                    "Conversion values must be finite."
            );
        }

        String sql = """
                INSERT INTO conversions
                    (input_value, input_unit, output_value,
                     output_unit, language)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setDouble(1, inputValue);
            statement.setString(2, inputUnit);
            statement.setDouble(3, outputValue);
            statement.setString(4, outputUnit);
            statement.setString(5, language);

            statement.executeUpdate();
        }
    }

    public void saveMeasurement(double distance, double timeHours)
            throws SQLException {

        saveMeasurement(distance, timeHours, "en");
    }

    public void saveMeasurement(
            double distance,
            double timeHours,
            String language
    ) throws SQLException {

        if (!Double.isFinite(distance) || distance < 0) {
            throw new IllegalArgumentException(
                    "Distance must be a finite, non-negative number."
            );
        }

        if (!Double.isFinite(timeHours) || timeHours <= 0) {
            throw new IllegalArgumentException(
                    "Time must be a positive, finite number."
            );
        }

        double speed = distance / timeHours;
        double timeSeconds = timeHours * 3600.0;

        try (Connection connection = DatabaseManager.connect()) {

            connection.setAutoCommit(false);

            try {
                int conversionId;

                String conversionSql = """
                        INSERT INTO conversions
                            (input_value, input_unit, output_value,
                             output_unit, language)
                        VALUES (?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     conversionSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setDouble(1, distance);
                    statement.setString(2, "kilometer");
                    statement.setDouble(3, speed);
                    statement.setString(4, "km_per_hour");
                    statement.setString(5, language);

                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not retrieve conversion ID."
                            );
                        }

                        conversionId = keys.getInt(1);
                    }
                }

                String measurementSql = """
                        INSERT INTO measurements
                            (conversion_id, distance, time_seconds, speed)
                        VALUES (?, ?, ?, ?)
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(measurementSql)) {

                    statement.setInt(1, conversionId);
                    statement.setDouble(2, distance);
                    statement.setDouble(3, timeSeconds);
                    statement.setDouble(4, speed);

                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<String> getRecentConversions() throws SQLException {

        String sql = """
                SELECT input_value,
                       input_unit,
                       output_value,
                       output_unit,
                       language,
                       created_at
                FROM conversions
                ORDER BY id DESC
                LIMIT 5
                """;

        List<String> history = new ArrayList<>();

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                double input = result.getDouble("input_value");
                String inputUnit = result.getString("input_unit");
                double output = result.getDouble("output_value");
                String outputUnit = result.getString("output_unit");
                String language = result.getString("language");

                history.add(String.format(
                        "%s: %.2f %s -> %.2f %s (%s)",
                        result.getTimestamp("created_at"),
                        input,
                        inputUnit,
                        output,
                        outputUnit,
                        language
                ));
            }
        }

        return history;
    }
}