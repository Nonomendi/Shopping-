package za.co.tyaphile.product;

import java.util.ArrayList;
import java.util.Collection;

public interface Products {
    Collection<Product> allProducts = new ArrayList<>();
    Collection<Product> getAllProducts();
    void addProduct(Product product);
    void removeProduct(String id);
    void updateProduct(String id, Product product);
}
