package com.example.apporder.DatabaseHelper;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.apporder.Model.paFood;

public class phOrderedHelper {

    private final FirebaseFirestore db;

    public phOrderedHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnBillLoadedListener {
        void onSuccess(String tableName, List<paFood> foodList, List<Integer> quantities);
        void onFailure(Exception e);
    }

    public void getBillById(@NonNull String billId, @NonNull OnBillLoadedListener listener) {
        db.collection("bills").document(billId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onFailure(new Exception("Hóa đơn không tồn tại"));
                        return;
                    }

                    String tableName = documentSnapshot.getString("tableName");

                    ArrayList<paFood> foodList = new ArrayList<>();
                    ArrayList<Integer> quantities = new ArrayList<>();

                    List<?> items = (List<?>) documentSnapshot.get("items");
                    if (items != null) {
                        for (Object obj : items) {
                            if (obj instanceof Map) {
                                Map<?, ?> itemMap = (Map<?, ?>) obj;

                                paFood food = new paFood();
                                food.setId((String) itemMap.get("foodId"));
                                food.setName((String) itemMap.get("foodName"));
                                food.setImageUrl((String) itemMap.get("imageUrl"));

                                Object priceObj = itemMap.get("price");
                                int price = (priceObj instanceof Long) ? ((Long) priceObj).intValue() : 0;
                                food.setPrice(price);

                                Object qtyObj = itemMap.get("quantity");
                                int qty = (qtyObj instanceof Long) ? ((Long) qtyObj).intValue() : 0;

                                foodList.add(food);
                                quantities.add(qty);
                            }
                        }
                    }

                    listener.onSuccess(tableName, foodList, quantities);
                })
                .addOnFailureListener(listener::onFailure);
    }
}