import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class MeasurementDAOTest {
    private final MeasurementDAO dao = new MeasurementDAO();

    /*
     * These tests use the MariaDB database configured through:
     * DB_URL, DB_USER and DB_PASSWORD.
     *
     * If MariaDB is unavailable, database integration tests are skipped.
     */
    private boolean isDatabaseAvailable() {
        try (Connection connection = DatabaseManager.connect()) {
            return connection.isValid(3);
        } catch (SQLException exception) {
            return false;
        }
    }

    @Test
    void getLocalizedUnitsFallsBackToEnglishWhenTranslationIsMissing()
            throws SQLException {

        assumeTrue(
                isDatabaseAvailable(),
                "MariaDB is unavailable; skipping database test."
        );

        List<MeasurementDAO.UnitInfo> units =
                dao.getLocalizedUnits("zz");

        assertFalse(units.isEmpty());

        MeasurementDAO.UnitInfo celsius = units.stream()
                .filter(unit -> "celsius".equals(unit.getUnitKey()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Celsius unit was not found.")
                );

        assertEquals("Celsius", celsius.getName());
        assertNotNull(celsius.getDescription());
        assertFalse(celsius.getDescription().isBlank());
    }

    @Test
    void getLocalizedUnitsReturnsFinnishTranslations()
            throws SQLException {

        assumeTrue(
                isDatabaseAvailable(),
                "MariaDB is unavailable; skipping database test."
        );

        List<MeasurementDAO.UnitInfo> units =
                dao.getLocalizedUnits("fi");

        assertFalse(units.isEmpty());

        MeasurementDAO.UnitInfo celsius = units.stream()
                .filter(unit -> "celsius".equals(unit.getUnitKey()))
                .findFirst()
                .orElseThrow();

        assertEquals("Celsius", celsius.getName());
        assertNotNull(celsius.getDescription());
    }

    @Test
    void saveConversionStoresConversionInDatabase()
            throws SQLException {

        assumeTrue(
                isDatabaseAvailable(),
                "MariaDB is unavailable; skipping database test."
        );

        String testUnit = "test_" +
                UUID.randomUUID().toString().substring(0, 8);

        double inputValue = 123.456;
        double outputValue = 789.123;

        try {
            dao.saveConversion(
                    inputValue,
                    testUnit,
                    outputValue,
                    "test_output",
                    "en"
            );

            String sql = """
                    SELECT input_value, output_value, language
                    FROM conversions
                    WHERE input_unit = ?
                    ORDER BY id DESC
                    LIMIT 1
                    """;

            try (Connection connection = DatabaseManager.connect();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, testUnit);

                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(
                            result.next(),
                            "The conversion should be saved."
                    );

                    assertEquals(
                            inputValue,
                            result.getDouble("input_value"),
                            0.001
                    );

                    assertEquals(
                            outputValue,
                            result.getDouble("output_value"),
                            0.001
                    );

                    assertEquals("en", result.getString("language"));
                }
            }
        } finally {
            deleteConversionByInputUnit(testUnit);
        }
    }

    @Test
    void saveMeasurementStoresMeasurementAndRelatedConversion()
            throws SQLException {

        assumeTrue(
                isDatabaseAvailable(),
                "MariaDB is unavailable; skipping database test."
        );

        double distance = 1234.56789;
        double timeHours = 2.5;
        double timeSeconds = timeHours * 3600.0;
        double expectedSpeed = distance / timeHours;

        try {
            dao.saveMeasurement(distance, timeHours, "en");

            String sql = """
                    SELECT m.conversion_id, m.distance,
                           m.time_seconds, m.speed
                    FROM measurements m
                    WHERE m.distance = ?
                      AND m.time_seconds = ?
                    ORDER BY m.id DESC
                    LIMIT 1
                    """;

            try (Connection connection = DatabaseManager.connect();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setDouble(1, distance);
                statement.setDouble(2, timeSeconds);

                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(
                            result.next(),
                            "The measurement should be saved."
                    );

                    assertTrue(result.getInt("conversion_id") > 0);

                    assertEquals(
                            distance,
                            result.getDouble("distance"),
                            0.000001
                    );

                    assertEquals(
                            timeSeconds,
                            result.getDouble("time_seconds"),
                            0.000001
                    );

                    assertEquals(
                            expectedSpeed,
                            result.getDouble("speed"),
                            0.000001
                    );
                }
            }
        } finally {
            deleteMeasurement(distance, timeSeconds);
        }
    }

    @Test
    void saveConversionRejectsNonFiniteValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dao.saveConversion(
                        Double.NaN,
                        "celsius",
                        32.0,
                        "fahrenheit",
                        "en"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.saveConversion(
                        25.0,
                        "celsius",
                        Double.POSITIVE_INFINITY,
                        "fahrenheit",
                        "en"
                )
        );
    }

    @Test
    void saveMeasurementRejectsInvalidDistanceAndTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dao.saveMeasurement(-1.0, 2.0, "en")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.saveMeasurement(
                        Double.NaN,
                        2.0,
                        "en"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.saveMeasurement(10.0, 0.0, "en")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.saveMeasurement(
                        10.0,
                        Double.POSITIVE_INFINITY,
                        "en"
                )
        );
    }

    @Test
    void getRecentConversionsReturnsAtMostFiveEntries()
            throws SQLException {

        assumeTrue(
                isDatabaseAvailable(),
                "MariaDB is unavailable; skipping database test."
        );

        List<String> history = dao.getRecentConversions();

        assertNotNull(history);
        assertTrue(
                history.size() <= 5,
                "The history should contain no more than five entries."
        );
    }

    private void deleteConversionByInputUnit(String inputUnit)
            throws SQLException {

        String sql = """
                DELETE FROM conversions
                WHERE input_unit = ?
                """;

        try (Connection connection = DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, inputUnit);
            statement.executeUpdate();
        }
    }

    private void deleteMeasurement(double distance, double timeSeconds)
            throws SQLException {

        String findSql = """
                SELECT conversion_id
                FROM measurements
                WHERE distance = ? AND time_seconds = ?
                """;

        try (Connection connection = DatabaseManager.connect()) {
            connection.setAutoCommit(false);

            try {
                int conversionId = -1;

                try (PreparedStatement statement =
                             connection.prepareStatement(findSql)) {

                    statement.setDouble(1, distance);
                    statement.setDouble(2, timeSeconds);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            conversionId =
                                    result.getInt("conversion_id");
                        }
                    }
                }

                if (conversionId != -1) {
                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         "DELETE FROM measurements " +
                                                 "WHERE conversion_id = ?"
                                 )) {

                        statement.setInt(1, conversionId);
                        statement.executeUpdate();
                    }

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         "DELETE FROM conversions " +
                                                 "WHERE id = ?"
                                 )) {

                        statement.setInt(1, conversionId);
                        statement.executeUpdate();
                    }
                }

                connection.commit();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}