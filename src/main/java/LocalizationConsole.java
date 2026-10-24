import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class LocalizationConsole {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TemperatureConverter converter = new TemperatureConverter();

        System.out.println("Select your language:");
        System.out.println("1. English");
        System.out.println("2. Suomi");
        System.out.println("3. Svenska");
        System.out.println("4. 日本語");

        String language = scanner.nextLine().trim();
        Locale locale;
        switch (language) {
            case "2":
                locale = Locale.forLanguageTag("fi-FI");
                break;
            case "3":
                locale = Locale.forLanguageTag("sv-SE");
                break;
            case "4":
                locale = Locale.forLanguageTag("ja-JP");
                break;
            default:
                locale = Locale.forLanguageTag("en-US");
        }

        ResourceBundle messages =
                ResourceBundle.getBundle("MessagesBundle", locale);

        System.out.println(messages.getString("welcome"));

        while (true) {

            System.out.println();
            System.out.println(messages.getString("menu"));
            System.out.println(messages.getString("option1"));
            System.out.println(messages.getString("option2"));
            System.out.println(messages.getString("option3"));
            System.out.println(messages.getString("option0"));

            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println(messages.getString("goodbye"));
                break;
            }

            if (!choice.equals("1")
                    && !choice.equals("2")
                    && !choice.equals("3")) {
                System.out.println(messages.getString("invalidOption"));
                continue;
            }

            System.out.println(messages.getString("enterTemperature"));
            double temperature;

            try {
                temperature = Double.parseDouble(
                        scanner.nextLine().trim().replace(',', '.')
                );
            } catch (NumberFormatException e) {
                System.out.println(messages.getString("invalidNumber"));
                continue;
            }

            switch (choice) {
                case "1":
                    System.out.println(
                            messages.getString("result") + " " +
                                    converter.fahrenheitToCelsius(temperature) + " °C"
                    );
                    break;

                case "2":
                    System.out.println(
                            messages.getString("result") + " " +
                                    converter.celsiusToFahrenheit(temperature) + " °F"
                    );
                    break;

                case "3":
                    boolean extreme =
                            converter.isExtremeTemperature(temperature);
                    System.out.println(messages.getString(
                            extreme ? "extreme" : "normal"
                    ));
                    break;
            }
        }
        scanner.close();
    }
}