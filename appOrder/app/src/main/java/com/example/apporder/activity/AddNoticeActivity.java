package com.example.apporder.activity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apporder.R;
import com.example.apporder.database.NoticeHelper;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AddNoticeActivity extends AppCompatActivity {
    private NoticeHelper noticeHelper;
    private EditText etTieuDe;
    private EditText etThongTin;
    private static final String FCM_API = "https://fcm.googleapis.com/v1/projects/apporder-bf300/messages:send";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pa_act_add_noti);

        noticeHelper = new NoticeHelper();
        etTieuDe = findViewById(R.id.etTieuDe);
        etThongTin = findViewById(R.id.etThongtin);

        ShapeableImageView backButton = findViewById(R.id.r3rk27dbznwb);
        backButton.setOnClickListener(v -> finish());

        Button btnLuu = findViewById(R.id.btnLuu);
        btnLuu.setOnClickListener(v -> {
            String title = etTieuDe.getText().toString().trim();
            String content = etThongTin.getText().toString().trim();

            if (!title.isEmpty() && !content.isEmpty()) {
                noticeHelper.addNotice(title, content, success -> {
                    if (success) {
                        Toast.makeText(this, "Đã lưu thông báo", Toast.LENGTH_SHORT).show();
                        sendNotificationToAllDevices(title, content);
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnHuy = findViewById(R.id.btnHuy);
        btnHuy.setOnClickListener(v -> finish());
    }

    private void sendNotificationToAllDevices(String title, String content) {
        FirebaseFirestore.getInstance().collection("devices")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        for (String token : queryDocumentSnapshots.getDocuments()
                                .stream()
                                .map(doc -> doc.getString("token"))
                                .toList()) {
                            new SendNotificationTask().execute(token, title, content);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("FCM", "Error fetching tokens", e));
    }

    private class SendNotificationTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String token = params[0];
            String title = params[1];
            String content = params[2];

            try {
                String accessToken = getAccessToken();
                if (accessToken == null) {
                    Log.e("FCM", "Failed to get access token");
                    return null;
                }

                URL url = new URL(FCM_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject message = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", content);
                message.put("notification", notification);
                message.put("token", token);

                JSONObject json = new JSONObject();
                json.put("message", message);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("FCM", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("FCM", "Notification sent successfully");
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.e("FCM", "Error sending notification: " + response.toString());
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("FCM", "Error sending notification", e);
            }
            return null;
        }

        private String getAccessToken() {
            try {
                // Đọc file service account từ raw resource
                InputStream inputStream = getResources().openRawResource(R.raw.service_account);
                if (inputStream == null) {
                    Log.e("FCM", "Service account file not found in raw resources");
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();
                inputStream.close();

                JSONObject serviceAccount = new JSONObject(jsonString.toString());
                String clientEmail = serviceAccount.getString("client_email");
                String privateKey = serviceAccount.getString("private_key");

                // Xử lý private key (loại bỏ header/footer và ký tự xuống dòng)
                String privateKeyCleaned = privateKey
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\n", "")
                        .trim();

                // Chuyển private key thành định dạng PKCS#8
                byte[] privateKeyBytes = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    privateKeyBytes = Base64.getDecoder().decode(privateKeyCleaned);
                }
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey key = keyFactory.generatePrivate(keySpec);

                // Tạo JWT
                long now = System.currentTimeMillis() / 1000;
                String jwt = Jwts.builder()
                        .setIssuer(clientEmail)
                        .setAudience("https://oauth2.googleapis.com/token")
                        .setSubject(clientEmail)
                        .claim("scope", "https://www.googleapis.com/auth/firebase.messaging")
                        .setIssuedAt(new java.util.Date(now * 1000))
                        .setExpiration(new java.util.Date((now + 3600) * 1000))
                        .signWith(SignatureAlgorithm.RS256, key)
                        .compact();

                // Gọi API để lấy Access Token
                URL url = new URL("https://oauth2.googleapis.com/token");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer";
                String data = "grant_type=" + grantType + "&assertion=" + jwt;

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes(StandardCharsets.UTF_8));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String accessToken = jsonResponse.getString("access_token");
                    Log.d("FCM", "Access Token: " + accessToken);
                    return accessToken;
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.e("FCM", "Failed to get access token, response code: " + responseCode + ", response: " + response.toString());
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("FCM", "Error getting access token: " + e.getMessage(), e);
            }
            return null;
        }
    }
}