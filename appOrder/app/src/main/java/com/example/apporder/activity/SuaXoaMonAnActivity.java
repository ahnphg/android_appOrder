package com.example.apporder.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.bumptech.glide.Glide;
import com.example.apporder.R;
import com.example.apporder.database.FirestoreHelper;
import com.example.apporder.modules.Food;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SuaXoaMonAnActivity extends AppCompatActivity {

    private EditText etMaMonAn, etTenMonAn, etGia;
    private ImageView imageViewSelectPhotoMonAn;
    private Button btnUpdate, btnXoa, btnHuy;
    private FirestoreHelper firestoreHelper;
    private FirebaseStorage storage;
    private Food food;
    private Uri imageUri;

    // ActivityResultLauncher để chọn ảnh
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imageViewSelectPhotoMonAn.setImageURI(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pa_act_suaxoa_mon_an);

        // Ánh xạ các view
        etMaMonAn = findViewById(R.id.etMaMonAn);
        etTenMonAn = findViewById(R.id.etTenMonAn);
        etGia = findViewById(R.id.etGia);
        imageViewSelectPhotoMonAn = findViewById(R.id.imageViewSelectPhotoMonAn);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnXoa = findViewById(R.id.btnXoa);
        btnHuy = findViewById(R.id.btnHuy);

        // Khởi tạo FirestoreHelper và FirebaseStorage
        firestoreHelper = new FirestoreHelper();
        storage = FirebaseStorage.getInstance();

        // Lấy dữ liệu món ăn từ Intent
        food = (Food) getIntent().getSerializableExtra("food");
        if (food != null) {
            etMaMonAn.setText(food.getId());
            etTenMonAn.setText(food.getName());
            etGia.setText(String.valueOf(food.getPrice()));
            // Load ảnh từ imageUrl bằng Glide
            Glide.with(this)
                    .load(food.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(imageViewSelectPhotoMonAn);
        }

        // Xử lý sự kiện nhấn vào ImageView để chọn ảnh
        imageViewSelectPhotoMonAn.setOnClickListener(v -> {
            String permissionToCheck = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

            if (ContextCompat.checkSelfPermission(this, permissionToCheck)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập bộ nhớ chưa được cấp! Vui lòng cấp quyền trong cài đặt.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                pickImageLauncher.launch("image/*");
            }
        });

        // Xử lý sự kiện nhấn nút Cập nhật
        btnUpdate.setOnClickListener(v -> updateFood());

        // Xử lý sự kiện nhấn nút Xóa
        btnXoa.setOnClickListener(v -> deleteFood());

        // Xử lý sự kiện nhấn nút Hủy
        btnHuy.setOnClickListener(v -> finish());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    // Nén ảnh trước khi upload
    private byte[] compressImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Nén ảnh với chất lượng 80%
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi nén ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void updateFood() {
        String id = etMaMonAn.getText().toString().trim();
        String name = etTenMonAn.getText().toString().trim();
        String priceStr = etGia.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá phải là số", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu không chọn ảnh mới, giữ nguyên imageUrl cũ
        if (imageUri == null) {
            Food updatedFood = new Food(id, name, price, food.getImageUrl());
            firestoreHelper.updateFood(updatedFood,
                    () -> {
                        Toast.makeText(this, "Cập nhật món ăn thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show());
        } else {
            // Kiểm tra kết nối mạng
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "Không có kết nối mạng! Vui lòng kiểm tra kết nối và thử lại.", Toast.LENGTH_LONG).show();
                return;
            }

            // Đảm bảo ID hợp lệ (thay thế ký tự không hợp lệ)
            String safeId = id.replaceAll("[^a-zA-Z0-9]", "_");

            // Nén ảnh trước khi upload
            byte[] compressedImage = compressImage(imageUri);
            if (compressedImage == null) {
                Toast.makeText(this, "Không thể nén ảnh, vui lòng thử lại.", Toast.LENGTH_LONG).show();
                return;
            }

            // Upload ảnh mới lên Firebase Storage
            StorageReference storageRef = storage.getReference().child("food_images/" + safeId + ".jpg");
            UploadTask uploadTask = storageRef.putBytes(compressedImage);

            // Xử lý khi upload hoàn tất
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Upload thành công, lấy URL của ảnh
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Food updatedFood = new Food(id, name, price, imageUrl);
                    // Cập nhật vào Firestore
                    firestoreHelper.updateFood(updatedFood,
                            () -> {
                                Toast.makeText(this, "Cập nhật món ăn thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            error -> Toast.makeText(this, "Lỗi khi lưu vào Firestore: " + error, Toast.LENGTH_LONG).show());
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }).addOnFailureListener(e -> {
                // Upload thất bại
                Toast.makeText(this, "Lỗi khi upload ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void deleteFood() {
        // Xóa ảnh từ Firebase Storage (nếu cần)
        String safeId = food.getId().replaceAll("[^a-zA-Z0-9]", "_");
        StorageReference storageRef = storage.getReference().child("food_images/" + safeId + ".jpg");
        storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa dữ liệu từ Firestore
                    firestoreHelper.deleteFood(food.getId(),
                            () -> {
                                Toast.makeText(this, "Xóa món ăn thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            error -> Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> {
                    // Nếu không xóa được ảnh, vẫn xóa dữ liệu từ Firestore
                    firestoreHelper.deleteFood(food.getId(),
                            () -> {
                                Toast.makeText(this, "Xóa món ăn thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            error -> Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show());
                });
    }
}