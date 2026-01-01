package com.canteen.model;

/**
 * Interface for billable items
 * Demonstrates Interface implementation requirement
 */
public interface Billable {
    /**
     * Get the total cost of the item
     * @return total cost
     */
    double getTotalCost();
    
    /**
     * Get item details for billing
     * @return formatted item details
     */
    String getItemDetails();
    
    /**
     * Apply discount to the item
     * @param discountPercentage discount percentage
     * @return discounted price
     */
    double applyDiscount(double discountPercentage);
}
