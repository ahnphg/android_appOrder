package com.example.apporder.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporder.Model.paFood;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.apporder.Adapter.PaymentAdapter;
import com.example.apporder.Adapter.phBillAdapter;
import com.example.apporder.DatabaseHelper.PaymentHelper;
import com.example.apporder.Model.Bill;
import com.example.apporder.Model.Payment;
import com.example.apporder.R;

public class phMyOrderActivity extends AppCompatActivity {
    private RecyclerView recyclerOrder;
    private Button btnOngoing, btnHistory;
    private ImageView btnBack;
    private FirebaseFirestore db;

    // Launcher để yêu cầu quyền thông báo
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initializeUI();
                } else {
                    Toast.makeText(this, "Quyền thông báo bị từ chối. Một số tính năng có thể không hoạt động.", Toast.LENGTH_LONG).show();
                    initializeUI();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                initializeUI();
            }
        } else {
            initializeUI();
        }
    }

    private void initializeUI() {
        try {
            setContentView(R.layout.pa_act_my_order);
        } catch (Exception e) {
            Log.e("phMyOrderActivity", "Error setting content view: " + e.getMessage());
            Toast.makeText(this, "Lỗi giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerOrder = findViewById(R.id.recyclerOrder);
        btnOngoing = findViewById(R.id.btnOngoing);
        btnHistory = findViewById(R.id.btnHistory);
        btnBack = findViewById(R.id.r5mgogsypwwu);

        db = FirebaseFirestore.getInstance();
        recyclerOrder.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnOngoing.setOnClickListener(v -> {
            btnOngoing.setTextColor(getResources().getColor(R.color.orange));
            btnHistory.setTextColor(getResources().getColor(R.color.gray));
            loadOngoingBills();
        });

        btnHistory.setOnClickListener(v -> {
            btnHistory.setTextColor(getResources().getColor(R.color.orange));
            btnOngoing.setTextColor(getResources().getColor(R.color.gray));
            loadPaymentHistory();
        });

        // Mặc định hiển thị Ongoing
        btnOngoing.performClick();
    }

    private void loadOngoingBills() {
        db.collection("bills")
                .whereEqualTo("status", "Đang phục vụ")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("phMyOrderActivity", "Số lượng document trả về: " + queryDocumentSnapshots.size());
                    List<Bill> billList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d("phMyOrderActivity", "Document ID: " + document.getId() + ", Status: " + document.getString("status"));
                            Bill bill = new Bill();
                            bill.setId(document.getId());
                            bill.setTableId(document.getString("tableId"));
                            bill.setTableName(document.getString("tableName"));
                            bill.setStatus(document.getString("status"));

                            // Xử lý trường timestamp của bill
                            String timestampStr;
                            Object timestampObj = document.get("timestamp");
                            if (timestampObj instanceof Timestamp) {
                                Timestamp timestamp = (Timestamp) timestampObj;
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm:ss a 'UTC'Z", Locale.getDefault());
                                timestampStr = sdf.format(timestamp.toDate());
                            } else if (timestampObj instanceof String) {
                                timestampStr = (String) timestampObj;
                            } else {
                                timestampStr = "Unknown";
                            }
                            bill.setTimestamp(timestampStr);

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
                                        food.setPrice((itemMap.get("price") instanceof Long) ? ((Long) itemMap.get("price")).intValue() : 0);
                                        foodList.add(food);

                                        int qty = (itemMap.get("quantity") instanceof Long) ? ((Long) itemMap.get("quantity")).intValue() : 0;
                                        quantities.add(qty);
                                    }
                                }
                            }
                            bill.setFoodList(foodList);
                            bill.setQuantities(quantities);

                            long totalPrice = 0;
                            for (int i = 0; i < foodList.size(); i++) {
                                totalPrice += foodList.get(i).getPrice() * quantities.get(i);
                            }
                            bill.setTotalPrice(totalPrice);

                            billList.add(bill);
                        } catch (Exception e) {
                            Log.e("phMyOrderActivity", "Error parsing bill: " + e.getMessage());
                        }
                    }

                    Log.d("phMyOrderActivity", "Số lượng bill trong billList: " + billList.size());
                    phBillAdapter adapter = new phBillAdapter(this, billList);
                    recyclerOrder.setAdapter(adapter);
                    recyclerOrder.getAdapter().notifyDataSetChanged();

                    if (billList.isEmpty()) {
                        Toast.makeText(this, "Không có bản ghi", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("phMyOrderActivity", "Error loading bills: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi tải danh sách hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPaymentHistory() {
        PaymentHelper helper = new PaymentHelper();
        helper.getAllPayments(new PaymentHelper.OnPaymentsLoadedListener() {
            @Override
            public void onSuccess(List<Payment> paymentList) {
                Log.d("phMyOrderActivity", "Số lượng payment: " + paymentList.size());
                PaymentAdapter adapter = new PaymentAdapter(phMyOrderActivity.this, paymentList);
                recyclerOrder.setAdapter(adapter);
                recyclerOrder.getAdapter().notifyDataSetChanged();

                if (paymentList.isEmpty()) {
                    Toast.makeText(phMyOrderActivity.this, "Không có bản ghi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("phMyOrderActivity", "Error loading payment history: " + e.getMessage());
                Toast.makeText(phMyOrderActivity.this, "Lỗi khi tải lịch sử thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}