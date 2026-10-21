package shoedeliverysystem2.model;

import javafx.beans.property.*;

public class Shoe {
    private final StringProperty brand;
    private final StringProperty style;
    private final StringProperty size;
    private final StringProperty color;
    private final IntegerProperty price;
    private final IntegerProperty stock;

    public Shoe(String brand, String style, String size, String color, int price, int stock) {
        this.brand = new SimpleStringProperty(brand);
        this.style = new SimpleStringProperty(style);
        this.size = new SimpleStringProperty(size);
        this.color = new SimpleStringProperty(color);
        this.price = new SimpleIntegerProperty(price);
        this.stock = new SimpleIntegerProperty(stock);
    }

    // Property methods (Required for your current Controller code)
    public StringProperty brandProperty() { return brand; }
    public StringProperty styleProperty() { return style; }
    public StringProperty sizeProperty() { return size; }
    public StringProperty colorProperty() { return color; }
    public IntegerProperty priceProperty() { return price; }
    public IntegerProperty stockProperty() { return stock; }

    // Standard Getters (Useful for Database logic)
    public String getBrand() { return brand.get(); }
    public String getStyle() { return style.get(); }
    public String getSize() { return size.get(); }
    public String getColor() { return color.get(); }
    public int getPrice() { return price.get(); }
    public int getStock() { return stock.get(); }
}