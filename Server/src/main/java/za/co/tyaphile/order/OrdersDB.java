package za.co.tyaphile.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class OrdersDB {
    private final Collection<Order> orders = new ArrayList<>();
    public Collection<Order> getOrders() {
        return orders.stream().toList();
    }

    public Collection<Order> getOrders(String customerID) {
        return orders.stream().filter(x -> x.getCustomerId().equals(customerID)).toList();
    }

    public void addOrder(Order placeOrder) {
        Optional<Order> upPaidOrder = orders.stream().filter(x -> x.getCustomerId()
                        .equals(placeOrder.getCustomerId()) && !x.isPaid())
                .findFirst();
        if(upPaidOrder.isPresent()) {
            Order order = upPaidOrder.get();
            order.setTotal(placeOrder.getTotal());
            order.getOrderedProducts().add(placeOrder.getOrderedProducts().stream().toList().get(0));

            System.out.println(getOrders(placeOrder.getCustomerId()).stream().map(Order::getOrderedProducts).toList().get(0).size());
            System.out.println(order.getOrderedProducts().size());
        } else {
            orders.add(placeOrder);
        }
    }
}
