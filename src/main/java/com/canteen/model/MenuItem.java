package com.canteen.model;

/**
 * MenuItem class implementing Billable interface
 * Demonstrates Interface implementation and OOP
 */
public class MenuItem implements Billable {
    private int itemId;
    private String itemName;
    private String category;
    private double price;
    private boolean available;
    
    public MenuItem() {}
    
    public MenuItem(int itemId, String itemName, String category, double price, boolean available) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.price = price;
        this.available = available;
    }
    
    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    /**
     * Implementation of Billable interface
     */
    @Override
    public double getTotalCost() {
        return price;
    }
    
    @Override
    public String getItemDetails() {
        return String.format("%s (%s) - ₹%.2f", itemName, category, price);
    }
    
    @Override
    public double applyDiscount(double discountPercentage) {
        return price * (1 - discountPercentage / 100);
    }
    
    @Override
    public String toString() {
        return getItemDetails();
    }
}
