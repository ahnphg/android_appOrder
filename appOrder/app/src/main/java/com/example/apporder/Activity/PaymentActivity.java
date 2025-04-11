package com.example.apporder.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.apporder.Adapter.phOrderAdapter;
import com.example.apporder.DatabaseHelper.PaymentHelper;
import com.example.apporder.DatabaseHelper.phOrderedHelper;
import com.example.apporder.Model.Payment;
import com.example.apporder.Model.paFood;
import com.example.apporder.R;

public class PaymentActivity extends AppCompatActivity {
    private RecyclerView recyclerOrdered;
    private TextView tvTableName, txtPrice;
    private Button btnThanhToan;
    private ImageView btnBack;

    private String tableId, billId, tableName;
    private List<paFood> foodList;
    private List<Integer> quantities;
    private long totalPrice;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pa_act_pay);

        recyclerOrdered = findViewById(R.id.recyclerOrdered);
        tvTableName = findViewById(R.id.tv_table_nam);
        txtPrice = findViewById(R.id.txtPrice);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        btnBack = findViewById(R.id.r5mgogsypwwu);

        tableId = getIntent().getStringExtra("tableId");
        billId = getIntent().getStringExtra("billId");
        tableName = getIntent().getStringExtra("tableName");

        if (billId == null || billId.trim().isEmpty() || "null".equalsIgnoreCase(billId)) {
            Toast.makeText(this, "Không có hóa đơn để hiển thị!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTableName.setText(tableName);
        recyclerOrdered.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnThanhToan.setOnClickListener(v -> processPayment());

        db = FirebaseFirestore.getInstance();

        loadBill();
    }

    private void loadBill() {
        phOrderedHelper helper = new phOrderedHelper();
        helper.getBillById(billId, new phOrderedHelper.OnBillLoadedListener() {
            @Override
            public void onSuccess(String tableNameResult, List<paFood> foodListResult, List<Integer> quantitiesResult) {
                // Gộp các món trùng nhau
                HashMap<String, paFood> foodMap = new HashMap<>();
                HashMap<String, Integer> quantityMap = new HashMap<>();

                for (int i = 0; i < foodListResult.size(); i++) {
                    paFood food = foodListResult.get(i);
                    int qty = quantitiesResult.get(i);
                    String foodId = food.getId();
                    if (foodMap.containsKey(foodId)) {
                        // Nếu món đã tồn tại, cộng dồn số lượng
                        quantityMap.put(foodId, quantityMap.get(foodId) + qty);
                    } else {
                        // Nếu món chưa tồn tại, thêm mới
                        foodMap.put(foodId, food);
                        quantityMap.put(foodId, qty);
                    }
                }

                foodList = new ArrayList<>(foodMap.values());
                quantities = new ArrayList<>();
                totalPrice = 0;
                for (paFood food : foodList) {
                    int qty = quantityMap.get(food.getId());
                    quantities.add(qty);
                    totalPrice += (long) (food.getPrice() * qty); // Chuyển đổi để tính totalPrice
                    Log.d("PaymentActivity", "Food: " + food.getName() + ", Price: " + food.getPrice());
                }

                phOrderAdapter adapter = new phOrderAdapter(foodList, quantities);
                recyclerOrdered.setAdapter(adapter);
                txtPrice.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(totalPrice) + "đ");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(PaymentActivity.this, "Lỗi khi tải hóa đơn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processPayment() {
        Payment payment = new Payment();
        payment.setBillId(billId);
        payment.setTableId(tableId);
        payment.setTableName(tableName);
        payment.setFoodList(foodList);
        payment.setQuantities(quantities);
        payment.setOrderPerson("dt@gmail.com");
        payment.setTimestamp(new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(new Date()));
        payment.setTotalPrice(totalPrice);

        // Log để kiểm tra price trước khi lưu
        for (paFood food : foodList) {
            Log.d("PaymentActivity", "Saving Food: " + food.getName() + ", Price: " + food.getPrice());
        }

        PaymentHelper paymentHelper = new PaymentHelper();
        paymentHelper.savePayment(payment, new PaymentHelper.OnPaymentSavedListener() {
            @Override
            public void onSuccess() {
                // Cập nhật trạng thái của bill thành "đã thanh toán"
                db.collection("bills").document(billId)
                        .update("status", "đã thanh toán")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PaymentActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PaymentActivity.this, phMyOrderActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PaymentActivity.this, "Lỗi khi cập nhật trạng thái hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(PaymentActivity.this, "Lỗi khi thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}