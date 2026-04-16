package dev.allstak.sample.service;

import dev.allstak.AllStak;
import dev.allstak.sample.dto.CreateOrderRequest;
import dev.allstak.sample.entity.Order;
import dev.allstak.sample.exception.OrderNotFoundException;
import dev.allstak.sample.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(CreateOrderRequest dto) {
        Order order = new Order();
        order.setCustomerName(dto.getCustomerName());
        order.setProduct(dto.getProduct());
        order.setQuantity(dto.getQuantity());
        order.setTotalPrice(dto.getTotalPrice());
        order.setNotes(dto.getNotes());
        order.setStatus("PENDING");

        Order saved = orderRepository.save(order);

        AllStak.captureLog("info", "Order created successfully", Map.of(
                "orderId", saved.getId().toString(),
                "customerName", saved.getCustomerName(),
                "product", saved.getProduct(),
                "action", "CREATE_ORDER"
        ));

        return saved;
    }

    public Order getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        AllStak.captureLog("info", "Order retrieved", Map.of(
                "orderId", id.toString(),
                "action", "GET_ORDER"
        ));

        return order;
    }

    public List<Order> listOrders() {
        List<Order> orders = orderRepository.findAll();

        AllStak.captureLog("info", "Listed all orders", Map.of(
                "count", orders.size(),
                "action", "LIST_ORDERS"
        ));

        return orders;
    }

    public Order updateOrder(UUID id, CreateOrderRequest dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        order.setCustomerName(dto.getCustomerName());
        order.setProduct(dto.getProduct());
        order.setQuantity(dto.getQuantity());
        order.setTotalPrice(dto.getTotalPrice());
        order.setNotes(dto.getNotes());

        Order updated = orderRepository.save(order);

        AllStak.captureLog("info", "Order updated successfully", Map.of(
                "orderId", id.toString(),
                "customerName", updated.getCustomerName(),
                "action", "UPDATE_ORDER"
        ));

        return updated;
    }

    public void deleteOrder(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found with id: " + id);
        }

        orderRepository.deleteById(id);

        AllStak.captureLog("info", "Order deleted", Map.of(
                "orderId", id.toString(),
                "action", "DELETE_ORDER"
        ));
    }

    public Order confirmOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        order.setStatus("CONFIRMED");
        Order confirmed = orderRepository.save(order);

        AllStak.captureLog("info", "Order confirmed", Map.of(
                "orderId", id.toString(),
                "customerName", confirmed.getCustomerName(),
                "previousStatus", "PENDING",
                "newStatus", "CONFIRMED",
                "action", "CONFIRM_ORDER"
        ));

        return confirmed;
    }

    public Order cancelOrder(UUID id) {
        AllStak.addBreadcrumb("log", "Attempting to cancel order: " + id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        AllStak.addBreadcrumb("log", "Order found with status: " + order.getStatus(), "info",
                Map.of("orderId", id.toString(), "currentStatus", order.getStatus()));

        if (!"PENDING".equals(order.getStatus())) {
            AllStak.captureLog("warn", "Cannot cancel non-pending order", Map.of(
                    "orderId", id.toString(),
                    "currentStatus", order.getStatus(),
                    "action", "CANCEL_ORDER_REJECTED"
            ));
            throw new IllegalStateException(
                    "Cannot cancel order with status: " + order.getStatus() + ". Only PENDING orders can be cancelled.");
        }

        order.setStatus("CANCELLED");
        Order cancelled = orderRepository.save(order);

        AllStak.captureLog("info", "Order cancelled", Map.of(
                "orderId", id.toString(),
                "customerName", cancelled.getCustomerName(),
                "action", "CANCEL_ORDER"
        ));

        return cancelled;
    }
}
