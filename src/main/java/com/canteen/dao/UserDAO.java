package com.canteen.dao;

import com.canteen.database.DatabaseManager;
import com.canteen.exception.DatabaseException;
import com.canteen.model.Admin;
import com.canteen.model.Staff;
import com.canteen.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for User operations
 * Demonstrates polymorphism and factory pattern
 */
public class UserDAO implements GenericDAO<User> {
    private DatabaseManager dbManager;
    
    public UserDAO() throws DatabaseException {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @Override
    public int insert(User user) throws DatabaseException {
        String sql = "INSERT INTO users (username, password, role, extra_info) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            
            // Store type-specific info
            if (user instanceof Admin) {
                pstmt.setString(4, ((Admin) user).getDepartment());
            } else if (user instanceof Staff) {
                pstmt.setString(4, ((Staff) user).getShift());
            } else {
                pstmt.setString(4, null);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new DatabaseException("Failed to insert user");
            
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting user", e);
        }
    }
    
    @Override
    public boolean update(User user) throws DatabaseException {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?, extra_info = ? WHERE user_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            
            if (user instanceof Admin) {
                pstmt.setString(4, ((Admin) user).getDepartment());
            } else if (user instanceof Staff) {
                pstmt.setString(4, ((Staff) user).getShift());
            } else {
                pstmt.setString(4, null);
            }
            
            pstmt.setInt(5, user.getUserId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting user", e);
        }
    }
    
    @Override
    public User findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUser(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by ID", e);
        }
    }
    
    @Override
    public List<User> findAll() throws DatabaseException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUser(rs));
            }
            return users;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all users", e);
        }
    }
    
    /**
     * Find user by username
     */
    public User findByUsername(String username) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUser(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username", e);
        }
    }
    
    /**
     * Authenticate user
     */
    public User authenticate(String username, String password) throws DatabaseException {
        User user = findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
    /**
     * Extract User from ResultSet - demonstrates Factory pattern
     */
    private User extractUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String role = rs.getString("role");
        String extraInfo = rs.getString("extra_info");
        
        User user;
        
        // Factory pattern: create appropriate User subclass
        if ("ADMIN".equals(role)) {
            user = new Admin(userId, username, password, extraInfo);
        } else if ("STAFF".equals(role)) {
            user = new Staff(userId, username, password, extraInfo);
        } else {
            // Generic user (shouldn't happen with current design)
            throw new SQLException("Unknown user role: " + role);
        }
        
        return user;
    }
}
