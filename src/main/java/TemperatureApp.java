import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class TemperatureApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            DatabaseManager.initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Could not initialize MariaDB:");
            e.printStackTrace();
        }

        Locale.setDefault(Locale.US);
        ResourceBundle bundle =
                ResourceBundle.getBundle("MessagesBundle", Locale.US);
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/temperature.fxml"),
                bundle
        );

        Scene scene = new Scene(loader.load(), 500, 500);
        stage.setTitle(bundle.getString("title"));
        stage.setMinWidth(450);
        stage.setMinHeight(450);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}