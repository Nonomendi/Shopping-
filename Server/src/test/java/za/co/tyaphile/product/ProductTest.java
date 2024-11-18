package za.co.tyaphile.product;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {
    @Test
    void testWithPrice() {
        Product prod1 = new Product("Atcher", "Mango fruits made spicy", 20.0);
        Product prod2 = new Product("Roll On", "No sweaty armpits", 47);

        assertEquals("Atcher", prod1.getProductName());
        assertEquals("Mango fruits made spicy", prod1.getProductDescription());
        assertEquals(20, prod1.getPrice());
        prod1.setPrice(22);
        assertEquals(22, prod1.getPrice());
        assertNotEquals(prod1.getProductName(), prod2.getProductName());
        assertNotEquals(prod1.getProductId(), prod2.getProductId());
    }

    @Test
    void testWithoutPrice() {
        Product prod1 = new Product("Atcher", "Mango fruits made spicy");
        Product prod2 = new Product("Roll On", "No sweaty armpits");

        assertEquals("Atcher", prod1.getProductName());
        assertEquals("Mango fruits made spicy", prod1.getProductDescription());
        assertEquals(0, prod1.getPrice());
        assertNotEquals(prod1.getProductName(), prod2.getProductName());
        assertNotEquals(prod1.getProductId(), prod2.getProductId());
    }

    @Test
    void testRemoveFromEmpty() {
        ProductsDB productsDB = new ProductsDB();
        productsDB.removeProduct("1");
    }

    @Test
    void testRemoveExisting() {
        ProductsDB productsDB = new ProductsDB();
        Product product = new Product("USB stick", "64 GB storage device");
        assertTrue(productsDB.getAllProducts().isEmpty());
        productsDB.addProduct(product);
        assertEquals(1, productsDB.getAllProducts().size());
        productsDB.removeProduct(product.getProductId());
        assertTrue(productsDB.getAllProducts().isEmpty());
    }
}
