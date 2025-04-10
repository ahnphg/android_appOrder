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
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.example.apporder.R;
import com.example.apporder.database.paFirestoreHelper;
import com.example.apporder.modules.paFood;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;

public class paThemMonAnActivity extends AppCompatActivity {

    private EditText etMaMonAn, etTenMonAn, etGia;
    private ImageView imageViewSelectPhotoMonAn;
    private Button btnLuu, btnHuy;
    private paFirestoreHelper firestoreHelper;
    private Uri imageUri;
    private OkHttpClient client;

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
        setContentView(R.layout.pa_act_them_mon_an);

        // Ánh xạ các view
        etMaMonAn = findViewById(R.id.etMaMonAn);
        etTenMonAn = findViewById(R.id.etTenMonAn);
        etGia = findViewById(R.id.etGia);
        imageViewSelectPhotoMonAn = findViewById(R.id.imageViewSelectPhotoMonAn);
        btnLuu = findViewById(R.id.btnLuu);
        btnHuy = findViewById(R.id.btnHuy);

        // Khởi tạo FirestoreHelper và OkHttpClient
        firestoreHelper = new paFirestoreHelper();
        client = new OkHttpClient();

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

        // Xử lý sự kiện nhấn nút Lưu
        btnLuu.setOnClickListener(v -> saveFood());

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

    // Chuyển ảnh thành base64 để upload lên Imgur
    private String imageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi chuyển ảnh thành base64: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // Upload ảnh lên Imgur
    private void uploadImageToImgur(String base64Image, Callback callback) {
        String clientId = "22b4c7b814c93ec"; // Thay bằng Client ID của bạn
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", base64Image)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image")
                .addHeader("Authorization", "Client-ID " + clientId)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }

    private void saveFood() {
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

        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra kết nối mạng
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối mạng! Vui lòng kiểm tra kết nối và thử lại.", Toast.LENGTH_LONG).show();
            return;
        }

        // Chuyển ảnh thành base64
        String base64Image = imageToBase64(imageUri);
        if (base64Image == null) {
            Toast.makeText(this, "Không thể chuyển ảnh thành base64, vui lòng thử lại.", Toast.LENGTH_LONG).show();
            return;
        }

        // Upload ảnh lên Imgur
        uploadImageToImgur(base64Image, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(paThemMonAnActivity.this, "Lỗi khi upload ảnh lên Imgur: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String imageUrl = json.getJSONObject("data").getString("link");
                        paFood food = new paFood(id, name, price, imageUrl);
                        // Lưu vào Firestore
                        firestoreHelper.addFood(food,
                                () -> runOnUiThread(() -> {
                                    Toast.makeText(paThemMonAnActivity.this, "Thêm món ăn thành công", Toast.LENGTH_SHORT).show();
                                    finish();
                                }),
                                error -> runOnUiThread(() -> Toast.makeText(paThemMonAnActivity.this, "Lỗi khi lưu vào Firestore: " + error, Toast.LENGTH_LONG).show()));
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(paThemMonAnActivity.this, "Lỗi khi phân tích phản hồi từ Imgur: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(paThemMonAnActivity.this, "Lỗi khi upload ảnh lên Imgur: " + response.message(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}