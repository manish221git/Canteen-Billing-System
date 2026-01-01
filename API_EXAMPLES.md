# API Usage Examples

This document provides examples of how to use the Canteen Billing System API.

## Prerequisites

Deploy the WAR file to a servlet container (Tomcat, Jetty, etc.) and ensure it's running.

## Base URL
```
http://localhost:8080/canteen-billing-system
```

## Menu API Examples

### 1. List All Menu Items
```bash
curl http://localhost:8080/canteen-billing-system/menu/?action=list
```

**Response:**
```json
[
  {
    "itemId": 1,
    "itemName": "Veg Burger",
    "category": "Fast Food",
    "price": 80.00,
    "available": true
  },
  {
    "itemId": 2,
    "itemName": "Chicken Burger",
    "category": "Fast Food",
    "price": 120.00,
    "available": true
  }
]
```

### 2. View Specific Menu Item
```bash
curl "http://localhost:8080/canteen-billing-system/menu/?action=view&itemId=1"
```

### 3. Add New Menu Item
```bash
curl -X POST "http://localhost:8080/canteen-billing-system/menu/?action=add" \
  -d "itemName=Paneer Tikka" \
  -d "category=Starters" \
  -d "price=150.00" \
  -d "available=true"
```

**Response:**
```json
{
  "status": "success",
  "itemId": 6
}
```

### 4. Update Menu Item
```bash
curl -X POST "http://localhost:8080/canteen-billing-system/menu/?action=update" \
  -d "itemId=1" \
  -d "price=90.00"
```

### 5. Get Items by Category
```bash
curl "http://localhost:8080/canteen-billing-system/menu/?action=category&category=Fast%20Food"
```

## Order API Examples

### 1. Create New Order
```bash
curl -X POST "http://localhost:8080/canteen-billing-system/order/?action=create" \
  -d "itemId=1" \
  -d "quantity=2" \
  -d "itemId=3" \
  -d "quantity=1"
```

**Response:**
```json
{
  "status": "success",
  "orderId": 1,
  "message": "Order created successfully"
}
```

### 2. List All Orders
```bash
curl "http://localhost:8080/canteen-billing-system/order/?action=list"
```

**Response:**
```json
[
  {
    "orderId": 1,
    "userId": 1,
    "totalAmount": 220.00,
    "status": "PENDING",
    "orderDate": "2026-01-01 12:45:46"
  }
]
```

### 3. View Specific Order
```bash
curl "http://localhost:8080/canteen-billing-system/order/?action=view&orderId=1"
```

**Response:**
```
Order ID: 1
Date: 2026-01-01 12:45:46.0
Status: PENDING
Items:
  - Veg Burger x 2 = ₹160.00
  - French Fries x 1 = ₹60.00
Total: ₹220.00
```

### 4. Update Order Status
```bash
curl -X POST "http://localhost:8080/canteen-billing-system/order/?action=update" \
  -d "orderId=1" \
  -d "status=COMPLETED"
```

### 5. Delete Order
```bash
curl -X POST "http://localhost:8080/canteen-billing-system/order/?action=delete" \
  -d "orderId=1"
```

## Bill API Examples

### 1. Generate Bill
```bash
curl -X POST "http://localhost:8080/canteen-billing-system/bill/?action=generate" \
  -d "orderId=1" \
  -d "taxPercentage=5.0" \
  -d "discountPercentage=0.0" \
  -d "paymentMethod=CASH"
```

**Response:**
```json
{
  "status": "success",
  "billId": 1,
  "finalAmount": 231.00,
  "message": "Bill generated successfully"
}
```

### 2. View Bill
```bash
curl "http://localhost:8080/canteen-billing-system/bill/?action=view&billId=1"
```

**Response:**
```
========================================
         CANTEEN BILL RECEIPT          
========================================
Bill ID: 1
Order ID: 1
Subtotal: ₹220.00
Tax: ₹11.00
Discount: ₹0.00
Final Amount: ₹231.00
Payment: CASH
Date: 2026-01-01 12:45:46.858
========================================
      Thank you for your order!        
========================================
```

### 3. List All Bills
```bash
curl "http://localhost:8080/canteen-billing-system/bill/?action=list"
```

**Response:**
```json
[
  {
    "billId": 1,
    "orderId": 1,
    "totalAmount": 220.00,
    "taxAmount": 11.00,
    "discountAmount": 0.00,
    "finalAmount": 231.00,
    "paymentMethod": "CASH",
    "billDate": "2026-01-01 12:45:46.858"
  }
]
```

### 4. View Bill by Order ID
```bash
curl "http://localhost:8080/canteen-billing-system/bill/?action=viewByOrder&orderId=1"
```

### 5. Update Bill Payment Method
```bash
curl -X POST "http://localhost:8080/canteen-billing-system/bill/?action=update" \
  -d "billId=1" \
  -d "paymentMethod=CARD"
```

## Complete Workflow Example

### Scenario: Customer orders 2 burgers and 1 coffee, then gets a bill

```bash
# Step 1: View menu
curl "http://localhost:8080/canteen-billing-system/menu/?action=list"

# Step 2: Create order with items
curl -X POST "http://localhost:8080/canteen-billing-system/order/?action=create" \
  -d "itemId=1" -d "quantity=2" \
  -d "itemId=4" -d "quantity=1"

# Response: {"status": "success", "orderId": 2, "message": "Order created successfully"}

# Step 3: View the order
curl "http://localhost:8080/canteen-billing-system/order/?action=view&orderId=2"

# Step 4: Generate bill with 5% tax and 10% discount
curl -X POST "http://localhost:8080/canteen-billing-system/bill/?action=generate" \
  -d "orderId=2" \
  -d "taxPercentage=5.0" \
  -d "discountPercentage=10.0" \
  -d "paymentMethod=UPI"

# Response: {"status": "success", "billId": 2, "finalAmount": 223.65, ...}

# Step 5: View the final bill
curl "http://localhost:8080/canteen-billing-system/bill/?action=view&billId=2"
```

## Error Handling

All endpoints return proper error responses:

```json
{
  "status": "error",
  "message": "Order not found"
}
```

**HTTP Status Codes:**
- 200: Success
- 500: Internal Server Error

## Payment Methods Supported
- CASH
- CARD
- UPI
- WALLET

## Notes

1. All price values are in Indian Rupees (₹)
2. Tax and discount are calculated as percentages
3. Orders are processed asynchronously in background threads
4. Bills are cached for better performance
5. Database is automatically initialized on first run

## GUI Application

To run the desktop GUI application:

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.canteen.gui.CanteenGUI"

# Or using Java directly
java -cp target/classes:lib/* com.canteen.gui.CanteenGUI
```

The GUI provides the same functionality with a user-friendly interface.

## Testing

Run the test application to verify all features:

```bash
mvn compile exec:java -Dexec.mainClass="com.canteen.TestApp"
```

This will test:
- Database initialization
- Menu operations
- User authentication
- Order creation
- Bill generation
- Collections and generics
- Polymorphism
- Exception handling

---

For more details, see [FEATURES.md](FEATURES.md) and [README.md](README.md).
