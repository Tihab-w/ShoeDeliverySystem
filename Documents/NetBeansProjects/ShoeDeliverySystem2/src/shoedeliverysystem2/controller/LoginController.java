package shoedeliverysystem2.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import shoedeliverysystem2.utils.DatabaseConnection;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            setStatus("Please fill in all fields", "#ef4444");
            return;
        }

        String sql = "SELECT password, role FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dbPass = rs.getString("password");
                String role = rs.getString("role");

                if (dbPass.equals(pass)) {
                    navigateTo(role, event);
                } else {
                    setStatus("Access Denied: Unverified password.", "#ef4444");
                }
            } else {
                setStatus("Account not found. Please Sign Up.", "#0ea5e9");
            }

        } catch (SQLException e) {
            setStatus("Database Error: Check Connection.", "#ef4444");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            setStatus("Enter details to create account.", "#ef4444");
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'user')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.executeUpdate();
            
            setStatus("Account Created! You can now Login.", "#10b981");
        } catch (SQLException e) {
            setStatus("Username already exists.", "#ef4444");
        }
    }

    private void navigateTo(String role, ActionEvent event) {
        try {
            String fxmlPath = role.equalsIgnoreCase("admin") ? 
                              "/shoedeliverysystem2/view/adminPanel.fxml" : 
                              "/shoedeliverysystem2/view/userPanel.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            
            stage.setScene(scene);
            stage.setFullScreen(true); 
            stage.setFullScreenExitHint(""); 
            stage.show();
            
        } catch (Exception e) {
            setStatus("Error loading dashboard.", "#ef4444");
            e.printStackTrace();
        }
    }

    private void setStatus(String text, String colorHex) {
        errorLabel.setText(text);
        errorLabel.setStyle("-fx-text-fill: " + colorHex + ";");
    }

    @FXML private void handleExit() { System.exit(0); }

    @FXML 
    private void handleMousePressed(MouseEvent event) { 
        xOffset = event.getSceneX(); 
        yOffset = event.getSceneY(); 
    }

    @FXML 
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (!stage.isFullScreen()) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }
}