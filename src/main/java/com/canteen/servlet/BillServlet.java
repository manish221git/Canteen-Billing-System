package com.canteen.servlet;

import com.canteen.dao.BillDAO;
import com.canteen.dao.OrderDAO;
import com.canteen.exception.DatabaseException;
import com.canteen.model.Bill;
import com.canteen.model.Order;
import com.canteen.thread.BillGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet for Bill Management
 * Demonstrates Servlet Implementation
 */
public class BillServlet extends HttpServlet {
    private BillDAO billDAO;
    private OrderDAO orderDAO;
    private BillGenerator billGenerator;
    
    @Override
    public void init() throws ServletException {
        try {
            billDAO = new BillDAO();
            orderDAO = new OrderDAO();
            billGenerator = BillGenerator.getInstance();
            
            // Start bill generator cleanup thread
            Thread generatorThread = new Thread(billGenerator);
            generatorThread.setDaemon(true);
            generatorThread.start();
            
        } catch (DatabaseException e) {
            throw new ServletException("Failed to initialize BillServlet", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            if ("list".equals(action)) {
                listBills(request, response);
            } else if ("view".equals(action)) {
                viewBill(request, response);
            } else if ("viewByOrder".equals(action)) {
                viewBillByOrder(request, response);
            } else {
                listBills(request, response);
            }
        } catch (DatabaseException e) {
            handleError(response, "Database error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            if ("generate".equals(action)) {
                generateBill(request, response);
            } else if ("update".equals(action)) {
                updateBill(request, response);
            }
        } catch (DatabaseException e) {
            handleError(response, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * Generate new bill
     */
    private void generateBill(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        Order order = orderDAO.findById(orderId);
        
        if (order == null) {
            handleError(response, "Order not found");
            return;
        }
        
        // Create bill
        Bill bill = new Bill(0, orderId, order.getTotalAmount());
        
        // Apply tax and discount if provided
        String taxStr = request.getParameter("taxPercentage");
        if (taxStr != null && !taxStr.isEmpty()) {
            double taxPercentage = Double.parseDouble(taxStr);
            bill.applyTax(taxPercentage);
        } else {
            bill.applyTax(5.0); // Default 5% tax
        }
        
        String discountStr = request.getParameter("discountPercentage");
        if (discountStr != null && !discountStr.isEmpty()) {
            double discountPercentage = Double.parseDouble(discountStr);
            bill.applyDiscount(discountPercentage);
        }
        
        String paymentMethod = request.getParameter("paymentMethod");
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            bill.setPaymentMethod(paymentMethod);
        } else {
            bill.setPaymentMethod("CASH");
        }
        
        // Generate bill using thread-safe generator
        bill = billGenerator.generateBill(bill);
        
        // Update order status
        order.setStatus("BILLED");
        orderDAO.update(order);
        
        // Send response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("{");
        out.println("  \"status\": \"success\",");
        out.println("  \"billId\": " + bill.getBillId() + ",");
        out.println("  \"finalAmount\": " + bill.getFinalAmount() + ",");
        out.println("  \"message\": \"Bill generated successfully\"");
        out.println("}");
    }
    
    /**
     * List all bills
     */
    private void listBills(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        List<Bill> bills = billDAO.findAll();
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        out.println("[");
        for (int i = 0; i < bills.size(); i++) {
            Bill bill = bills.get(i);
            out.println("  {");
            out.println("    \"billId\": " + bill.getBillId() + ",");
            out.println("    \"orderId\": " + bill.getOrderId() + ",");
            out.println("    \"totalAmount\": " + bill.getTotalAmount() + ",");
            out.println("    \"taxAmount\": " + bill.getTaxAmount() + ",");
            out.println("    \"discountAmount\": " + bill.getDiscountAmount() + ",");
            out.println("    \"finalAmount\": " + bill.getFinalAmount() + ",");
            out.println("    \"paymentMethod\": \"" + bill.getPaymentMethod() + "\",");
            out.println("    \"billDate\": \"" + bill.getBillDate() + "\"");
            out.print("  }");
            if (i < bills.size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("]");
    }
    
    /**
     * View single bill
     */
    private void viewBill(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int billId = Integer.parseInt(request.getParameter("billId"));
        Bill bill = billGenerator.getBill(billId); // Use cached version
        
        if (bill == null) {
            handleError(response, "Bill not found");
            return;
        }
        
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("========================================");
        out.println("         CANTEEN BILL RECEIPT          ");
        out.println("========================================");
        out.println(bill.getBillDetails());
        out.println("========================================");
        out.println("      Thank you for your order!        ");
        out.println("========================================");
    }
    
    /**
     * View bill by order ID
     */
    private void viewBillByOrder(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        Bill bill = billDAO.findByOrderId(orderId);
        
        if (bill == null) {
            handleError(response, "Bill not found for this order");
            return;
        }
        
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println(bill.getBillDetails());
    }
    
    /**
     * Update bill
     */
    private void updateBill(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int billId = Integer.parseInt(request.getParameter("billId"));
        Bill bill = billDAO.findById(billId);
        
        if (bill == null) {
            handleError(response, "Bill not found");
            return;
        }
        
        String paymentMethod = request.getParameter("paymentMethod");
        if (paymentMethod != null) {
            bill.setPaymentMethod(paymentMethod);
            billDAO.update(bill);
            
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{\"status\": \"success\", \"message\": \"Bill updated\"}");
        } else {
            handleError(response, "Payment method required");
        }
    }
    
    /**
     * Handle errors
     */
    private void handleError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        PrintWriter out = response.getWriter();
        out.println("{\"status\": \"error\", \"message\": \"" + message + "\"}");
    }
    
    @Override
    public void destroy() {
        if (billGenerator != null) {
            billGenerator.stop();
        }
    }
}
