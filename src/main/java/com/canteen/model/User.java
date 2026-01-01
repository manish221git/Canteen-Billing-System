package com.canteen.model;

/**
 * Base class for all users in the system
 * Demonstrates Inheritance and Polymorphism
 * 
 * NOTE: For production systems, password should be:
 * 1. Hashed using BCrypt or PBKDF2
 * 2. Never stored or returned in plain text
 * 3. Compared using secure hash comparison
 * Plain text is used here for educational/demo purposes only
 */
public abstract class User {
    protected int userId;
    protected String username;
    protected String password; // TODO: Should be hashed in production
    protected String role;
    
    public User() {}
    
    public User(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    /**
     * Abstract method to demonstrate polymorphism
     * Each user type implements their own permissions
     */
    public abstract boolean hasPermission(String permission);
    
    /**
     * Method to be overridden by subclasses
     */
    public String getUserInfo() {
        return "User: " + username + " (" + role + ")";
    }
}
