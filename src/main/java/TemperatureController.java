import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class TemperatureController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private ComboBox<String> languageComboBox;

    @FXML private Label temperatureSectionLabel;
    @FXML private ComboBox<MeasurementDAO.UnitInfo> temperatureFromComboBox;
    @FXML private Label fromLabel;
    @FXML private ComboBox<MeasurementDAO.UnitInfo> temperatureToComboBox;
    @FXML private Label toLabel;
    @FXML private Label temperatureFromDescription;
    @FXML private Label temperatureToDescription;
    @FXML private TextField temperatureField;
    @FXML private Button convertButton;
    @FXML private Label resultLabel;

    @FXML private Label speedSectionLabel;
    @FXML private ComboBox<MeasurementDAO.UnitInfo> distanceUnitComboBox;
    @FXML private ComboBox<MeasurementDAO.UnitInfo> timeUnitComboBox;
    @FXML private Label distanceUnitLabel;
    @FXML private Label distanceDescription;
    @FXML private Label timeUnitLabel;
    @FXML private Label timeDescription;
    @FXML private TextField distanceField;
    @FXML private TextField timeField;
    @FXML private Button speedButton;
    @FXML private Label speedResultLabel;
    @FXML private Label historyLabel;

    @FXML private ListView<String> historyListView;

    private final TemperatureConverter converter =
            new TemperatureConverter();

    private final MeasurementDAO measurementDAO =
            new MeasurementDAO();

    private ResourceBundle bundle;
    private Locale currentLocale = Locale.US;
    private String currentLanguage = "en";

    @FXML
    private void initialize() {
        languageComboBox.getItems().setAll(
                "English",
                "Suomi",
                "Svenska",
                "日本語",
                "العربية"
        );

        languageComboBox.getSelectionModel().select(0);

        temperatureFromComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        updateDescription(
                                temperatureFromDescription, newValue
                        ));

        temperatureToComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        updateDescription(
                                temperatureToDescription, newValue
                        ));

        distanceUnitComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        updateDescription(distanceDescription, newValue));

        timeUnitComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        updateDescription(timeDescription, newValue));

        languageComboBox.getSelectionModel()
                .selectedIndexProperty()
                .addListener((observable, oldValue, newValue) -> {
                    int index = newValue == null
                            ? 0 : newValue.intValue();

                    Locale locale = switch (index) {
                        case 1 -> Locale.forLanguageTag("fi-FI");
                        case 2 -> Locale.forLanguageTag("sv-SE");
                        case 3 -> Locale.forLanguageTag("ja-JP");
                        case 4 -> Locale.forLanguageTag("ar");
                        default -> Locale.US;
                    };

                    applyLanguage(locale);
                });

        applyLanguage(Locale.US);
    }

    private void applyLanguage(Locale locale) {
        currentLocale = locale;
        currentLanguage = locale.getLanguage();

        try {
            if ("ar".equals(currentLanguage)) {
                try (InputStream stream =
                             getClass().getResourceAsStream(
                                     "/MessagesBundle_ar.properties")) {

                    if (stream == null) {
                        throw new IllegalStateException(
                                "/MessagesBundle_ar.properties was not found."
                        );
                    }

                    bundle = new PropertyResourceBundle(
                            new InputStreamReader(
                                    stream,
                                    StandardCharsets.UTF_8
                            )
                    );
                }
            } else {
                bundle = ResourceBundle.getBundle(
                        "MessagesBundle",
                        locale
                );
            }

            updateLabels();
            loadLocalizedUnits();
            loadHistory();

            if (root != null) {
                root.setNodeOrientation(
                        "ar".equals(currentLanguage)
                                ? NodeOrientation.RIGHT_TO_LEFT
                                : NodeOrientation.LEFT_TO_RIGHT
                );
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            resultLabel.setText("Could not load language or database data.");
        }
    }

    private void updateLabels() {
        titleLabel.setText(bundle.getString("title"));
        temperatureSectionLabel.setText(
                bundle.getString("temperatureSection"));
        fromLabel.setText(bundle.getString("from"));
        toLabel.setText(bundle.getString("to"));

        temperatureField.setPromptText(
                bundle.getString("temperaturePrompt"));
        convertButton.setText(bundle.getString("convert"));
        resultLabel.setText(bundle.getString("result"));

        speedSectionLabel.setText(
                bundle.getString("speedSection"));
        distanceUnitLabel.setText(bundle.getString("distanceUnit"));
        timeUnitLabel.setText(bundle.getString("timeUnit"));

        distanceField.setPromptText(
                bundle.getString("distancePrompt"));
        timeField.setPromptText(
                bundle.getString("timePrompt"));

        speedButton.setText(bundle.getString("speedButton"));
        speedResultLabel.setText(bundle.getString("speedResult"));
        historyLabel.setText(bundle.getString("history"));
    }

    private void loadLocalizedUnits() throws SQLException {
        List<MeasurementDAO.UnitInfo> allUnits =
                measurementDAO.getLocalizedUnits(currentLanguage);
        System.out.println("Current language: " + currentLanguage);
        System.out.println("Units returned from database: " + allUnits.size());

        for (MeasurementDAO.UnitInfo unit : allUnits) {
            System.out.println(
                    unit.getUnitKey() + " | "
                            + unit.getCategory() + " | "
                            + unit.getName()
            );
        }

        List<MeasurementDAO.UnitInfo> temperatures =
                new ArrayList<>();
        List<MeasurementDAO.UnitInfo> distances =
                new ArrayList<>();
        List<MeasurementDAO.UnitInfo> times =
                new ArrayList<>();

        for (MeasurementDAO.UnitInfo unit : allUnits) {
            switch (unit.getCategory()) {
                case "temperature" -> temperatures.add(unit);
                case "distance" -> distances.add(unit);
                case "time" -> times.add(unit);
                default -> {
                    // No dropdown is needed for other categories.
                }
            }
        }

        temperatureFromComboBox.getItems().setAll(temperatures);
        temperatureToComboBox.getItems().setAll(temperatures);

        distanceUnitComboBox.getItems().setAll(distances);
        timeUnitComboBox.getItems().setAll(times);

        selectUnit(temperatureFromComboBox, "celsius");
        selectUnit(temperatureToComboBox, "fahrenheit");
        selectUnit(distanceUnitComboBox, "kilometer");
        selectUnit(timeUnitComboBox, "hour");
    }

    private void selectUnit(
            ComboBox<MeasurementDAO.UnitInfo> comboBox,
            String key
    ) {
        for (MeasurementDAO.UnitInfo unit : comboBox.getItems()) {
            if (unit.getUnitKey().equals(key)) {
                comboBox.getSelectionModel().select(unit);
                return;
            }
        }
    }

    private void updateDescription(
            Label label,
            MeasurementDAO.UnitInfo unit
    ) {
        if (unit != null) {
            label.setText(unit.getDescription());
        } else {
            label.setText("");
        }
    }

    @FXML
    private void handleConvert() {
        try {
            double input = Double.parseDouble(
                    temperatureField.getText().trim()
            );

            MeasurementDAO.UnitInfo from =
                    temperatureFromComboBox.getValue();

            MeasurementDAO.UnitInfo to =
                    temperatureToComboBox.getValue();

            if (from == null || to == null) {
                resultLabel.setText(bundle.getString("selectTemperatureUnits"));
                return;
            }

            double output = converter.convertTemperature(
                    input,
                    from.getUnitKey(),
                    to.getUnitKey()
            );

            measurementDAO.saveConversion(
                    input,
                    from.getUnitKey(),
                    output,
                    to.getUnitKey(),
                    currentLanguage
            );

            NumberFormat format =
                    NumberFormat.getNumberInstance(currentLocale);
            format.setMaximumFractionDigits(2);

            String message = format.format(input)
                    + " " + from.getName()
                    + " = "
                    + format.format(output)
                    + " " + to.getName();

            double celsius = switch (from.getUnitKey()) {
                case "celsius" -> input;
                case "fahrenheit" ->
                        converter.fahrenheitToCelsius(input);
                case "kelvin" ->
                        converter.kelvinToCelsius(input);
                default -> input;
            };

            if (converter.isExtremeTemperature(celsius)) {
                message += " - " + bundle.getString("extremeWarning");
            }

            resultLabel.setText(message);
            loadHistory();

        } catch (NumberFormatException exception) {
            resultLabel.setText(bundle.getString("invalidNumber"));

        } catch (IllegalArgumentException exception) {
            resultLabel.setText(exception.getMessage());

        } catch (SQLException exception) {
            exception.printStackTrace();
            resultLabel.setText(bundle.getString("databaseError"));
        }
    }

    @FXML
    private void handleSpeed() {
        try {
            double distance = Double.parseDouble(
                    distanceField.getText().trim()
            );

            double timeHours = Double.parseDouble(
                    timeField.getText().trim()
            );

            MeasurementDAO.UnitInfo distanceUnit =
                    distanceUnitComboBox.getValue();

            MeasurementDAO.UnitInfo timeUnit =
                    timeUnitComboBox.getValue();

            if (distanceUnit == null || timeUnit == null) {
                speedResultLabel.setText(bundle.getString("selectSpeedUnits"));
                return;
            }

            if (!"kilometer".equals(distanceUnit.getUnitKey())
                    || !"hour".equals(timeUnit.getUnitKey())) {
                speedResultLabel.setText(bundle.getString("speedUnitRestriction"));
                return;
            }

            if (!Double.isFinite(distance) || distance < 0
                    || !Double.isFinite(timeHours) || timeHours <= 0) {
                throw new IllegalArgumentException(bundle.getString("invalidSpeedInputs"));
            }

            measurementDAO.saveMeasurement(
                    distance,
                    timeHours,
                    currentLanguage
            );

            double speed = distance / timeHours;

            NumberFormat format =
                    NumberFormat.getNumberInstance(currentLocale);
            format.setMaximumFractionDigits(2);

            speedResultLabel.setText(
                    format.format(speed) + " km/h"
            );

            loadHistory();

        } catch (NumberFormatException exception) {
            speedResultLabel.setText(
                    bundle.getString("invalidNumber")
            );

        } catch (IllegalArgumentException exception) {
            speedResultLabel.setText(exception.getMessage());

        } catch (SQLException exception) {
            exception.printStackTrace();
            speedResultLabel.setText(
                    bundle.getString("databaseError")
            );
        }
    }

    private void loadHistory() {
        try {
            historyListView.getItems().setAll(
                    measurementDAO.getRecentConversions()
            );
        } catch (SQLException exception) {
            exception.printStackTrace();
            historyListView.getItems().setAll(bundle.getString("historyError"));
        }
    }
}