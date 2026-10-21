package shoedeliverysystem2.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminOrderController implements Initializable {

    @FXML private TableView<?> ordersTable; // Replace ? with your Order model later
    @FXML private TableColumn<?, ?> orderIdCol, customerCol, shoeCol, quantityCol, totalCol, statusCol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Logic to load orders from database goes here
        System.out.println("Orders Screen Initialized");
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/shoedeliverysystem2/view/adminPanel.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCompleteOrder() {
        System.out.println("Order marked as complete");
    }

    @FXML
    private void handleCancelOrder() {
        System.out.println("Order cancelled");
    }
}