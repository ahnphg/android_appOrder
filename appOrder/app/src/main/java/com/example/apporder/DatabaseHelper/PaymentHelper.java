package com.example.apporder.DatabaseHelper;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.apporder.Model.Payment;
import com.example.apporder.Model.paFood;

public class PaymentHelper {
    private final FirebaseFirestore db;

    public PaymentHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnPaymentSavedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnPaymentsLoadedListener {
        void onSuccess(List<Payment> paymentList);
        void onFailure(Exception e);
    }

    public void savePayment(Payment payment, @NonNull OnPaymentSavedListener listener) {
        db.collection("payment").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newId = queryDocumentSnapshots.size();
                    payment.setId(String.valueOf(newId));

                    Map<String, Object> paymentData = new HashMap<>();
                    paymentData.put("id", payment.getId());
                    paymentData.put("billId", payment.getBillId());
                    paymentData.put("tableId", payment.getTableId());
                    paymentData.put("tableName", payment.getTableName());
                    paymentData.put("orderPerson", payment.getOrderPerson());
                    paymentData.put("timestamp", payment.getTimestamp());
                    paymentData.put("totalPrice", payment.getTotalPrice());

                    List<Map<String, Object>> items = new ArrayList<>();
                    for (int i = 0; i < payment.getFoodList().size(); i++) {
                        paFood food = payment.getFoodList().get(i);
                        Map<String, Object> item = new HashMap<>();
                        item.put("foodId", food.getId());
                        item.put("foodName", food.getName());
                        item.put("imageUrl", food.getImageUrl());
                        item.put("price", food.getPrice()); // Lưu price dưới dạng số
                        item.put("quantity", payment.getQuantities().get(i));
                        items.add(item);
                    }
                    paymentData.put("items", items);

                    db.collection("payment").document(payment.getId())
                            .set(paymentData)
                            .addOnSuccessListener(aVoid -> {
                                Map<String, Object> tableUpdates = new HashMap<>();
                                tableUpdates.put("currentOrderedID", "");
                                tableUpdates.put("status", "Trống");

                                db.collection("tables").document(payment.getTableId())
                                        .update(tableUpdates)
                                        .addOnSuccessListener(aVoid1 -> listener.onSuccess())
                                        .addOnFailureListener(listener::onFailure);
                            })
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public void getAllPayments(@NonNull OnPaymentsLoadedListener listener) {
        db.collection("payment").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Payment> paymentList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Payment payment = new Payment();
                        payment.setId(document.getString("id"));
                        payment.setBillId(document.getString("billId"));
                        payment.setTableId(document.getString("tableId"));
                        payment.setTableName(document.getString("tableName"));
                        payment.setOrderPerson(document.getString("orderPerson"));
                        payment.setTotalPrice(document.getLong("totalPrice") != null ? document.getLong("totalPrice") : 0);

                        String timestampStr;
                        Object timestampObj = document.get("timestamp");
                        if (timestampObj instanceof Timestamp) {
                            Timestamp timestamp = (Timestamp) timestampObj;
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
                            timestampStr = sdf.format(timestamp.toDate());
                        } else if (timestampObj instanceof String) {
                            timestampStr = (String) timestampObj;
                        } else {
                            timestampStr = "Unknown";
                        }
                        payment.setTimestamp(timestampStr);

                        List<paFood> foodList = new ArrayList<>();
                        List<Integer> quantities = new ArrayList<>();
                        List<?> items = (List<?>) document.get("items");
                        if (items != null) {
                            for (Object obj : items) {
                                if (obj instanceof Map) {
                                    Map<?, ?> itemMap = (Map<?, ?>) obj;
                                    paFood food = new paFood();
                                    food.setId((String) itemMap.get("foodId"));
                                    food.setName((String) itemMap.get("foodName"));
                                    food.setImageUrl((String) itemMap.get("imageUrl"));

                                    Object priceObj = itemMap.get("price");
                                    int price = 0;
                                    if (priceObj instanceof Long) {
                                        price = ((Long) priceObj).intValue();
                                    } else if (priceObj instanceof Integer) {
                                        price = (Integer) priceObj;
                                    } else if (priceObj instanceof String) {
                                        try {
                                            price = Integer.parseInt((String) priceObj);
                                        } catch (NumberFormatException e) {
                                            price = 0;
                                        }
                                    }
                                    food.setPrice(price);

                                    int qty = (itemMap.get("quantity") instanceof Long) ? ((Long) itemMap.get("quantity")).intValue() : 0;
                                    quantities.add(qty);
                                    foodList.add(food);
                                }
                            }
                        }
                        payment.setFoodList(foodList);
                        payment.setQuantities(quantities);
                        paymentList.add(payment);
                    }
                    listener.onSuccess(paymentList);
                })
                .addOnFailureListener(listener::onFailure);
    }
}