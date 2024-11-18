package za.co.tyaphile.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import kong.unirest.JsonNode;
import za.co.tyaphile.database.DatabaseManager;
import za.co.tyaphile.product.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static za.co.tyaphile.ECommerceServer.getErrorMessage;

public class ProductAPI {

    public void getProductList(Context context) {
        List<Product> allProducts;
        List<String> response;

//        Map<String, String> response = allProducts.stream()
//                .collect(Collectors.toMap(Product::getProductId, this::getProductJson));

        try {
            ArrayList<?> items = (ArrayList<?>) new Gson().fromJson(context.body(), ArrayList.class);
            allProducts = items.stream().map(x -> DatabaseManager.getProduct(String.valueOf(x))).toList();
        } catch (Exception e) {
            allProducts = DatabaseManager.getAllProducts();
        }
        response = allProducts.stream().map(this::getProductJson).collect(Collectors.toList());

        context.json(new Gson().toJson(response));
    }

    public void getProduct(Context context) {
        Product product = DatabaseManager.getProduct(context.pathParam("id"));
        if (product != null) {
            context.status(HttpStatus.OK);
            context.json(getProductJson(product));
        } else {
            context.status(HttpStatus.NOT_FOUND);
            context.json(getErrorMessage(HttpStatus.NOT_FOUND, "Product ID: " + context.pathParam("id") + " not found"));
        }
    }

    public void deleteProduct(Context context) {
        try {
            ArrayList<?> items = (ArrayList<?>) new Gson().fromJson(context.body(), ArrayList.class);
            items.forEach(x -> DatabaseManager.removeProduct(x.toString()));
            context.status(HttpStatus.OK);
        } catch (Exception e) {
            context.status(HttpStatus.BAD_REQUEST);
            context.json(getErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public void addProduct(Context ctx) {
        final JsonNode node = new JsonNode(ctx.body());
        try {
            Product product = DatabaseManager.addProduct(node.getObject().getString("name"),
                    node.getObject().getString("description"),
                    Double.parseDouble(node.getObject().getString("price")));
            ctx.status(HttpStatus.OK);

            assert product != null;
            ctx.json(getProductJson(product));
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(getErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    public void updateProduct(Context ctx) {
        final JsonNode node = new JsonNode(ctx.body());
        try {
            boolean isUpdated = DatabaseManager.updateProduct(node.getObject().getString("id"),
                    node.getObject().getString("name"),
                    node.getObject().getString("description"),
                    Double.parseDouble(node.getObject().getString("price")));
            if (isUpdated) {
                ctx.status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_MODIFIED);
            }
            getProduct(ctx);
        } catch (SQLException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(getErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    private String getProductJson(Product product) {
        JsonObject json = new JsonObject();
        json.addProperty("id", product.getProductId());
        json.addProperty("name", product.getProductName());
        json.addProperty("description", product.getProductDescription());
        json.addProperty("price", product.getPrice());

        return json.toString();
    }
}
