package za.co.tyaphile.product;

import java.util.UUID;

public class Product {
    private String prodId, prodName, prodDesc;
    private double price;

    public Product() {}

    public Product(final String productName, String productDescription) {
        this.prodId = UUID.randomUUID().toString();
        this.prodName = productName;
        this.prodDesc = productDescription;
    }

    public Product(final String productName, String productDescription, double productPrice) {
        this(productName, productDescription);
        this.price = productPrice;
    }

    public Product(String id, final String productName, String productDescription, double productPrice) {
        this.prodId = id;
        this.prodName = productName;
        this.prodDesc = productDescription;
        this.price = productPrice;
    }

    public void setProductName(String prodName) {
        this.prodName = prodName;
    }

    public void setProductDescription(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductId() {
        return prodId;
    }

    public String getProductName() {
        return prodName;
    }

    public String getProductDescription() {
        return prodDesc;
    }

    public double getPrice() {
        return price;
    }
}
