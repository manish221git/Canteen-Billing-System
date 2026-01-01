package com.canteen.model;

import java.util.List;

/**
 * Interface for order operations
 */
public interface Orderable {
    /**
     * Add item to order
     * @param item menu item
     * @param quantity quantity
     */
    void addItem(MenuItem item, int quantity);
    
    /**
     * Remove item from order
     * @param itemId item ID
     */
    void removeItem(int itemId);
    
    /**
     * Get all items in order
     * @return list of order items
     */
    List<OrderItem> getItems();
    
    /**
     * Calculate total order amount
     * @return total amount
     */
    double calculateTotal();
}
