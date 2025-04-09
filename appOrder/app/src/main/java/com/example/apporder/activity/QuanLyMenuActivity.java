package com.example.apporder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporder.R;
import com.example.apporder.adapter.FoodAdapter;
import com.example.apporder.database.FirestoreHelper;
import com.example.apporder.modules.Food;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class QuanLyMenuActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenu;
    private ImageView imgThemMonAn;
    private FoodAdapter adapter;
    private List<Food> foodList;
    private FirestoreHelper firestoreHelper;
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
        firestoreHelper = new FirestoreHelper();

        // Thiết lập RecyclerView
        foodList = new ArrayList<>();
        adapter = new FoodAdapter(foodList, this::goToEditFoodActivity);
        recyclerViewMenu.setLayoutManager(new GridLayoutManager(this, 2)); // 2 cột
        recyclerViewMenu.setAdapter(adapter);

        // Tải danh sách món ăn từ Firestore
        loadFoods();

        // Xử lý sự kiện nhấn nút Thêm Món Ăn
        imgThemMonAn.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyMenuActivity.this, ThemMonAnActivity.class);
            startActivity(intent);
        });
    }

    private void loadFoods() {
        firestoreHelper.getAllFoods(new FirestoreHelper.OnFoodsLoadedListener() {
            @Override
            public void onFoodsLoaded(List<Food> foods) {
                foodList.clear();
                foodList.addAll(foods);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(QuanLyMenuActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToEditFoodActivity(Food food) {
        Intent intent = new Intent(QuanLyMenuActivity.this, SuaXoaMonAnActivity.class);
        intent.putExtra("food", food);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoods(); // Tải lại danh sách khi quay lại activity
    }
}