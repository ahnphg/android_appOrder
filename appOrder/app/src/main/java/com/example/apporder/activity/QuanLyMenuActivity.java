package com.example.apporder.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporder.R;
import com.example.apporder.adapter.FoodAdapter;
import com.example.apporder.database.FirestoreHelper;
import com.example.apporder.modules.Food;
import java.util.ArrayList;
import java.util.List;

public class QuanLyMenuActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenu;
    private ImageView imgThemMonAn;
    private FoodAdapter adapter;
    private List<Food> foodList;
    private FirestoreHelper firestoreHelper;
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pa_act_quan_li_menu);

        // Ánh xạ các view
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        imgThemMonAn = findViewById(R.id.imgThemMonAn);

        // Khởi tạo FirestoreHelper
        firestoreHelper = new FirestoreHelper();

        // Thiết lập RecyclerView
        foodList = new ArrayList<>();
        adapter = new FoodAdapter(foodList, this::goToEditFoodActivity);
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMenu.setAdapter(adapter);

        // Tải danh sách món ăn từ Firestore
        loadFoods();

        // Xử lý sự kiện nhấn nút Thêm Món Ăn
        imgThemMonAn.setOnClickListener(v -> {
            Intent intent = new Intent(QuanLyMenuActivity.this, ThemMonAnActivity.class);
            startActivity(intent);
        });

        // Hiển thị dialog yêu cầu quyền ngay khi activity khởi động
        showPermissionDialog();
    }

    private void showPermissionDialog() {
        // Xác định quyền cần yêu cầu dựa trên phiên bản Android
        String permissionToRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        // Kiểm tra xem quyền đã được cấp chưa
        if (ContextCompat.checkSelfPermission(this, permissionToRequest)
                != PackageManager.PERMISSION_GRANTED) {
            // Tạo dialog
            new AlertDialog.Builder(this)
                    .setTitle("Yêu cầu quyền truy cập bộ nhớ")
                    .setMessage("Ứng dụng cần quyền truy cập bộ nhớ để chọn ảnh món ăn. Bạn có đồng ý cấp quyền không?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        // Yêu cầu quyền khi người dùng nhấn "Đồng ý"
                        ActivityCompat.requestPermissions(this,
                                new String[]{permissionToRequest},
                                STORAGE_PERMISSION_CODE);
                    })
                    .setNegativeButton("Không đồng ý", (dialog, which) -> {
                        // Hiển thị thông báo nếu người dùng không đồng ý
                        Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối. Bạn sẽ không thể chọn ảnh.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    })
                    .setCancelable(false) // Không cho phép đóng dialog bằng nút Back
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập bộ nhớ đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                                ? Manifest.permission.READ_MEDIA_IMAGES
                                : Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối! Bạn sẽ không thể chọn ảnh.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối vĩnh viễn. Vui lòng vào cài đặt để cấp quyền.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        }
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