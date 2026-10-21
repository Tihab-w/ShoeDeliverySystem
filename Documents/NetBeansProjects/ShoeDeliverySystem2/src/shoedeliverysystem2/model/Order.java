package shoedeliverysystem2.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Order {
    private final StringProperty customerName;
    private final StringProperty itemsOrdered;
    private final StringProperty orderTime;
    private final StringProperty arrivalStatus; // Matches your model's field

    public Order(String customerName, String itemsOrdered, String orderTime, String arrivalStatus) {
        this.customerName = new SimpleStringProperty(customerName);
        this.itemsOrdered = new SimpleStringProperty(itemsOrdered);
        this.orderTime = new SimpleStringProperty(orderTime);
        this.arrivalStatus = new SimpleStringProperty(arrivalStatus);
    }

    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty itemsOrderedProperty() { return itemsOrdered; }
    public StringProperty orderTimeProperty() { return orderTime; }
    public StringProperty arrivalStatusProperty() { return arrivalStatus; }

    public String getCustomerName() { return customerName.get(); }
    public String getItemsOrdered() { return itemsOrdered.get(); }
    public String getOrderTime() { return orderTime.get(); }
    public String getArrivalStatus() { return arrivalStatus.get(); }
}