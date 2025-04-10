package com.example.apporder.database;

import com.example.apporder.modules.paFood;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class paFirestoreHelper {

    private final FirebaseFirestore db;
    private static final String COLLECTION_FOODS = "foods";

    public paFirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Thêm món ăn
    public void addFood(paFood food, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_FOODS)
                .document(food.getId())
                .set(food)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // Sửa món ăn
    public void updateFood(paFood food, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_FOODS)
                .document(food.getId())
                .set(food)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // Xóa món ăn
    public void deleteFood(String foodId, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_FOODS)
                .document(foodId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // Đọc danh sách món ăn
    public void getAllFoods(OnFoodsLoadedListener listener) {
        db.collection(COLLECTION_FOODS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<paFood> foodList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            paFood food = document.toObject(paFood.class);
                            foodList.add(food);
                        }
                        listener.onFoodsLoaded(foodList);
                    } else {
                        listener.onError(task.getException().getMessage());
                    }
                });
    }

    // Interfaces để xử lý callback
    public interface OnSuccessListener {
        void onSuccess();
    }

    public interface OnFailureListener {
        void onFailure(String error);
    }

    public interface OnFoodsLoadedListener {
        void onFoodsLoaded(List<paFood> foodList);
        void onError(String error);
    }
}