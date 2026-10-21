package shoedeliverysystem2.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptService {
    public static String generateReceipt(String brand, String style, double price) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return "----------------------------------\n" +
               "       AHD SHOE DELIVERY          \n" +
               "----------------------------------\n" +
               "Date: " + dtf.format(LocalDateTime.now()) + "\n" +
               "Item: " + brand + " " + style + "\n" +
               "Total: $" + String.format("%.2f", price) + "\n" +
               "----------------------------------\n" +
               "    THANK YOU FOR SHOPPING!       \n" +
               "----------------------------------";
    }
}