package com.canteen.database;

import com.canteen.exception.DatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database Connection Manager
 * Demonstrates JDBC connectivity and database operations
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:canteen.db";
    private Connection connection;
    
    private DatabaseManager() throws DatabaseException {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("SQLite JDBC driver not found", e);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to connect to database", e);
        }
    }
    
    /**
     * Singleton pattern to get database instance
     */
    public static synchronized DatabaseManager getInstance() throws DatabaseException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get database connection", e);
        }
    }
    
    /**
     * Initialize database tables
     */
    private void initializeDatabase() throws DatabaseException {
        try (Statement stmt = connection.createStatement()) {
            // Create Users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "extra_info TEXT)"
            );
            
            // Create MenuItems table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS menu_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_name TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "available INTEGER NOT NULL DEFAULT 1)"
            );
            
            // Create Orders table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "total_amount REAL NOT NULL, " +
                "status TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id))"
            );
            
            // Create OrderItems table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS order_items (" +
                "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER NOT NULL, " +
                "item_id INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "subtotal REAL NOT NULL, " +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id), " +
                "FOREIGN KEY (item_id) REFERENCES menu_items(item_id))"
            );
            
            // Create Bills table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bills (" +
                "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER NOT NULL, " +
                "total_amount REAL NOT NULL, " +
                "tax_amount REAL NOT NULL DEFAULT 0, " +
                "discount_amount REAL NOT NULL DEFAULT 0, " +
                "final_amount REAL NOT NULL, " +
                "bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "payment_method TEXT, " +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id))"
            );
            
            // Insert default admin user if not exists
            // NOTE: For production, password should be hashed using BCrypt or similar
            // Plain text password is used here for educational/demo purposes only
            stmt.execute(
                "INSERT OR IGNORE INTO users (user_id, username, password, role, extra_info) " +
                "VALUES (1, 'admin', 'admin123', 'ADMIN', 'Management')"
            );
            
            // Insert sample menu items if not exists
            stmt.execute(
                "INSERT OR IGNORE INTO menu_items (item_id, item_name, category, price) VALUES " +
                "(1, 'Veg Burger', 'Fast Food', 80.00), " +
                "(2, 'Chicken Burger', 'Fast Food', 120.00), " +
                "(3, 'French Fries', 'Sides', 60.00), " +
                "(4, 'Cold Coffee', 'Beverages', 70.00), " +
                "(5, 'Pizza', 'Fast Food', 150.00)"
            );
            
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database", e);
        }
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() throws DatabaseException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to close database connection", e);
        }
    }
}
