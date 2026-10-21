package shoedeliverysystem2.controller;

import shoedeliverysystem2.model.Shoe;
import shoedeliverysystem2.utils.DatabaseConnection;
import shoedeliverysystem2.utils.AlertHelper;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class UserPanelController implements Initializable {

    @FXML private TableView<Shoe> userTable;
    @FXML private TableColumn<Shoe, String> brandCol, styleCol, sizeCol, colorCol;
    @FXML private TableColumn<Shoe, Integer> priceCol, stockCol;
    @FXML private TextField searchField;
    @FXML private Label welcomeLabel;

    private final ObservableList<Shoe> shoeList = FXCollections.observableArrayList();
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        loadInventory();
        setupSearch(); 
    }

    private void setupTable() {
        brandCol.setCellValueFactory(c -> c.getValue().brandProperty());
        styleCol.setCellValueFactory(c -> c.getValue().styleProperty());
        sizeCol.setCellValueFactory(c -> c.getValue().sizeProperty());
        colorCol.setCellValueFactory(c -> c.getValue().colorProperty());
        priceCol.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        stockCol.setCellValueFactory(c -> c.getValue().stockProperty().asObject());
        userTable.setItems(shoeList);
    }

    private void loadInventory() {
        shoeList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM shoes")) {
            while (rs.next()) {
                shoeList.add(new Shoe(rs.getString("brand"), rs.getString("style"), 
                        rs.getString("size"), rs.getString("color"), 
                        rs.getInt("price"), rs.getInt("stock")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Shoe> filteredData = new FilteredList<>(shoeList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(shoe -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return shoe.getBrand().toLowerCase().contains(filter) || 
                       shoe.getStyle().toLowerCase().contains(filter);
            });
        });
        userTable.setItems(filteredData);
    }

    @FXML
    private void handleBuyNow() {
        Shoe selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a shoe from the inventory.");
            return;
        }

        if (selected.getStock() <= 0) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Out of Stock", "Sorry, this shoe is currently sold out.");
            return;
        }

        // --- Step 1: Delivery Dialog ---
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Delivery Details");
        dialog.setHeaderText("STEP 1: SHIPPING INFORMATION");
        
        ButtonType nextBtn = new ButtonType("CONTINUE TO PAYMENT", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(nextBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(25);
        grid.setPadding(new Insets(40, 60, 40, 40));
        grid.setAlignment(Pos.CENTER);

        TextField addr = new TextField(); 
        addr.setPromptText("City, Sub-city, House No.");
        addr.setPrefSize(400, 45);

        TextField phne = new TextField(); 
        phne.setPromptText("09... or 07...");
        phne.setPrefSize(400, 45);
        
        grid.add(new Label("Address:"), 0, 0); grid.add(addr, 1, 0);
        grid.add(new Label("Phone:"), 0, 1); grid.add(phne, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        applyLargeDialogStyle(dialog.getDialogPane(), 700, 450);

        dialog.setResultConverter(btn -> btn == nextBtn ? Arrays.asList(addr.getText(), phne.getText()) : null);
        Optional<List<String>> result = dialog.showAndWait();

        result.ifPresent(details -> {
            if(details.get(0).trim().isEmpty() || details.get(1).trim().isEmpty()){
                 AlertHelper.showAlert(Alert.AlertType.ERROR, "Missing Info", "Address and Phone are required.");
                 return;
            }

            // --- Step 2: Payment Dialog ---
            List<String> payments = Arrays.asList("Telebirr", "CBE Birr", "M-PESA", "HelloCash", "Bank Transfer");
            ChoiceDialog<String> payDialog = new ChoiceDialog<>("Telebirr", payments);
            payDialog.setTitle("Payment Method");
            payDialog.setHeaderText("STEP 2: SELECT PAYMENT METHOD");
            applyLargeDialogStyle(payDialog.getDialogPane(), 600, 400);
            
            Optional<String> method = payDialog.showAndWait();

            method.ifPresent(selectedMethod -> {
                LocalDate arrivalDate = LocalDate.now().plusDays(3);
                String formattedDate = arrivalDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"));

                // UPDATED: Process both stock reduction AND order creation
                processDatabaseUpdate(selected);
                saveOrderToDatabase(selected, "Guest Customer", formattedDate);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Order Confirmed");
                success.setHeaderText("ORDER SUCCESSFUL!");
                success.setContentText("Item: " + selected.getBrand() + " " + selected.getStyle() + "\nDelivery By: " + formattedDate);
                applyLargeDialogStyle(success.getDialogPane(), 650, 400);
                success.showAndWait();
            });
        });
    }

    private void processDatabaseUpdate(Shoe selected) {
        String sql = "UPDATE shoes SET stock = stock - 1 WHERE brand = ? AND style = ? AND size = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selected.getBrand());
            ps.setString(2, selected.getStyle());
            ps.setString(3, selected.getSize());
            ps.executeUpdate();
            loadInventory();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // NEW METHOD: This makes the orders show up in the Admin Panel
    private void saveOrderToDatabase(Shoe selected, String customer, String arrival) {
        String sql = "INSERT INTO orders (customer_name, items_ordered, arrival_date) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer);
            ps.setString(2, selected.getBrand() + " " + selected.getStyle());
            ps.setString(3, arrival);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void applyLargeDialogStyle(DialogPane pane, double width, double height) {
        pane.setMinWidth(width);
        pane.setPrefWidth(width);
        pane.setMinHeight(height);
    }

    @FXML 
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/shoedeliverysystem2/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleExit() { System.exit(0); }
    
    @FXML private void handleMousePressed(MouseEvent event) { 
        xOffset = event.getSceneX(); 
        yOffset = event.getSceneY(); 
    }
    
    @FXML private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if(!stage.isFullScreen()){
            stage.setX(event.getScreenX() - xOffset); 
            stage.setY(event.getScreenY() - yOffset);
        }
    }
}