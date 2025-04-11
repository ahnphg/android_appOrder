package com.example.apporder.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporder.R;
import com.example.apporder.Adapter.paFoodAdapter;
import com.example.apporder.DatabaseHelper.paFirestoreHelper;
import com.example.apporder.Model.paFood;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class paQuanLyMenuActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenu;
    private ImageView imgThemMonAn;
    private paFoodAdapter adapter;
    private List<paFood> foodList;
    private paFirestoreHelper firestoreHelper;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pa_act_quan_li_menu);

        // Ánh xạ các view
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        imgThemMonAn = findViewById(R.id.imgThemMonAn);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Khởi tạo FirestoreHelper
        firestoreHelper = new paFirestoreHelper();

        // Thiết lập RecyclerView
        foodList = new ArrayList<>();
        adapter = new paFoodAdapter(foodList, this::goToEditFoodActivity);
        recyclerViewMenu.setLayoutManager(new GridLayoutManager(this, 2)); // 2 cột
        recyclerViewMenu.setAdapter(adapter);

        // Tải danh sách món ăn từ Firestore
        loadFoods();

        // Xử lý sự kiện nhấn nút Thêm Món Ăn
        imgThemMonAn.setOnClickListener(v -> {
            Intent intent = new Intent(paQuanLyMenuActivity.this, paThemMonAnActivity.class);
            startActivity(intent);
        });
    }

    private void loadFoods() {
        firestoreHelper.getAllFoods(new paFirestoreHelper.OnFoodsLoadedListener() {
            @Override
            public void onFoodsLoaded(List<paFood> foods) {
                foodList.clear();
                foodList.addAll(foods);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(paQuanLyMenuActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToEditFoodActivity(paFood food) {
        Intent intent = new Intent(paQuanLyMenuActivity.this, paSuaXoaMonAnActivity.class);
        intent.putExtra("food", food);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoods(); // Tải lại danh sách khi quay lại activity
    }
}