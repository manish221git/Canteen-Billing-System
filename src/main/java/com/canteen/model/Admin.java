package com.canteen.model;

/**
 * Admin user class - demonstrates inheritance
 */
public class Admin extends User {
    private String department;
    
    public Admin() {
        super();
        this.role = "ADMIN";
    }
    
    public Admin(int userId, String username, String password, String department) {
        super(userId, username, password, "ADMIN");
        this.department = department;
    }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    /**
     * Admin has all permissions - demonstrates polymorphism
     */
    @Override
    public boolean hasPermission(String permission) {
        return true; // Admin has all permissions
    }
    
    /**
     * Override getUserInfo to provide admin-specific information
     */
    @Override
    public String getUserInfo() {
        return "Admin: " + username + " - Department: " + department;
    }
}
