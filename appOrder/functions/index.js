const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Cloud Function được kích hoạt khi có document mới trong collection "notices"
exports.sendNoticeNotification = functions.firestore
    .document("notices/{noticeId}")
    .onCreate(async (snap, context) => {
      // Lấy dữ liệu của document mới (thông báo)
      const notice = snap.data();

      // Lấy tất cả FCM Token từ collection "devices"
      const tokensSnapshot = await admin.firestore()
          .collection("devices").get();
      const tokens = tokensSnapshot.docs.map((doc) => doc.data().token);

      // Kiểm tra nếu không có token nào
      if (tokens.length === 0) {
        console.log("No devices to send notification to");
        return null;
      }

      // Tạo payload thông báo
      const payload = {
        notification: {
          title: notice.title,
          body: notice.content,
        },
      };

      // Gửi thông báo đến tất cả token
      try {
        const response = await admin.messaging()
            .sendToDevice(tokens, payload);
        console.log("Successfully sent message:", response);
      } catch (error) {
        console.log("Error sending message:", error);
      }
    });
