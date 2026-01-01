package com.canteen.thread;

import com.canteen.dao.OrderDAO;
import com.canteen.exception.DatabaseException;
import com.canteen.model.Order;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread-safe Order Processor
 * Demonstrates Multithreading & Synchronization
 */
public class OrderProcessor implements Runnable {
    private BlockingQueue<Order> orderQueue;
    private OrderDAO orderDAO;
    private volatile boolean running = true;
    private static OrderProcessor instance;
    
    private OrderProcessor() {
        this.orderQueue = new LinkedBlockingQueue<>();
        try {
            this.orderDAO = new OrderDAO();
        } catch (DatabaseException e) {
            System.err.println("Failed to initialize OrderDAO: " + e.getMessage());
        }
    }
    
    /**
     * Singleton pattern for thread safety
     */
    public static synchronized OrderProcessor getInstance() {
        if (instance == null) {
            instance = new OrderProcessor();
        }
        return instance;
    }
    
    /**
     * Add order to processing queue - thread-safe
     */
    public synchronized void addOrder(Order order) {
        try {
            orderQueue.put(order);
            System.out.println("Order " + order.getOrderId() + " added to queue");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to add order to queue: " + e.getMessage());
        }
    }
    
    /**
     * Process orders from queue
     */
    @Override
    public void run() {
        System.out.println("OrderProcessor thread started");
        
        while (running) {
            try {
                // Take order from queue (blocks if empty)
                Order order = orderQueue.take();
                
                // Process the order
                processOrder(order);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("OrderProcessor interrupted");
                break;
            }
        }
        
        System.out.println("OrderProcessor thread stopped");
    }
    
    /**
     * Process individual order - synchronized for thread safety
     */
    private synchronized void processOrder(Order order) {
        try {
            System.out.println("Processing order " + order.getOrderId() + "...");
            
            // Simulate processing time
            Thread.sleep(1000);
            
            // Update order status
            order.setStatus("PROCESSING");
            orderDAO.update(order);
            
            // Additional processing
            Thread.sleep(1000);
            
            // Mark as completed
            order.setStatus("COMPLETED");
            orderDAO.update(order);
            
            System.out.println("Order " + order.getOrderId() + " completed");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Order processing interrupted");
        } catch (DatabaseException e) {
            System.err.println("Failed to update order: " + e.getMessage());
        }
    }
    
    /**
     * Stop the processor
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Get queue size
     */
    public int getQueueSize() {
        return orderQueue.size();
    }
}
