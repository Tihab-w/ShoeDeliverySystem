package shoedeliverysystem2.controller;

import shoedeliverysystem2.model.Shoe;
import shoedeliverysystem2.model.Order; 
import shoedeliverysystem2.utils.DatabaseConnection;
import shoedeliverysystem2.utils.AlertHelper;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AdminPanelController implements Initializable {

    @FXML private TableView<Shoe> shoeTable;
    @FXML private TableColumn<Shoe, String> brandCol, styleCol, sizeCol, colorCol;
    @FXML private TableColumn<Shoe, Integer> priceCol, stockCol;
    @FXML private TextField brandField, styleField, sizeField, colorField, priceField, stockField, searchField;

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> orderUserCol, orderDetailsCol, orderTimeCol, deliveryCol;

    private final ObservableList<Shoe> shoeList = FXCollections.observableArrayList();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (shoeTable != null) {
            setupInventoryTable();
            loadInventory();
            setupSearch();
            shoeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) fillFields(newVal);
            });
        }

        if (orderTable != null) {
            setupOrderTable();
            loadOrders();
        }
    }

    private void setupInventoryTable() {
        brandCol.setCellValueFactory(c -> c.getValue().brandProperty());
        styleCol.setCellValueFactory(c -> c.getValue().styleProperty());
        sizeCol.setCellValueFactory(c -> c.getValue().sizeProperty());
        colorCol.setCellValueFactory(c -> c.getValue().colorProperty());
        priceCol.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        stockCol.setCellValueFactory(c -> c.getValue().stockProperty().asObject());
        shoeTable.setItems(shoeList);
    }

    private void setupOrderTable() {
        orderUserCol.setCellValueFactory(c -> c.getValue().customerNameProperty());
        orderDetailsCol.setCellValueFactory(c -> c.getValue().itemsOrderedProperty());
        orderTimeCol.setCellValueFactory(c -> c.getValue().orderTimeProperty());
        deliveryCol.setCellValueFactory(c -> c.getValue().arrivalStatusProperty());
        orderTable.setItems(orderList);
    }

    private void loadOrders() {
        orderList.clear();
        // UPDATED: Query uses your new XAMPP columns
        String query = "SELECT customer_name, items_ordered, order_time, arrival_date FROM orders ORDER BY order_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                orderList.add(new Order(
                    rs.getString("customer_name"),
                    rs.getString("items_ordered"),
                    rs.getString("order_time"),
                    rs.getString("arrival_date") // Maps arrival_date to your model's arrivalStatus
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadInventory() {
        shoeList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM shoes")) {
            while (rs.next()) {
                shoeList.add(new Shoe(rs.getString("brand"), rs.getString("style"), 
                    rs.getString("size"), rs.getString("color"), rs.getInt("price"), rs.getInt("stock")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Shoe> filteredData = new FilteredList<>(shoeList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(shoe -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return shoe.getBrand().toLowerCase().contains(filter) || shoe.getStyle().toLowerCase().contains(filter);
            });
        });
        shoeTable.setItems(filteredData);
    }

    @FXML private void handleAddShoe() {
        if (brandField.getText().isEmpty() || priceField.getText().isEmpty()) return;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO shoes (brand, style, size, color, price, stock) VALUES (?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, brandField.getText());
            ps.setString(2, styleField.getText());
            ps.setString(3, sizeField.getText());
            ps.setString(4, colorField.getText());
            ps.setInt(5, Integer.parseInt(priceField.getText()));
            ps.setInt(6, Integer.parseInt(stockField.getText()));
            ps.executeUpdate();
            loadInventory();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleDeleteShoe() {
        Shoe selected = shoeTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM shoes WHERE brand=? AND style=? AND size=?")) {
            ps.setString(1, selected.getBrand());
            ps.setString(2, selected.getStyle());
            ps.setString(3, selected.getSize());
            ps.executeUpdate();
            loadInventory();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/shoedeliverysystem2/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleExit() { System.exit(0); }

    private void fillFields(Shoe shoe) {
        brandField.setText(shoe.getBrand()); styleField.setText(shoe.getStyle());
        sizeField.setText(shoe.getSize()); colorField.setText(shoe.getColor());
        priceField.setText(String.valueOf(shoe.getPrice())); stockField.setText(String.valueOf(shoe.getStock()));
    }

    private void clearFields() {
        brandField.clear(); styleField.clear(); sizeField.clear();
        colorField.clear(); priceField.clear(); stockField.clear();
    }
}