import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TemperatureConverterTest {
    private final TemperatureConverter converter =
            new TemperatureConverter();
    @Test
    void convertsCelsiusToFahrenheit() {
        assertEquals(32.0, converter.celsiusToFahrenheit(0), 0.0001);
    }
    @Test
    void convertsFahrenheitToCelsius() {
        assertEquals(0.0, converter.fahrenheitToCelsius(32), 0.0001);
    }
    @Test
    void detectsExtremeTemperatures() {
        assertTrue(converter.isExtremeTemperature(-41));
        assertTrue(converter.isExtremeTemperature(51));
        assertFalse(converter.isExtremeTemperature(20));
        assertFalse(converter.isExtremeTemperature(-40));
        assertFalse(converter.isExtremeTemperature(50));
    }
}