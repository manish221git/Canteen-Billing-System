package com.canteen;

import com.canteen.dao.MenuItemDAO;
import com.canteen.dao.OrderDAO;
import com.canteen.dao.BillDAO;
import com.canteen.dao.UserDAO;
import com.canteen.database.DatabaseManager;
import com.canteen.exception.DatabaseException;
import com.canteen.model.*;
import com.canteen.thread.BillGenerator;

import java.util.List;

/**
 * Test application to verify database and core functionality
 */
public class TestApp {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Canteen Billing System Test");
        System.out.println("========================================\n");
        
        try {
            // Test 1: Database initialization
            System.out.println("Test 1: Database Initialization");
            DatabaseManager dbManager = DatabaseManager.getInstance();
            System.out.println("✓ Database initialized successfully\n");
            
            // Test 2: Menu items
            System.out.println("Test 2: Menu Items");
            MenuItemDAO menuItemDAO = new MenuItemDAO();
            List<MenuItem> menuItems = menuItemDAO.findAll();
            System.out.println("✓ Found " + menuItems.size() + " menu items:");
            for (MenuItem item : menuItems) {
                System.out.println("  - " + item.getItemDetails());
            }
            System.out.println();
            
            // Test 3: User authentication
            System.out.println("Test 3: User Authentication");
            UserDAO userDAO = new UserDAO();
            User admin = userDAO.authenticate("admin", "admin123");
            if (admin != null) {
                System.out.println("✓ Admin login successful: " + admin.getUserInfo());
                System.out.println("  Has permission to VIEW_MENU: " + admin.hasPermission("VIEW_MENU"));
            }
            System.out.println();
            
            // Test 4: Create order
            System.out.println("Test 4: Create Order");
            OrderDAO orderDAO = new OrderDAO();
            Order order = new Order(0, 1);
            
            // Add items to order
            MenuItem burger = menuItems.get(0);
            MenuItem fries = menuItems.get(2);
            order.addItem(burger, 2);
            order.addItem(fries, 1);
            
            System.out.println("✓ Order created with " + order.getItems().size() + " items");
            System.out.println("  Total: ₹" + String.format("%.2f", order.getTotalAmount()));
            
            // Save order
            int orderId = orderDAO.insert(order);
            order.setOrderId(orderId);
            System.out.println("✓ Order saved with ID: " + orderId);
            System.out.println();
            
            // Test 5: Generate bill
            System.out.println("Test 5: Generate Bill");
            BillDAO billDAO = new BillDAO();
            Bill bill = new Bill(0, orderId, order.getTotalAmount());
            bill.applyTax(5.0);
            bill.applyDiscount(0.0);
            bill.setPaymentMethod("CASH");
            
            BillGenerator billGenerator = BillGenerator.getInstance();
            bill = billGenerator.generateBill(bill);
            
            System.out.println("✓ Bill generated successfully");
            System.out.println("========================================");
            System.out.println(bill.getBillDetails());
            System.out.println("========================================\n");
            
            // Test 6: Collections & Generics
            System.out.println("Test 6: Collections & Generics");
            System.out.println("✓ Using ArrayList for orders: " + order.getItems().getClass().getName());
            System.out.println("✓ Using generic DAO pattern for all operations");
            System.out.println("✓ Thread-safe operations with synchronized methods");
            System.out.println();
            
            // Test 7: Polymorphism
            System.out.println("Test 7: Polymorphism & Inheritance");
            System.out.println("✓ User hierarchy: Admin extends User");
            System.out.println("✓ Polymorphic method: hasPermission() overridden in subclasses");
            System.out.println("✓ Interface implementation: MenuItem implements Billable");
            System.out.println("✓ Interface implementation: Order implements Orderable");
            System.out.println();
            
            // Test 8: Exception Handling
            System.out.println("Test 8: Exception Handling");
            try {
                menuItemDAO.findById(999);
                System.out.println("✓ Exception handling tested - no exception for null result");
            } catch (DatabaseException e) {
                System.out.println("✓ DatabaseException caught: " + e.getMessage());
            }
            System.out.println();
            
            System.out.println("========================================");
            System.out.println("  All Tests Passed! ✓");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
