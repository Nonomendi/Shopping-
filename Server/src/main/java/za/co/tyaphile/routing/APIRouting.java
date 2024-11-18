package za.co.tyaphile.routing;

import io.javalin.Javalin;
import za.co.tyaphile.api.OrderAPI;
import za.co.tyaphile.api.ProductAPI;
import za.co.tyaphile.api.UserAPI;

import static io.javalin.apibuilder.ApiBuilder.get;

public class APIRouting {
    public APIRouting(Javalin server) {
        server.routes(() -> {
            get("/", context -> context.render("index.html"));
        });

        ProductAPI product = new ProductAPI();
        server.post("/product", product::addProduct);
        server.post("/remove-products", product::deleteProduct);
        server.post("/products", product::getProductList);

        server.get("/product/{id}", product::getProduct);
        server.put("/product/{id}", product::updateProduct);

        OrderAPI order = new OrderAPI();
        server.post("/order", order::addOrder);
        server.put("/order/{id}", order::updateOrder);
        server.post("/orders", order::getOrder);
        server.get("/order/{id}", order::getOrder);

        UserAPI user = new UserAPI();
        server.post("/customer", user::addUser);
        server.delete("/customer/{id}", user::deleteUser);
    }
}
