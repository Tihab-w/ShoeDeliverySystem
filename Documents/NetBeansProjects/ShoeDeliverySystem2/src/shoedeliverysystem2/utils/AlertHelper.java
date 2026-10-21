package shoedeliverysystem2.utils;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AlertHelper {
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // This ensures the popup is styled by the system
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        alert.showAndWait();
    }
}
