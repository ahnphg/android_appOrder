package com.example.apporder.DatabaseHelper;

import com.example.apporder.Model.Notice;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeHelper {
    private FirebaseFirestore db;
    private static final String COLLECTION_NAME = "notices";

    public interface NoticeCallback {
        void onComplete(List<Notice> notices);
    }

    public interface AddNoticeCallback {
        void onComplete(boolean success);
    }

    public NoticeHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void addNotice(String title, String content, AddNoticeCallback callback) {
        Map<String, Object> notice = new HashMap<>();
        notice.put("title", title);
        notice.put("content", content);
        notice.put("timestamp", new Date().getTime());

        db.collection(COLLECTION_NAME)
                .add(notice)
                .addOnSuccessListener(documentReference -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public void getNotices(NoticeCallback callback) {
        db.collection(COLLECTION_NAME)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onComplete(new ArrayList<>());
                        return;
                    }

                    List<Notice> notices = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Notice notice = new Notice(
                                    doc.getId(),
                                    doc.getString("title"),
                                    doc.getString("content"),
                                    doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0
                            );
                            notices.add(notice);
                        }
                    }
                    callback.onComplete(notices);
                });
    }
}