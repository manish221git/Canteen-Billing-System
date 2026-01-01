package com.canteen.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Order class implementing Orderable interface
 * Demonstrates Collections & Generics usage
 */
public class Order implements Orderable {
    private int orderId;
    private int userId;
    private Timestamp orderDate;
    private double totalAmount;
    private String status;
    private List<OrderItem> items; // Generic collection
    
    public Order() {
        this.items = new ArrayList<>(); // Generic ArrayList
        this.orderDate = new Timestamp(System.currentTimeMillis());
        this.status = "PENDING";
    }
    
    public Order(int orderId, int userId) {
        this();
        this.orderId = orderId;
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    /**
     * Implementation of Orderable interface
     */
    @Override
    public void addItem(MenuItem item, int quantity) {
        OrderItem orderItem = new OrderItem(0, orderId, item, quantity);
        items.add(orderItem);
        calculateTotal();
    }
    
    @Override
    public void removeItem(int itemId) {
        items.removeIf(item -> item.getMenuItem().getItemId() == itemId);
        calculateTotal();
    }
    
    @Override
    public List<OrderItem> getItems() {
        return new ArrayList<>(items); // Return copy for safety
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotal();
    }
    
    @Override
    public double calculateTotal() {
        totalAmount = items.stream()
                          .mapToDouble(OrderItem::getTotalCost)
                          .sum();
        return totalAmount;
    }
    
    /**
     * Apply discount to entire order
     */
    public double applyOrderDiscount(double discountPercentage) {
        double discountAmount = totalAmount * (discountPercentage / 100);
        return totalAmount - discountAmount;
    }
    
    public String getOrderSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Order ID: ").append(orderId).append("\n");
        summary.append("Date: ").append(orderDate).append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Items:\n");
        for (OrderItem item : items) {
            summary.append("  - ").append(item.getItemDetails()).append("\n");
        }
        summary.append("Total: ₹").append(String.format("%.2f", totalAmount));
        return summary.toString();
    }
}
