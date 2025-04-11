package com.example.apporder.Model;

import java.util.List;

public class Bill {
    private String id;
    private String tableId;
    private String tableName;
    private String status;
    private String timestamp;
    private List<paFood> foodList;
    private List<Integer> quantities;
    private long totalPrice;

    // Constructor
    public Bill() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public List<paFood> getFoodList() { return foodList; }
    public void setFoodList(List<paFood> foodList) { this.foodList = foodList; }

    public List<Integer> getQuantities() { return quantities; }
    public void setQuantities(List<Integer> quantities) { this.quantities = quantities; }

    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
}