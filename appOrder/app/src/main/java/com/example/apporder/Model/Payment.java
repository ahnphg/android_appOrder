package com.example.apporder.Model;

import java.util.List;

public class Payment {
    private String id;
    private String billId;
    private String tableId;
    private String tableName;
    private List<paFood> foodList;
    private List<Integer> quantities;
    private String orderPerson;
    private String timestamp;
    private long totalPrice;

    // Constructor
    public Payment() {}

    public Payment(String id, String billId, String tableId, String tableName, List<paFood> foodList, List<Integer> quantities, String orderPerson, String timestamp, long totalPrice) {
        this.id = id;
        this.billId = billId;
        this.tableId = tableId;
        this.tableName = tableName;
        this.foodList = foodList;
        this.quantities = quantities;
        this.orderPerson = orderPerson;
        this.timestamp = timestamp;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public List<paFood> getFoodList() { return foodList; }
    public void setFoodList(List<paFood> foodList) { this.foodList = foodList; }

    public List<Integer> getQuantities() { return quantities; }
    public void setQuantities(List<Integer> quantities) { this.quantities = quantities; }

    public String getOrderPerson() { return orderPerson; }
    public void setOrderPerson(String orderPerson) { this.orderPerson = orderPerson; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
}
