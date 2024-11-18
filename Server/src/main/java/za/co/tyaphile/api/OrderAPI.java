package za.co.tyaphile.api;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import za.co.tyaphile.database.DatabaseManager;
import za.co.tyaphile.order.Order;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static za.co.tyaphile.ECommerceServer.getErrorMessage;

public class OrderAPI {

    public void updateOrder(Context context) {
        Map<?, ?> request = (Map<?, ?>) new Gson().fromJson(context.body(), Map.class);

        if (!request.containsKey("customerId") ||
                (request.get("customerId").toString() == null || request.get("customerId").toString().isBlank())) {
            context.status(HttpStatus.BAD_REQUEST);
            context.json(getErrorMessage(HttpStatus.BAD_REQUEST, "You need to be signed in to pay for the order"));
        } else {
            if (request.containsKey("paid") && ((Boolean) request.get("paid"))) {
                String customerId = request.get("customerId").toString();
                String orderId = request.get("id").toString();
                DatabaseManager.makePayment(customerId);
                context.json(getOrderJson(DatabaseManager.getCustomerOrder(customerId, orderId)));
            }
        }
    }

    public void getOrder(Context context) {
        Map<?, ?> request = (Map<?, ?>) new Gson().fromJson(context.body(), Map.class);

        if (request == null) {
            context.json(getOrderJson(DatabaseManager.getOrder(context.pathParamMap().get("id"))));
        } else {
            if (!request.containsKey("customerId") ||
                    (request.get("customerId").toString() == null || request.get("customerId").toString().isBlank())) {
                context.status(HttpStatus.BAD_REQUEST);
                context.json(getErrorMessage(HttpStatus.BAD_REQUEST, "You need to be signed in to place an order"));
            } else {
                context.json(getOrderJson(DatabaseManager.getCustomerOrder(request.get("customerId").toString())));
            }
        }
    }

    public void addOrder(Context context) {
        Map<?, ?> request = (Map<?, ?>) new Gson().fromJson(context.body(), Map.class);
        if (!request.containsKey("customerId") ||
                (request.get("customerId").toString() == null || request.get("customerId").toString().isBlank())) {
            context.status(HttpStatus.BAD_REQUEST);
            context.json(getErrorMessage(HttpStatus.BAD_REQUEST, "You need to be signed in to place an order"));
        } else {
            String customerID = request.get("customerId").toString();
            String productsString = request.get("products").toString();

            String[] products;
            if (productsString.startsWith("[") && productsString.endsWith("]")) {
                List<String> productList = Arrays.stream(productsString.substring(1, productsString.length() - 1)
                        .replaceAll("\"", "").split(",")).toList();
                products = productList.toArray(new String[0]);
            } else {
                products = new String[] {productsString};
            }

            boolean isOrdered = DatabaseManager.addOrder(customerID, products);
            if (isOrdered) {
                context.json(getOrderJson(DatabaseManager.getCustomerOrder(customerID)));
            } else {
                context.json(getErrorMessage(HttpStatus.BAD_REQUEST, "Failed to placed an order"));
            }
        }
    }

    private String getOrderJson(Order order) {
        Gson json = new Gson();

        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("id", order.getOrderId());
        orderInfo.put("paid", order.isPaid());
        orderInfo.put("customerId", order.getCustomerId());
        orderInfo.put("products", order.getOrderedProducts());
        orderInfo.put("total", order.getTotal());

        return json.toJson(orderInfo);
    }
}
