package com.canteen.model;

/**
 * OrderItem class representing an item in an order
 * Also implements Billable interface
 */
public class OrderItem implements Billable {
    private int orderItemId;
    private int orderId;
    private MenuItem menuItem;
    private int quantity;
    private double subtotal;
    
    public OrderItem() {}
    
    public OrderItem(int orderItemId, int orderId, MenuItem menuItem, int quantity) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.subtotal = menuItem.getPrice() * quantity;
    }
    
    // Getters and Setters
    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { 
        this.menuItem = menuItem;
        updateSubtotal();
    }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        updateSubtotal();
    }
    
    public double getSubtotal() { return subtotal; }
    
    private void updateSubtotal() {
        if (menuItem != null) {
            this.subtotal = menuItem.getPrice() * quantity;
        }
    }
    
    /**
     * Implementation of Billable interface
     */
    @Override
    public double getTotalCost() {
        return subtotal;
    }
    
    @Override
    public String getItemDetails() {
        return String.format("%s x %d = ₹%.2f", 
            menuItem.getItemName(), quantity, subtotal);
    }
    
    @Override
    public double applyDiscount(double discountPercentage) {
        return subtotal * (1 - discountPercentage / 100);
    }
}
