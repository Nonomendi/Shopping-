package za.co.tyaphile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.*;
import za.co.tyaphile.database.DatabaseManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private static ECommerceServer server;
    private static final Map<String, Object[]> products = new HashMap<>();
    private List<String> ids;

    @Test
    void testPlaceOrderWithoutLogin() {
        HttpResponse<JsonNode> response;
        for(String id:ids) {
            Map<String, String> order = new HashMap<>();
            order.put("customerId", null);
            order.put("products", id);
            response = Unirest.post("http://localhost:5000/order")
                    .body(new Gson().toJson(order))
                    .asJson();

            assertEquals(400, response.getStatus());
        }
    }

    @Test
    void testPlaceOneOrderAtATime() {
        Map<String, String> user = new HashMap<>();
        user.put("name", "John");
        user.put("email", "john@example.com");

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/customer")
                .body(new Gson().toJson(user))
                .asJson();

        assertEquals(200, response.getStatus());
        String customerID = response.getBody().getObject().toMap().get("id").toString();
        assertNotNull(customerID);

        for(String id:ids) {
            Map<String, String> order = new HashMap<>();
            order.put("customerId", customerID);
            order.put("products", id);
            response = Unirest.post("http://localhost:5000/order")
                    .body(new Gson().toJson(order))
                    .asJson();

            assertEquals(200, response.getStatus());
        }
        assertEquals(8979.99, response.getBody().getObject().get("total"));
    }

    @Test
    void testPlaceAllOrderAtOnce() {
        Map<String, String> user = new HashMap<>();
        user.put("name", "John");
        user.put("email", "john@example.com");

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/customer")
                .body(new Gson().toJson(user))
                .asJson();

        assertEquals(200, response.getStatus());
        String customerID = response.getBody().getObject().toMap().get("id").toString();
        assertNotNull(customerID);

        Map<String, String> order = new HashMap<>();
        order.put("customerId", customerID);
        order.put("products", new Gson().toJson(ids));
        response = Unirest.post("http://localhost:5000/order")
                .body(new Gson().toJson(order))
                .asJson();

        assertEquals(200, response.getStatus());
        assertEquals(8979.99, response.getBody().getObject().get("total"));
    }

    @Test
    void testAddUser() {
        Map<String, String> user = new HashMap<>();
        user.put("name", "John");
        user.put("email", "john@example.com");

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/customer")
                .body(new Gson().toJson(user))
                .asJson();

        JSONObject json = response.getBody().getObject();

        assertEquals(200, response.getStatus());
        assertNotNull(json.toMap().get("id"));
        assertEquals(user.get("name"), json.toMap().get("name").toString());
        assertEquals(user.get("email"), json.toMap().get("email").toString());
    }

    @Test
    void testAddUserThenDelete() {
        Map<String, String> user = new HashMap<>();
        user.put("name", "John");
        user.put("email", "john@example.com");

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/customer")
                .body(new Gson().toJson(user))
                .asJson();

        JSONObject json = response.getBody().getObject();

        assertEquals(200, response.getStatus());
        assertNotNull(json.toMap().get("id"));
        assertEquals(user.get("name"), json.toMap().get("name").toString());
        assertEquals(user.get("email"), json.toMap().get("email").toString());

        String id = response.getBody().getObject().toMap().get("id").toString();
        response = Unirest.delete("http://localhost:5000/customer/" + id).asJson();
        assertEquals(200, response.getStatus());
    }

    @Test
    void testGetSomeProducts() {
        ArrayList<String> lookup = new ArrayList<>(ids.subList(0, 2));
        ArrayList<String> notLookup = new ArrayList<>(ids.subList(2, ids.size()));

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/products")
                .body(lookup)
                .asJson();

        Gson json = new Gson();
        ArrayList<?> results = (ArrayList<?>) json.fromJson(response.getBody().toString(), ArrayList.class);

        assertEquals(2, results.size());
        for (Object item:results) {
            String id = ((Map<?, ?>) json.fromJson(String.valueOf(item), Map.class)).get("id").toString();
            assertTrue(lookup.contains(id));
        }
        for (Object item:results) {
            String id = ((Map<?, ?>) json.fromJson(String.valueOf(item), Map.class)).get("id").toString();
            assertFalse(notLookup.contains(id));
        }
    }

    @Test
    void testProductAddThenDeleteOne() {
        Random random = new Random();
        String randomIdSelect = ids.get(random.nextInt(ids.size()));

        HttpResponse<JsonNode> response = Unirest.get("http://localhost:5000/product/" + randomIdSelect).asJson();
        assertEquals(200, response.getStatus());
        assertEquals(randomIdSelect, response.getBody().getObject().toMap().get("id"));

        response = Unirest.post("http://localhost:5000/remove-products")
                .body(new ArrayList<>(Collections.singletonList(randomIdSelect)))
                .asJson();

        assertEquals(200, response.getStatus());

        response = Unirest.get("http://localhost:5000/product/" + randomIdSelect).asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Product ID: " + randomIdSelect + " not found", response.getBody().getObject().toMap().get("message"));
    }
    @Test
    void testProductAddThenDeleteAll() {
        HttpResponse<JsonNode> response;

        for (String id:ids) {
            response = Unirest.get("http://localhost:5000/product/" + id).asJson();
            assertEquals(200, response.getStatus());
            assertEquals(id, response.getBody().getObject().toMap().get("id"));
        }

        response = Unirest.post("http://localhost:5000/remove-products")
                .body(new ArrayList<>(ids))
                .asJson();

        assertEquals(200, response.getStatus());

        for(String id:ids) {
            response = Unirest.get("http://localhost:5000/product/" + id).asJson();
            assertEquals(404, response.getStatus());
            assertEquals("Product ID: " + id + " not found", response.getBody().getObject().toMap().get("message"));
        }
    }

    @Test
    void testProductAddThenFetch() {
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:5000/product/" + ids.get(ids.size() - 1)).asJson();
        assertEquals(200, response.getStatus());
        assertEquals(ids.get(ids.size() - 1), response.getBody().getObject().toMap().get("id"));
    }

    @Test
    void fetchNoneExistingProduct() {
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:5000/product/fake-id").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Product ID: fake-id not found", response.getBody().getObject().toMap().get("message"));
    }

    @Test
    void testAddProduct() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "USB Flash drive");
        json.addProperty("description", "64 GB storage");
        json.addProperty("price", 39.99);

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/product")
                .body(json)
                .asJson();

        assertEquals(200, response.getStatus());
        Gson gson = new Gson();
        ids.add(((Map<?, ?>) gson.fromJson(response.getBody().toString(), Map.class)).get("id").toString());
        assertEquals(6, ids.size());
    }

    @Test
    void testAddProductIncorrectDetails() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "USB Flash drive");
        json.addProperty("description", "64 GB storage");
        json.addProperty("price", "twelve");

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/product")
                .body(json)
                .asJson();

        assertEquals(400, response.getStatus());
    }

    @Test
    void testNoPricePlaced() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "USB Flash drive");
        json.addProperty("description", "64 GB storage");

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/product")
                .body(json)
                .asJson();

        assertEquals(400, response.getStatus());
    }

    @Test
    void testAddProductMissingDetails() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "USB Flash drive");
        json.addProperty("price", 39.99);

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/product")
                .body(json)
                .asJson();

        assertEquals(400, response.getStatus());
    }

    private void setIds() {
        ids = new ArrayList<>();

        for (Map.Entry<String, Object[]> entry:products.entrySet()) {
            JsonObject json = new JsonObject();
            json.addProperty("name", entry.getKey());
            json.addProperty("description", entry.getValue()[0].toString());
            json.addProperty("price", Double.parseDouble(entry.getValue()[1].toString()));

            HttpResponse<JsonNode> response = Unirest.post("http://localhost:5000/product")
                    .body(json)
                    .asJson();
            assertEquals(200, response.getStatus());
            ids.add(response.getBody().getObject().toMap().get("id").toString());
        }
    }

    @BeforeAll
    static void setup() throws InterruptedException {
        Thread.sleep(1500);
        new DatabaseManager(":memory:");
        server = new ECommerceServer();
        server.start();

        products.put("USB Flash drive", new Object[] {"64 GB storage", 39.99});
        products.put("Headphones", new Object[]{"Wireless headphones", 120.00});
        products.put("Laptop", new Object[]{"256 GB storage laptop", 7000});
        products.put("Routor", new Object[]{"32 connected devices", 1500});
        products.put("Bird cage", new Object[]{"Keep your birds safe at night", 320});
    }

    @BeforeEach
    void setProducts() {
        setIds();
    }

    @AfterEach
    void removeProducts() {
        Unirest.post("http://localhost:5000/remove-products")
                .body(new ArrayList<>(ids))
                .asJson();
    }

    @AfterAll
    static void cleanUp() {
        server.stop();
    }
}
