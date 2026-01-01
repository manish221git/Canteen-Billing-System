package com.canteen.gui;

import com.canteen.dao.*;
import com.canteen.exception.DatabaseException;
import com.canteen.model.*;
import com.canteen.thread.BillGenerator;
import com.canteen.thread.OrderProcessor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main GUI for Canteen Billing System
 * Demonstrates GUI implementation with Swing
 */
public class CanteenGUI extends JFrame {
    private MenuItemDAO menuItemDAO;
    private OrderDAO orderDAO;
    private BillDAO billDAO;
    
    private JTabbedPane tabbedPane;
    private DefaultTableModel menuTableModel;
    private DefaultTableModel orderTableModel;
    private Map<Integer, Integer> orderItemsMap; // itemId -> quantity
    private Order currentOrder;
    
    private OrderProcessor orderProcessor;
    private BillGenerator billGenerator;
    
    public CanteenGUI() {
        try {
            menuItemDAO = new MenuItemDAO();
            orderDAO = new OrderDAO();
            billDAO = new BillDAO();
            
            orderItemsMap = new HashMap<>();
            currentOrder = new Order(0, 1); // Default user ID 1
            
            // Initialize threads
            orderProcessor = OrderProcessor.getInstance();
            billGenerator = BillGenerator.getInstance();
            
            Thread processorThread = new Thread(orderProcessor);
            processorThread.setDaemon(true);
            processorThread.start();
            
            Thread generatorThread = new Thread(billGenerator);
            generatorThread.setDaemon(true);
            generatorThread.start();
            
            initComponents();
            loadMenuItems();
            
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to initialize database: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    /**
     * Initialize GUI components
     */
    private void initComponents() {
        setTitle("Canteen Billing System");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Menu", createMenuPanel());
        tabbedPane.addTab("Place Order", createOrderPanel());
        tabbedPane.addTab("View Orders", createViewOrdersPanel());
        tabbedPane.addTab("Generate Bill", createBillPanel());
        
        add(tabbedPane);
    }
    
    /**
     * Create Menu panel
     */
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Menu Items", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Name", "Category", "Price (₹)", "Available"};
        menuTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable menuTable = new JTable(menuTableModel);
        menuTable.setRowHeight(25);
        menuTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(menuTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Menu");
        refreshButton.addActionListener(e -> loadMenuItems());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create Order panel
     */
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel titleLabel = new JLabel("Place Order", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Center panel with order details
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Order items table
        String[] columns = {"Item", "Quantity", "Price", "Subtotal"};
        orderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable orderTable = new JTable(orderTableModel);
        orderTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalPanel.add(totalLabel);
        centerPanel.add(totalPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with item selection
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Item:"));
        
        JComboBox<String> itemComboBox = new JComboBox<>();
        itemComboBox.setPreferredSize(new Dimension(200, 25));
        selectionPanel.add(itemComboBox);
        
        selectionPanel.add(new JLabel("Quantity:"));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        selectionPanel.add(quantitySpinner);
        
        JButton addButton = new JButton("Add to Order");
        addButton.addActionListener(e -> {
            String selected = (String) itemComboBox.getSelectedItem();
            if (selected != null) {
                int itemId = Integer.parseInt(selected.split(" - ")[0]);
                int quantity = (Integer) quantitySpinner.getValue();
                addItemToOrder(itemId, quantity, totalLabel);
            }
        });
        selectionPanel.add(addButton);
        
        bottomPanel.add(selectionPanel);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton clearButton = new JButton("Clear Order");
        clearButton.addActionListener(e -> clearOrder(totalLabel));
        buttonsPanel.add(clearButton);
        
        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(e -> placeOrder(totalLabel));
        buttonsPanel.add(placeOrderButton);
        
        bottomPanel.add(buttonsPanel);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Load items into combo box
        try {
            List<MenuItem> items = menuItemDAO.findAll();
            for (MenuItem item : items) {
                if (item.isAvailable()) {
                    itemComboBox.addItem(item.getItemId() + " - " + item.getItemName() + 
                                       " (₹" + item.getPrice() + ")");
                }
            }
        } catch (DatabaseException e) {
            showError("Failed to load menu items: " + e.getMessage());
        }
        
        return panel;
    }
    
    /**
     * Create View Orders panel
     */
    private JPanel createViewOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("All Orders", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JTextArea ordersTextArea = new JTextArea();
        ordersTextArea.setEditable(false);
        ordersTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(ordersTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshButton = new JButton("Refresh Orders");
        refreshButton.addActionListener(e -> {
            try {
                List<Order> orders = orderDAO.findAll();
                StringBuilder sb = new StringBuilder();
                for (Order order : orders) {
                    sb.append(order.getOrderSummary()).append("\n\n");
                    sb.append("----------------------------------------\n\n");
                }
                ordersTextArea.setText(sb.toString());
            } catch (DatabaseException ex) {
                showError("Failed to load orders: " + ex.getMessage());
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create Bill panel
     */
    private JPanel createBillPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Generate Bill", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        formPanel.add(new JLabel("Order ID:"));
        JTextField orderIdField = new JTextField();
        formPanel.add(orderIdField);
        
        formPanel.add(new JLabel("Tax Percentage:"));
        JTextField taxField = new JTextField("5.0");
        formPanel.add(taxField);
        
        formPanel.add(new JLabel("Discount Percentage:"));
        JTextField discountField = new JTextField("0.0");
        formPanel.add(discountField);
        
        formPanel.add(new JLabel("Payment Method:"));
        JComboBox<String> paymentComboBox = new JComboBox<>(
            new String[]{"CASH", "CARD", "UPI", "WALLET"}
        );
        formPanel.add(paymentComboBox);
        
        JButton generateButton = new JButton("Generate Bill");
        formPanel.add(new JLabel(""));
        formPanel.add(generateButton);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Bill display area
        JTextArea billTextArea = new JTextArea();
        billTextArea.setEditable(false);
        billTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(billTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        generateButton.addActionListener(e -> {
            try {
                int orderId = Integer.parseInt(orderIdField.getText());
                double taxPercentage = Double.parseDouble(taxField.getText());
                double discountPercentage = Double.parseDouble(discountField.getText());
                String paymentMethod = (String) paymentComboBox.getSelectedItem();
                
                Order order = orderDAO.findById(orderId);
                if (order == null) {
                    showError("Order not found!");
                    return;
                }
                
                Bill bill = new Bill(0, orderId, order.getTotalAmount());
                bill.applyTax(taxPercentage);
                bill.applyDiscount(discountPercentage);
                bill.setPaymentMethod(paymentMethod);
                
                // Use thread-safe bill generator
                bill = billGenerator.generateBill(bill);
                
                // Update order status
                order.setStatus("BILLED");
                orderDAO.update(order);
                
                // Display bill
                StringBuilder billDisplay = new StringBuilder();
                billDisplay.append("========================================\n");
                billDisplay.append("       CANTEEN BILL RECEIPT\n");
                billDisplay.append("========================================\n\n");
                billDisplay.append(order.getOrderSummary()).append("\n\n");
                billDisplay.append("========================================\n\n");
                billDisplay.append(bill.getBillDetails()).append("\n\n");
                billDisplay.append("========================================\n");
                billDisplay.append("    Thank you for your order!\n");
                billDisplay.append("========================================\n");
                
                billTextArea.setText(billDisplay.toString());
                
                JOptionPane.showMessageDialog(this,
                    "Bill generated successfully!\nBill ID: " + bill.getBillId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (NumberFormatException ex) {
                showError("Invalid input: " + ex.getMessage());
            } catch (DatabaseException ex) {
                showError("Failed to generate bill: " + ex.getMessage());
            }
        });
        
        return panel;
    }
    
    /**
     * Load menu items into table
     */
    private void loadMenuItems() {
        try {
            List<MenuItem> items = menuItemDAO.findAll();
            menuTableModel.setRowCount(0);
            
            for (MenuItem item : items) {
                menuTableModel.addRow(new Object[]{
                    item.getItemId(),
                    item.getItemName(),
                    item.getCategory(),
                    String.format("%.2f", item.getPrice()),
                    item.isAvailable() ? "Yes" : "No"
                });
            }
        } catch (DatabaseException e) {
            showError("Failed to load menu items: " + e.getMessage());
        }
    }
    
    /**
     * Add item to current order
     */
    private void addItemToOrder(int itemId, int quantity, JLabel totalLabel) {
        try {
            MenuItem item = menuItemDAO.findById(itemId);
            if (item != null && item.isAvailable()) {
                currentOrder.addItem(item, quantity);
                orderItemsMap.put(itemId, orderItemsMap.getOrDefault(itemId, 0) + quantity);
                updateOrderTable();
                totalLabel.setText(String.format("Total: ₹%.2f", currentOrder.getTotalAmount()));
            }
        } catch (DatabaseException e) {
            showError("Failed to add item: " + e.getMessage());
        }
    }
    
    /**
     * Update order table
     */
    private void updateOrderTable() {
        orderTableModel.setRowCount(0);
        for (OrderItem item : currentOrder.getItems()) {
            orderTableModel.addRow(new Object[]{
                item.getMenuItem().getItemName(),
                item.getQuantity(),
                String.format("%.2f", item.getMenuItem().getPrice()),
                String.format("%.2f", item.getSubtotal())
            });
        }
    }
    
    /**
     * Clear current order
     */
    private void clearOrder(JLabel totalLabel) {
        currentOrder = new Order(0, 1);
        orderItemsMap.clear();
        orderTableModel.setRowCount(0);
        totalLabel.setText("Total: ₹0.00");
    }
    
    /**
     * Place order
     */
    private void placeOrder(JLabel totalLabel) {
        if (currentOrder.getItems().isEmpty()) {
            showError("Please add items to the order first!");
            return;
        }
        
        try {
            int orderId = orderDAO.insert(currentOrder);
            currentOrder.setOrderId(orderId);
            
            // Add to processing queue
            orderProcessor.addOrder(currentOrder);
            
            JOptionPane.showMessageDialog(this,
                "Order placed successfully!\nOrder ID: " + orderId,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            clearOrder(totalLabel);
            
        } catch (DatabaseException e) {
            showError("Failed to place order: " + e.getMessage());
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Main method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            CanteenGUI gui = new CanteenGUI();
            gui.setVisible(true);
        });
    }
}
