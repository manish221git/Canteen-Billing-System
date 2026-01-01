package com.canteen.servlet;

import com.canteen.dao.MenuItemDAO;
import com.canteen.dao.OrderDAO;
import com.canteen.exception.DatabaseException;
import com.canteen.model.MenuItem;
import com.canteen.model.Order;
import com.canteen.thread.OrderProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet for Order Management
 * Demonstrates Servlet Implementation
 */
public class OrderServlet extends HttpServlet {
    private OrderDAO orderDAO;
    private MenuItemDAO menuItemDAO;
    private OrderProcessor orderProcessor;
    
    @Override
    public void init() throws ServletException {
        try {
            orderDAO = new OrderDAO();
            menuItemDAO = new MenuItemDAO();
            orderProcessor = OrderProcessor.getInstance();
            
            // Start order processor thread
            Thread processorThread = new Thread(orderProcessor);
            processorThread.setDaemon(true);
            processorThread.start();
            
        } catch (DatabaseException e) {
            throw new ServletException("Failed to initialize OrderServlet", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            if ("list".equals(action)) {
                listOrders(request, response);
            } else if ("view".equals(action)) {
                viewOrder(request, response);
            } else {
                listOrders(request, response);
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
            if ("create".equals(action)) {
                createOrder(request, response);
            } else if ("update".equals(action)) {
                updateOrder(request, response);
            } else if ("delete".equals(action)) {
                deleteOrder(request, response);
            }
        } catch (DatabaseException e) {
            handleError(response, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * Create new order
     */
    private void createOrder(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            userId = 1; // Default to admin
        }
        
        Order order = new Order(0, userId);
        
        // Parse items from request
        String[] itemIds = request.getParameterValues("itemId");
        String[] quantities = request.getParameterValues("quantity");
        
        if (itemIds != null && quantities != null) {
            for (int i = 0; i < itemIds.length; i++) {
                int itemId = Integer.parseInt(itemIds[i]);
                int quantity = Integer.parseInt(quantities[i]);
                
                MenuItem item = menuItemDAO.findById(itemId);
                if (item != null && item.isAvailable()) {
                    order.addItem(item, quantity);
                }
            }
        }
        
        // Save order
        int orderId = orderDAO.insert(order);
        order.setOrderId(orderId);
        
        // Add to processing queue
        orderProcessor.addOrder(order);
        
        // Send response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("{");
        out.println("  \"status\": \"success\",");
        out.println("  \"orderId\": " + orderId + ",");
        out.println("  \"message\": \"Order created successfully\"");
        out.println("}");
    }
    
    /**
     * List all orders
     */
    private void listOrders(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        List<Order> orders = orderDAO.findAll();
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        out.println("[");
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            out.println("  {");
            out.println("    \"orderId\": " + order.getOrderId() + ",");
            out.println("    \"userId\": " + order.getUserId() + ",");
            out.println("    \"totalAmount\": " + order.getTotalAmount() + ",");
            out.println("    \"status\": \"" + order.getStatus() + "\",");
            out.println("    \"orderDate\": \"" + order.getOrderDate() + "\"");
            out.print("  }");
            if (i < orders.size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("]");
    }
    
    /**
     * View single order
     */
    private void viewOrder(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        Order order = orderDAO.findById(orderId);
        
        if (order == null) {
            handleError(response, "Order not found");
            return;
        }
        
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println(order.getOrderSummary());
    }
    
    /**
     * Update order
     */
    private void updateOrder(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        String status = request.getParameter("status");
        
        Order order = orderDAO.findById(orderId);
        if (order != null) {
            order.setStatus(status);
            orderDAO.update(order);
            
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{\"status\": \"success\", \"message\": \"Order updated\"}");
        } else {
            handleError(response, "Order not found");
        }
    }
    
    /**
     * Delete order
     */
    private void deleteOrder(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        boolean deleted = orderDAO.delete(orderId);
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if (deleted) {
            out.println("{\"status\": \"success\", \"message\": \"Order deleted\"}");
        } else {
            out.println("{\"status\": \"error\", \"message\": \"Failed to delete order\"}");
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
        if (orderProcessor != null) {
            orderProcessor.stop();
        }
    }
}
