# Kết quả kiểm tra bài tập 11

Ngày kiểm tra: 05/10/2026, giờ Việt Nam.

- Maven WAR build: thành công với Java 17.
- Unit test: 11/11 qua, không lỗi hoặc bỏ qua.
- Kiểm thử HTTP/JSP + Tomcat 11 + SQL Server thật: 59/59 qua.
- Giỏ hàng: thêm/sửa/xóa/xóa hết, số lượng lỗi, giới hạn, tách tài khoản.
- COD: kiểm tra dữ liệu nhận hàng, giá đổi sau khi mở checkout, tồn kho đổi, giao dịch cập nhật đồng thời, gửi lại checkout không tạo đơn trùng.
- Đơn hàng: bộ lọc, quyền sở hữu, hủy đơn mới, chặn hủy đơn đã xác nhận, tiến trình quản trị, snapshot giá/tên.
- UPDATE trạng thái bằng SQL: giao diện đọc trạng thái mới, trigger ghi diễn biến; hoàn kho một lần khi hủy/hoàn; UPDATE nhiều đơn cùng lúc; không mở lại đơn kết thúc.
- Hai tài khoản thanh toán đồng thời khi còn một sản phẩm: chỉ một đơn được tạo, tồn kho không âm.
- Video: JSP player có nguồn nội bộ, MP4 trả HTTP 206 và byte range cho tua video; thư viện/tìm kiếm/phân trang và quản lý nguồn phát render thành công.
- OTP sử dụng SMTP giả lập trên loopback, không gửi thư ra Internet. SMTP thật vẫn cần cấu hình và kiểm tra bằng hộp thư của người dùng.
- Kiểm tra trực quan bằng trình duyệt tự động chưa thực hiện được vì phiên công cụ không cung cấp trình duyệt. Kiểm thử JSP/HTTP đã chạy trên server thật.

Chi tiết tự động: `target/commerce-results.json`; database kiểm thử: `BAITAP11_test_20261005_092914`. Database kiểm thử được giữ riêng, không dùng làm dữ liệu ứng dụng. Chạy lại bằng `tools/commerce-smoke-test.ps1` sẽ tạo database thử mới.
