package shoedeliverysystem2.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import shoedeliverysystem2.utils.DatabaseConnection;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label totalOrdersLabel, totalRevenueLabel, pendingOrdersLabel;
    @FXML private BarChart<String, Number> salesChart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> {
            loadStatistics();
            loadChartData();
        });
    }

    private void loadStatistics() {
        // UPDATED: Query counts 'id' and joins with shoes table to get revenue
        String statsSql = "SELECT COUNT(o.id) as total_count, SUM(s.price) as total_sum " +
                         "FROM orders o JOIN shoes s ON o.items_ordered LIKE CONCAT(s.brand, ' ', s.style, '%')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(statsSql)) {
            if (rs.next()) {
                totalOrdersLabel.setText(String.valueOf(rs.getInt("total_count")));
                totalRevenueLabel.setText(String.format("%.2f ETB", rs.getDouble("total_sum")));
            }
            pendingOrdersLabel.setText("N/A"); // Based on your new table structure
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadChartData() {
        salesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Orders by Item");

        // UPDATED: Using items_ordered column
        String chartSql = "SELECT items_ordered, COUNT(*) as item_count FROM orders GROUP BY items_ordered";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(chartSql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("items_ordered"), rs.getInt("item_count")));
            }
            if (!series.getData().isEmpty()) salesChart.getData().add(series);
        } catch (SQLException e) { e.printStackTrace(); }
    }
}