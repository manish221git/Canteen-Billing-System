package com.canteen.dao;

import com.canteen.database.DatabaseManager;
import com.canteen.exception.DatabaseException;
import com.canteen.model.Bill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Bill operations
 */
public class BillDAO implements GenericDAO<Bill> {
    private DatabaseManager dbManager;
    
    public BillDAO() throws DatabaseException {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @Override
    public int insert(Bill bill) throws DatabaseException {
        String sql = "INSERT INTO bills (order_id, total_amount, tax_amount, discount_amount, final_amount, payment_method) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, bill.getOrderId());
            pstmt.setDouble(2, bill.getTotalAmount());
            pstmt.setDouble(3, bill.getTaxAmount());
            pstmt.setDouble(4, bill.getDiscountAmount());
            pstmt.setDouble(5, bill.getFinalAmount());
            pstmt.setString(6, bill.getPaymentMethod());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new DatabaseException("Failed to insert bill");
            
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting bill", e);
        }
    }
    
    @Override
    public boolean update(Bill bill) throws DatabaseException {
        String sql = "UPDATE bills SET total_amount = ?, tax_amount = ?, discount_amount = ?, " +
                    "final_amount = ?, payment_method = ? WHERE bill_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, bill.getTotalAmount());
            pstmt.setDouble(2, bill.getTaxAmount());
            pstmt.setDouble(3, bill.getDiscountAmount());
            pstmt.setDouble(4, bill.getFinalAmount());
            pstmt.setString(5, bill.getPaymentMethod());
            pstmt.setInt(6, bill.getBillId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error updating bill", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM bills WHERE bill_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting bill", e);
        }
    }
    
    @Override
    public Bill findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractBill(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding bill by ID", e);
        }
    }
    
    @Override
    public List<Bill> findAll() throws DatabaseException {
        String sql = "SELECT * FROM bills ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                bills.add(extractBill(rs));
            }
            return bills;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all bills", e);
        }
    }
    
    /**
     * Find bill by order ID
     */
    public Bill findByOrderId(int orderId) throws DatabaseException {
        String sql = "SELECT * FROM bills WHERE order_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractBill(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error finding bill by order ID", e);
        }
    }
    
    /**
     * Extract Bill from ResultSet
     */
    private Bill extractBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill(
            rs.getInt("bill_id"),
            rs.getInt("order_id"),
            rs.getDouble("total_amount")
        );
        bill.setTaxAmount(rs.getDouble("tax_amount"));
        bill.setDiscountAmount(rs.getDouble("discount_amount"));
        bill.setBillDate(rs.getTimestamp("bill_date"));
        bill.setPaymentMethod(rs.getString("payment_method"));
        return bill;
    }
}
