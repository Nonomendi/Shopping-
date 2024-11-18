package za.co.tyaphile.product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ProductsDB implements Products {

    @Override
    public Collection<Product> getAllProducts() {
        return allProducts;
    }

    @Override
    public void addProduct(Product product) {
        allProducts.add(product);
    }

    @Override
    public void removeProduct(String id) {
        List<Product> products = allProducts.stream().filter(x -> x.getProductId().equals(id)).toList();
        if (!products.isEmpty()) allProducts.remove(products.get(0));
    }

    @Override
    public void updateProduct(String id, Product product) {
        allProducts.stream().filter(x -> x.getProductId().equals(product.getProductId()))
                .forEach(prod -> {
                    prod.setPrice(product.getPrice());
                    prod.setProductDescription(product.getProductDescription());
                    prod.setProductName(product.getProductName());
                });
    }
}
