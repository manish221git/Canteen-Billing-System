package com.canteen.model;

import java.util.Arrays;
import java.util.List;

/**
 * Staff user class - demonstrates inheritance
 */
public class Staff extends User {
    private String shift;
    private static final List<String> STAFF_PERMISSIONS = Arrays.asList(
        "VIEW_MENU", "CREATE_ORDER", "VIEW_ORDER", "GENERATE_BILL"
    );
    
    public Staff() {
        super();
        this.role = "STAFF";
    }
    
    public Staff(int userId, String username, String password, String shift) {
        super(userId, username, password, "STAFF");
        this.shift = shift;
    }
    
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    
    /**
     * Staff has limited permissions - demonstrates polymorphism
     */
    @Override
    public boolean hasPermission(String permission) {
        return STAFF_PERMISSIONS.contains(permission);
    }
    
    /**
     * Override getUserInfo for staff-specific information
     */
    @Override
    public String getUserInfo() {
        return "Staff: " + username + " - Shift: " + shift;
    }
}
