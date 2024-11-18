package za.co.tyaphile.order;

import java.text.SimpleDateFormat;
import java.util.*;

public class Order {
    private final Collection<String> orderedProducts = new ArrayList<>();
    private String orderId, customerId;
    private boolean isPaid;
    private float total;

    public Order() {}

    public Order(String customerId) {
        orderId = new SimpleDateFormat("yyyy").format(new Date()).concat("-").concat(UUID.randomUUID().toString());
        this.customerId = customerId;
    }

    public Collection<String> getOrderedProducts() {
        return orderedProducts;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total += total;
    }
}
