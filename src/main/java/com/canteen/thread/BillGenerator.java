package com.canteen.thread;

import com.canteen.dao.BillDAO;
import com.canteen.exception.DatabaseException;
import com.canteen.model.Bill;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe Bill Generator
 * Demonstrates Multithreading with synchronization
 */
public class BillGenerator implements Runnable {
    private static BillGenerator instance;
    private ConcurrentHashMap<Integer, Bill> billCache; // Thread-safe map
    private BillDAO billDAO;
    private AtomicInteger billCounter; // Thread-safe counter
    private volatile boolean running = true;
    
    private BillGenerator() {
        this.billCache = new ConcurrentHashMap<>();
        this.billCounter = new AtomicInteger(0);
        try {
            this.billDAO = new BillDAO();
        } catch (DatabaseException e) {
            System.err.println("Failed to initialize BillDAO: " + e.getMessage());
        }
    }
    
    /**
     * Singleton pattern
     */
    public static synchronized BillGenerator getInstance() {
        if (instance == null) {
            instance = new BillGenerator();
        }
        return instance;
    }
    
    /**
     * Generate bill - synchronized method
     */
    public synchronized Bill generateBill(Bill bill) throws DatabaseException {
        try {
            // Simulate bill generation process
            Thread.sleep(500);
            
            // Save to database
            int billId = billDAO.insert(bill);
            bill.setBillId(billId);
            
            // Cache the bill
            billCache.put(billId, bill);
            billCounter.incrementAndGet();
            
            System.out.println("Bill " + billId + " generated successfully");
            return bill;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabaseException("Bill generation interrupted");
        }
    }
    
    /**
     * Get bill from cache or database
     */
    public Bill getBill(int billId) throws DatabaseException {
        // Check cache first
        Bill bill = billCache.get(billId);
        if (bill != null) {
            return bill;
        }
        
        // Fetch from database if not in cache
        bill = billDAO.findById(billId);
        if (bill != null) {
            billCache.put(billId, bill);
        }
        return bill;
    }
    
    /**
     * Background cleanup task
     */
    @Override
    public void run() {
        System.out.println("BillGenerator cleanup thread started");
        
        while (running) {
            try {
                // Periodic cache cleanup
                Thread.sleep(60000); // Every minute
                
                if (billCache.size() > 100) {
                    synchronized (this) {
                        // Keep only recent 50 bills
                        int toRemove = billCache.size() - 50;
                        billCache.keySet().stream()
                            .limit(toRemove)
                            .forEach(billCache::remove);
                    }
                    System.out.println("Bill cache cleaned");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("BillGenerator cleanup thread stopped");
    }
    
    /**
     * Get total bills generated
     */
    public int getTotalBillsGenerated() {
        return billCounter.get();
    }
    
    /**
     * Stop the generator
     */
    public void stop() {
        running = false;
    }
}
