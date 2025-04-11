package com.example.apporder.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporder.Adapter.phOrderAdapter;
import com.example.apporder.Model.paFood;
import com.example.apporder.R;

import java.util.ArrayList;
import java.util.List;

public class ViewDetailActivity extends AppCompatActivity {
    private TextView tvTableName, txtName, txtTime;
    private RecyclerView recyclerOrdered;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pa_act_viewdetail);

        // Ánh xạ các thành phần giao diện
        tvTableName = findViewById(R.id.tv_table_nam);
        txtName = findViewById(R.id.txtName);
        txtTime = findViewById(R.id.txtTime);
        recyclerOrdered = findViewById(R.id.recyclerOrdered);
        btnBack = findViewById(R.id.r5mgogsypwwu);

        // Nhận dữ liệu từ Intent
        String tableName = getIntent().getStringExtra("tableName");
        String orderPerson = getIntent().getStringExtra("orderPerson");
        String timestamp = getIntent().getStringExtra("timestamp");
        ArrayList<paFood> foodList = (ArrayList<paFood>) getIntent().getSerializableExtra("foodList");
        ArrayList<Integer> quantities = (ArrayList<Integer>) getIntent().getSerializableExtra("quantities");

        // Hiển thị thông tin
        tvTableName.setText(tableName);
        txtName.setText(orderPerson != null ? orderPerson : "Không xác định");
        txtTime.setText(timestamp != null ? timestamp : "Không xác định");

        // Log để kiểm tra foodList sau khi deserialize
        if (foodList != null) {
            for (paFood food : foodList) {
                Log.d("ViewDetailActivity", "Food: " + food.getName() + ", Price: " + food.getPrice());
            }
        } else {
            Log.e("ViewDetailActivity", "foodList is null");
        }

        // Thiết lập RecyclerView
        recyclerOrdered.setLayoutManager(new LinearLayoutManager(this));
        if (foodList != null && quantities != null) {
            phOrderAdapter adapter = new phOrderAdapter(foodList, quantities);
            recyclerOrdered.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Không có dữ liệu để hiển thị", Toast.LENGTH_SHORT).show();
        }

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());
    }
}