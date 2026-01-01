package com.canteen.servlet;

import com.canteen.dao.MenuItemDAO;
import com.canteen.exception.DatabaseException;
import com.canteen.model.MenuItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet for Menu Management
 */
public class MenuServlet extends HttpServlet {
    private MenuItemDAO menuItemDAO;
    
    @Override
    public void init() throws ServletException {
        try {
            menuItemDAO = new MenuItemDAO();
        } catch (DatabaseException e) {
            throw new ServletException("Failed to initialize MenuServlet", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        try {
            if ("list".equals(action)) {
                listMenuItems(request, response);
            } else if ("view".equals(action)) {
                viewMenuItem(request, response);
            } else if ("category".equals(action)) {
                listByCategory(request, response);
            } else {
                listMenuItems(request, response);
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
            if ("add".equals(action)) {
                addMenuItem(request, response);
            } else if ("update".equals(action)) {
                updateMenuItem(request, response);
            } else if ("delete".equals(action)) {
                deleteMenuItem(request, response);
            }
        } catch (DatabaseException e) {
            handleError(response, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * List all menu items
     */
    private void listMenuItems(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        List<MenuItem> items = menuItemDAO.findAll();
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        out.println("[");
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            out.println("  {");
            out.println("    \"itemId\": " + item.getItemId() + ",");
            out.println("    \"itemName\": \"" + item.getItemName() + "\",");
            out.println("    \"category\": \"" + item.getCategory() + "\",");
            out.println("    \"price\": " + item.getPrice() + ",");
            out.println("    \"available\": " + item.isAvailable());
            out.print("  }");
            if (i < items.size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("]");
    }
    
    /**
     * View single menu item
     */
    private void viewMenuItem(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int itemId = Integer.parseInt(request.getParameter("itemId"));
        MenuItem item = menuItemDAO.findById(itemId);
        
        if (item == null) {
            handleError(response, "Menu item not found");
            return;
        }
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("{");
        out.println("  \"itemId\": " + item.getItemId() + ",");
        out.println("  \"itemName\": \"" + item.getItemName() + "\",");
        out.println("  \"category\": \"" + item.getCategory() + "\",");
        out.println("  \"price\": " + item.getPrice() + ",");
        out.println("  \"available\": " + item.isAvailable());
        out.println("}");
    }
    
    /**
     * List items by category
     */
    private void listByCategory(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        String category = request.getParameter("category");
        List<MenuItem> items = menuItemDAO.findByCategory(category);
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        out.println("[");
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            out.println("  {");
            out.println("    \"itemId\": " + item.getItemId() + ",");
            out.println("    \"itemName\": \"" + item.getItemName() + "\",");
            out.println("    \"price\": " + item.getPrice());
            out.print("  }");
            if (i < items.size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("]");
    }
    
    /**
     * Add new menu item
     */
    private void addMenuItem(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        String itemName = request.getParameter("itemName");
        String category = request.getParameter("category");
        double price = Double.parseDouble(request.getParameter("price"));
        boolean available = Boolean.parseBoolean(request.getParameter("available"));
        
        MenuItem item = new MenuItem(0, itemName, category, price, available);
        int itemId = menuItemDAO.insert(item);
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("{\"status\": \"success\", \"itemId\": " + itemId + "}");
    }
    
    /**
     * Update menu item
     */
    private void updateMenuItem(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int itemId = Integer.parseInt(request.getParameter("itemId"));
        MenuItem item = menuItemDAO.findById(itemId);
        
        if (item == null) {
            handleError(response, "Menu item not found");
            return;
        }
        
        String itemName = request.getParameter("itemName");
        if (itemName != null) item.setItemName(itemName);
        
        String category = request.getParameter("category");
        if (category != null) item.setCategory(category);
        
        String priceStr = request.getParameter("price");
        if (priceStr != null) item.setPrice(Double.parseDouble(priceStr));
        
        String availableStr = request.getParameter("available");
        if (availableStr != null) item.setAvailable(Boolean.parseBoolean(availableStr));
        
        menuItemDAO.update(item);
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("{\"status\": \"success\", \"message\": \"Menu item updated\"}");
    }
    
    /**
     * Delete menu item
     */
    private void deleteMenuItem(HttpServletRequest request, HttpServletResponse response) 
            throws DatabaseException, IOException {
        
        int itemId = Integer.parseInt(request.getParameter("itemId"));
        boolean deleted = menuItemDAO.delete(itemId);
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if (deleted) {
            out.println("{\"status\": \"success\", \"message\": \"Menu item deleted\"}");
        } else {
            out.println("{\"status\": \"error\", \"message\": \"Failed to delete menu item\"}");
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
}
