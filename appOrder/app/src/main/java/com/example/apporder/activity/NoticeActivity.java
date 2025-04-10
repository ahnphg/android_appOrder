package com.example.apporder.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporder.R;
import com.example.apporder.adapter.NoticeAdapter;
import com.example.apporder.database.NoticeHelper;
import com.example.apporder.modules.Notice;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private RecyclerView recyclerView;
    private NoticeHelper noticeHelper;
    private NoticeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pa_act_noti);

        recyclerView = findViewById(R.id.recyclerViewTB);
        noticeHelper = new NoticeHelper();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Kiểm tra và yêu cầu quyền thông báo
        checkNotificationPermission();

        loadNotices();

        ShapeableImageView imgThemThongBao = findViewById(R.id.imgThemThongBao);
        imgThemThongBao.setOnClickListener(v -> {
            Intent intent = new Intent(NoticeActivity.this, AddNoticeActivity.class);
            startActivity(intent);
        });

        // Lấy và lưu FCM Token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM", "FCM Token: " + token);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> device = new HashMap<>();
                    device.put("token", token);
                    db.collection("devices")
                            .document(token)
                            .set(device)
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved to Firestore"))
                            .addOnFailureListener(e -> Log.w("FCM", "Error saving token", e));
                });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền thông báo đã được cấp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền thông báo để hoạt động", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadNotices() {
        noticeHelper.getNotices(new NoticeHelper.NoticeCallback() {
            @Override
            public void onComplete(List<Notice> notices) {
                adapter = new NoticeAdapter(notices);
                recyclerView.setAdapter(adapter);
            }
        });
    }
}