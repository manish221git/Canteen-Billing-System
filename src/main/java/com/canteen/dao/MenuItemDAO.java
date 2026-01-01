package com.canteen.dao;

import com.canteen.database.DatabaseManager;
import com.canteen.exception.DatabaseException;
import com.canteen.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for MenuItem operations
 * Demonstrates database operations and JDBC usage
 */
public class MenuItemDAO implements GenericDAO<MenuItem> {
    private DatabaseManager dbManager;
    
    public MenuItemDAO() throws DatabaseException {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @Override
    public int insert(MenuItem item) throws DatabaseException {
        String sql = "INSERT INTO menu_items (item_name, category, price, available) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setInt(4, item.isAvailable() ? 1 : 0);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new DatabaseException("Failed to insert menu item");
            
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting menu item", e);
        }
    }
    
    @Override
    public boolean update(MenuItem item) throws DatabaseException {
        String sql = "UPDATE menu_items SET item_name = ?, category = ?, price = ?, available = ? WHERE item_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setInt(4, item.isAvailable() ? 1 : 0);
            pstmt.setInt(5, item.getItemId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error updating menu item", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM menu_items WHERE item_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting menu item", e);
        }
    }
    
    @Override
    public MenuItem findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM menu_items WHERE item_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractMenuItem(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding menu item by ID", e);
        }
    }
    
    @Override
    public List<MenuItem> findAll() throws DatabaseException {
        String sql = "SELECT * FROM menu_items";
        List<MenuItem> items = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                items.add(extractMenuItem(rs));
            }
            return items;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all menu items", e);
        }
    }
    
    /**
     * Find items by category
     */
    public List<MenuItem> findByCategory(String category) throws DatabaseException {
        String sql = "SELECT * FROM menu_items WHERE category = ?";
        List<MenuItem> items = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                items.add(extractMenuItem(rs));
            }
            return items;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding menu items by category", e);
        }
    }
    
    /**
     * Extract MenuItem from ResultSet
     */
    private MenuItem extractMenuItem(ResultSet rs) throws SQLException {
        return new MenuItem(
            rs.getInt("item_id"),
            rs.getString("item_name"),
            rs.getString("category"),
            rs.getDouble("price"),
            rs.getInt("available") == 1
        );
    }
}
