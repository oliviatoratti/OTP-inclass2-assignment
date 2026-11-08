public class TemperatureConverter {

    public double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    public double celsiusToFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    public double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    public double kelvinToCelsius(double kelvin) {
        if (kelvin < 0) {
            throw new IllegalArgumentException(
                    "Kelvin cannot be below absolute zero."
            );
        }
        return kelvin - 273.15;
    }

    public double fahrenheitToKelvin(double fahrenheit) {
        return celsiusToKelvin(fahrenheitToCelsius(fahrenheit));
    }

    public double kelvinToFahrenheit(double kelvin) {
        return celsiusToFahrenheit(kelvinToCelsius(kelvin));
    }

    public double convertTemperature(
            double value,
            String fromUnit,
            String toUnit
    ) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Temperature must be finite.");
        }

        double celsius;

        switch (fromUnit.toLowerCase()) {
            case "celsius" -> celsius = value;
            case "fahrenheit" -> celsius = fahrenheitToCelsius(value);
            case "kelvin" -> celsius = kelvinToCelsius(value);
            default -> throw new IllegalArgumentException(
                    "Unknown input temperature unit: " + fromUnit
            );
        }

        return switch (toUnit.toLowerCase()) {
            case "celsius" -> celsius;
            case "fahrenheit" -> celsiusToFahrenheit(celsius);
            case "kelvin" -> celsiusToKelvin(celsius);
            default -> throw new IllegalArgumentException(
                    "Unknown output temperature unit: " + toUnit
            );
        };
    }

    public boolean isExtremeTemperature(double celsius) {
        return celsius < -40 || celsius > 50;
    }
}