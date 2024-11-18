package za.co.tyaphile.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import za.co.tyaphile.order.Order;
import za.co.tyaphile.product.Product;
import za.co.tyaphile.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    private static final Map<String, Object[]> products = new HashMap<>();
    private static final List<String> prodId = new ArrayList<>();
    @Test
    void testDatabaseConnection() {
        assertTrue(DatabaseManager.addUser("John", "john@example.com"));
        User user = DatabaseManager.getUser("John", "john@example.com");
        assertNotNull(user.getId());
        assertInstanceOf(String.class, user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }

    /**
     * Place one order
     * Update unpaid order by adding 4 extra items to bucket
     * Should contain 5 items and 1 active order
     * Pay the active order bucket
     * Make another order with 4 items
     * Should have 2 orders in total, 1 paid and 1 unpaid
     */
    @Test
    void testAddNewOrder() {
        String custId = "1";
        Order order = DatabaseManager.getCustomerOrder(custId);
        assertEquals(0, order.getOrderedProducts().size());
//        assertEquals(5, prodId.size());
        assertTrue(DatabaseManager.addOrder(custId, new String[]{prodId.get(0)}));
        order = DatabaseManager.getCustomerOrder(custId);
        assertEquals(1, order.getOrderedProducts().size());
        assertEquals(1, DatabaseManager.getAllCustomerOrders(custId).size());
        assertTrue(DatabaseManager.addOrder(custId, new String[]{prodId.get(1), prodId.get(2), prodId.get(3), prodId.get(4)}));
        order = DatabaseManager.getCustomerOrder(custId);
        assertEquals(5, order.getOrderedProducts().size());
        assertEquals(1, DatabaseManager.getAllCustomerOrders(custId).size());
        assertTrue(DatabaseManager.makePayment(custId));
        assertTrue(DatabaseManager.addOrder(custId, new String[]{prodId.get(1), prodId.get(2), prodId.get(3), prodId.get(4)}));
        order = DatabaseManager.getCustomerOrder(custId);
        assertEquals(4, order.getOrderedProducts().size());
        assertEquals(2, DatabaseManager.getAllCustomerOrders(custId).size());
    }

    @BeforeAll
    static void setupDBConnect() {
        new DatabaseManager(":memory:");

        products.put("USB Flash drive", new Object[] {"64 GB storage", 39.99});
        products.put("Headphones", new Object[]{"Wireless headphones", 120.00});
        products.put("Laptop", new Object[]{"256 GB storage laptop", 7000});
        products.put("Routor", new Object[]{"32 connected devices", 1500});
        products.put("Bird cage", new Object[]{"Keep your birds safe at night", 320});

        for(Map.Entry<String, Object[]> prodInfo : products.entrySet()) {
            DatabaseManager.addProduct(prodInfo.getKey(), prodInfo.getValue()[0].toString(), Double.parseDouble(prodInfo.getValue()[1].toString()));
        }

        prodId.addAll(DatabaseManager.getAllProducts().stream().map(Product::getProductId).toList());
    }
}
