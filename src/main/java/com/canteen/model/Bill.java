package com.canteen.model;

import java.sql.Timestamp;

/**
 * Bill class for generating bills
 */
public class Bill {
    private int billId;
    private int orderId;
    private double totalAmount;
    private double taxAmount;
    private double discountAmount;
    private double finalAmount;
    private Timestamp billDate;
    private String paymentMethod;
    
    public Bill() {
        this.billDate = new Timestamp(System.currentTimeMillis());
    }
    
    public Bill(int billId, int orderId, double totalAmount) {
        this();
        this.billId = billId;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        calculateFinalAmount();
    }
    
    // Getters and Setters
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { 
        this.totalAmount = totalAmount;
        calculateFinalAmount();
    }
    
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { 
        this.taxAmount = taxAmount;
        calculateFinalAmount();
    }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { 
        this.discountAmount = discountAmount;
        calculateFinalAmount();
    }
    
    public double getFinalAmount() { return finalAmount; }
    
    public Timestamp getBillDate() { return billDate; }
    public void setBillDate(Timestamp billDate) { this.billDate = billDate; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    /**
     * Calculate final amount with tax and discount
     */
    private void calculateFinalAmount() {
        finalAmount = totalAmount + taxAmount - discountAmount;
    }
    
    /**
     * Apply tax percentage
     */
    public void applyTax(double taxPercentage) {
        this.taxAmount = totalAmount * (taxPercentage / 100);
        calculateFinalAmount();
    }
    
    /**
     * Apply discount percentage
     */
    public void applyDiscount(double discountPercentage) {
        this.discountAmount = totalAmount * (discountPercentage / 100);
        calculateFinalAmount();
    }
    
    public String getBillDetails() {
        return String.format(
            "Bill ID: %d\nOrder ID: %d\nSubtotal: ₹%.2f\nTax: ₹%.2f\nDiscount: ₹%.2f\n" +
            "Final Amount: ₹%.2f\nPayment: %s\nDate: %s",
            billId, orderId, totalAmount, taxAmount, discountAmount, finalAmount, 
            paymentMethod != null ? paymentMethod : "N/A", billDate
        );
    }
}
