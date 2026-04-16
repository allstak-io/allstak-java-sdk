package dev.allstak.sample.controller;

import dev.allstak.AllStak;
import dev.allstak.sample.dto.CreateOrderRequest;
import dev.allstak.sample.entity.Order;
import dev.allstak.sample.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders() {
        return ResponseEntity.ok(orderService.listOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable UUID id,
                                              @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail() {
        AllStak.addBreadcrumb("navigation", "User navigated to /api/orders/fail");
        AllStak.addBreadcrumb("log", "Preparing to simulate server failure", "warn",
                java.util.Map.of("reason", "intentional-test", "endpoint", "/api/orders/fail"));
        AllStak.addBreadcrumb("http", "GET /api/orders/fail -> processing", "info",
                java.util.Map.of("method", "GET", "path", "/api/orders/fail"));
        throw new RuntimeException("Intentional internal server error for testing AllStak SDK");
    }
}
