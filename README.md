# Mod Minecraft 1.20.1 (Forge) – Ôn thi Lý luận dạy học Tin học

6 quyển sách = 6 câu tự luận. Chuột phải vào sách để mở bảng câu hỏi: điền cụm từ khóa vào NGUYÊN VĂN đề cương.
Đúng +10 XP, sai mất 1 tim (3 tim/ải). Xong ải sẽ hiện toàn văn để cuộn đọc lại.

* Lần đầu vào thế giới bạn tự nhận đủ 6 quyển. Mất thì gõ `/ldhquiz` để nhận lại (hoặc mở tab Creative "Ôn thi LLDH Tin học").
* Nội dung nằm ở `src/main/resources/assets/ldhquiz/quiz.json` (sửa được; `[[...]]` là ô trống, dòng bắt đầu `## ` là tiêu đề).

## Cách build ra file .jar
1. Cài JDK 17.
2. Mở terminal trong thư mục này, chạy:
   * Windows: `gradlew.bat build`
   * Mac/Linux: `./gradlew build`
3. Lấy `build/libs/ldhquiz-1.0.0.jar` bỏ vào thư mục `mods` của Minecraft Forge **1.20.1** (Forge 47.x).
Thử nhanh không cần cài Forge: `gradlew runClient`.

Lưu ý: chọn ngôn ngữ trò chơi bất kỳ vẫn đọc được; nếu chữ có dấu bị lỗi, tắt "Force Unicode Font" trong Options > Language.
