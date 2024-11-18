package za.co.tyaphile.database;

import za.co.tyaphile.database.connect.Connect;
import za.co.tyaphile.order.Order;
import za.co.tyaphile.product.Product;
import za.co.tyaphile.user.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static String DB_NAME = "";
    private static PreparedStatement ps;
    private static ResultSet rs;
    private static final String USER_TABLE = "users", PRODUCT = "products", ORDER = "orders";

    public DatabaseManager() {
        this("commerce.db");
    }

    public DatabaseManager(String db) {
        DB_NAME = db;
        setupDatabase();
    }

    public static void placeOrder() {

    }

    public static boolean makePayment(String customerId) {
        try {
            String sql = "UPDATE " + ORDER + " SET is_paid=? WHERE order_id=?";
            PreparedStatement ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setBoolean(1, true);
            ps.setString(2, getCustomerOrder(customerId).getOrderId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean addOrder(String customerId, String[] products) {
        Order order = getCustomerOrder(customerId);
        String sql;
        PreparedStatement ps;

        try {
            if (order.getTotal() <= 0) {
                sql = "INSERT INTO " + ORDER + " (order_id, customer_id, product_list, total_amount) VALUES (?, ?, ?, ?);";
                ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
                ps.setString(1, order.getOrderId());
                ps.setString(2, customerId);
                ps.setObject(3, Arrays.stream(products).collect(Collectors.toList()));
                ps.setDouble(4, Arrays.stream(products).map(x -> getProduct(x).getPrice())
                        .reduce(Double::sum).orElse(0d));
            } else {
                sql = "UPDATE " + ORDER + " SET product_list=?, total_amount=? WHERE order_id=?";
                ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
                order.getOrderedProducts().addAll(Arrays.stream(products).toList());
                ps.setObject(1, order.getOrderedProducts());
                ps.setDouble(2, (order.getTotal() + Arrays.stream(products).map(x -> getProduct(x).getPrice())
                        .reduce(Double::sum).orElse(0d)));
                ps.setString(3, order.getOrderId());
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Order getOrder(String orderID) {
        String sql = "SELECT * FROM " + ORDER + " WHERE order_id=?;";
        Order order = null;

        try {
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setString(1, orderID);
            rs = ps.executeQuery();
            while (rs.next()) {
                String customer_id = rs.getString("customer_id");
                boolean is_paid = rs.getBoolean("is_paid");
                String prodIds = rs.getObject("product_list").toString();
                List<String> wishProducts = Arrays.stream(prodIds.substring(1, prodIds.length() - 1).split(", ")).toList();
                float priceTotal = rs.getFloat("total_amount");

                order = new Order(customer_id);
                order.setOrderId(orderID);
                order.setPaid(is_paid);
                order.getOrderedProducts().addAll(wishProducts);
                order.setTotal(priceTotal);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    public static List<Order> getAllCustomerOrders(String customerId) {
        String sql = "SELECT * FROM " + ORDER + " WHERE customer_id=?;";
        List<Order> orders = new ArrayList<>();

        try {
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setString(1, customerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                String order_id = rs.getString("order_id");
                boolean is_paid = rs.getBoolean("is_paid");
                String prodIds = rs.getObject("product_list").toString();
                List<String> wishProducts = Arrays.stream(prodIds.substring(1, prodIds.length() - 1).split(", ")).toList();
                float priceTotal = rs.getFloat("total_amount");

                Order order = new Order(customerId);
                order.setOrderId(order_id);
                order.setPaid(is_paid);
                order.getOrderedProducts().addAll(wishProducts);
                order.setTotal(priceTotal);
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return orders;
    }

    public static Order getCustomerOrder(String customerId) {
        return getAllCustomerOrders(customerId).stream().filter(x -> !x.isPaid()).findFirst().orElse(new Order(customerId));
    }

    public static Order getCustomerOrder(String customerId, String orderId) {
        return getAllCustomerOrders(customerId).stream().filter(x -> x.getOrderId().equals(orderId)).findAny().orElse(new Order(customerId));
    }

    public static boolean addUser(String name, String email) {
        User user = getUser(name, email);
        if (user == null) {
            user = new User(name, email);
            String sql = "INSERT INTO " + USER_TABLE + " VALUES (?, ?, ?);";
            try {
                ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());

                return ps.executeUpdate() > 0;
            } catch (SQLException ignored) {}
        }
        return false;
    }

    public static User getUser(String name, String email) {
        String sql = "SELECT * from " + USER_TABLE + " WHERE user_name = ? AND user_email = ?;";
        try {
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);

            rs = ps.executeQuery();
            User user = null;
            while (rs.next()) {
                String id = rs.getString("user_id");
                name = rs.getString("user_name");
                email = rs.getString("user_email");

                user = new User(id, name, email);
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void removeUser(String id) {
        String sql = "DELETE FROM " + USER_TABLE + " WHERE user_id = ?;";
        try {
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Product addProduct(String productName, String description, double price) {
        Product product = new Product(productName, description, price);

        String sql = "INSERT INTO " + PRODUCT + " VALUES (?, ?, ?, ?);";
        try {
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setString(1, product.getProductId());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getProductDescription());
            ps.setDouble(4, product.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return product;
    }

    public static void removeProduct(String id) {
        String sql = "DELETE FROM " + PRODUCT + " WHERE product_id=?;";
        try {
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateProduct(String id, String name, String description, double price) throws SQLException {
        String sql = "UPDATE " + PRODUCT + " SET product_name=?, product_description=?, product_price=? WHERE product_id=?;";
        ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, description);
        ps.setDouble(3, price);
        ps.setString(4, id);

        return ps.executeUpdate() > 0;
    }

    public static Product getProduct(String id) {
        Product product = null;

        try {
            String sql = "SELECT * FROM " + PRODUCT + " WHERE product_id=?;";
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            ps.setString(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                product = new Product(rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("product_description"),
                        rs.getDouble("product_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return product;
    }

    public static List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        try {
            String sql = "SELECT * FROM " + PRODUCT;
            ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                products.add(new Product(rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("product_description"),
                        rs.getDouble("product_price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public static List<Product> getSelectedProducts(String[] ids) {
        List<Product> products = new ArrayList<>();

        for(String id:ids) {
            products.add(getProduct(id));
        }

        return products;
    }

    public static void setupDatabase() {
        String[] sqls = {
                "CREATE TABLE IF NOT EXISTS " + USER_TABLE + " (" +
                        "user_id VARCHAR(255) NOT NULL PRIMARY KEY, " +
                        "user_name VARCHAR(255) NOT NULL, " +
                        "user_email VARCHAR(255) UNIQUE NOT NULL" +
                        ");",
                "CREATE TABLE IF NOT EXISTS " + PRODUCT + " (" +
                        "product_id VARCHAR(255) NOT NULL PRIMARY KEY, " +
                        "product_name VARCHAR(255) NOT NULL, " +
                        "product_description VARCHAR(255) NOT NULL, " +
                        "product_price DECIMAL NOT NULL" +
                        ");",
                "CREATE TABLE IF NOT EXISTS " + ORDER + " (" +
                        "order_id VARCHAR(255) NOT NULL PRIMARY KEY, " +
                        "is_paid Boolean NOT NULL DEFAULT 0, " +
                        "customer_id VARCHAR(255) NOT NULL, " +
                        "product_list BLOB NOT NULL, " +
                        "total_amount DECIMAL NOT NULL" +
                        ");"
        };

        for (String sql:sqls) {
            try {
                ps = Connect.getConnection(DB_NAME).prepareStatement(sql);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
