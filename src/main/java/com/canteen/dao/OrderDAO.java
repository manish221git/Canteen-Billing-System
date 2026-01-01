package com.canteen.dao;

import com.canteen.database.DatabaseManager;
import com.canteen.exception.DatabaseException;
import com.canteen.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Order operations
 */
public class OrderDAO implements GenericDAO<Order> {
    private DatabaseManager dbManager;
    private MenuItemDAO menuItemDAO;
    
    public OrderDAO() throws DatabaseException {
        this.dbManager = DatabaseManager.getInstance();
        this.menuItemDAO = new MenuItemDAO();
    }
    
    @Override
    public int insert(Order order) throws DatabaseException {
        String sql = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, order.getUserId());
            pstmt.setDouble(2, order.getTotalAmount());
            pstmt.setString(3, order.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        order.setOrderId(orderId);
                        
                        // Insert order items
                        insertOrderItems(orderId, order.getItems());
                        
                        return orderId;
                    }
                }
            }
            throw new DatabaseException("Failed to insert order");
            
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting order", e);
        }
    }
    
    /**
     * Insert order items for an order
     */
    private void insertOrderItems(int orderId, List<OrderItem> items) throws DatabaseException {
        String sql = "INSERT INTO order_items (order_id, item_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (OrderItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getMenuItem().getItemId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getSubtotal());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting order items", e);
        }
    }
    
    @Override
    public boolean update(Order order) throws DatabaseException {
        String sql = "UPDATE orders SET total_amount = ?, status = ? WHERE order_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, order.getTotalAmount());
            pstmt.setString(2, order.getStatus());
            pstmt.setInt(3, order.getOrderId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error updating order", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws DatabaseException {
        // First delete order items
        String deleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String deleteOrder = "DELETE FROM orders WHERE order_id = ?";
        
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteItems);
                 PreparedStatement pstmt2 = conn.prepareStatement(deleteOrder)) {
                
                pstmt1.setInt(1, id);
                pstmt1.executeUpdate();
                
                pstmt2.setInt(1, id);
                int result = pstmt2.executeUpdate();
                
                conn.commit();
                return result > 0;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting order", e);
        }
    }
    
    @Override
    public Order findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Order order = extractOrder(rs);
                order.setItems(findOrderItems(id));
                return order;
            }
            return null;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding order by ID", e);
        }
    }
    
    /**
     * Find order items for an order
     */
    private List<OrderItem> findOrderItems(int orderId) throws DatabaseException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                MenuItem menuItem = menuItemDAO.findById(rs.getInt("item_id"));
                OrderItem orderItem = new OrderItem(
                    rs.getInt("order_item_id"),
                    orderId,
                    menuItem,
                    rs.getInt("quantity")
                );
                items.add(orderItem);
            }
            return items;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding order items", e);
        }
    }
    
    @Override
    public List<Order> findAll() throws DatabaseException {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Order order = extractOrder(rs);
                order.setItems(findOrderItems(order.getOrderId()));
                orders.add(order);
            }
            return orders;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all orders", e);
        }
    }
    
    /**
     * Extract Order from ResultSet
     */
    private Order extractOrder(ResultSet rs) throws SQLException {
        Order order = new Order(
            rs.getInt("order_id"),
            rs.getInt("user_id")
        );
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        return order;
    }
}
