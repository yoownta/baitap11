# Bài tập 11 — Video Learning & COD Shop

**Phan Tuấn Thanh — 24133054.** Phát triển từ `D:\Web\ktqt\24133054_made`, giữ Java 17, Servlet/JSP/JSTL, JPA/Hibernate, SiteMesh, Maven WAR và SQL Server. Bản mới nằm riêng tại `D:\Web\baitap11`.

## Chạy trên máy hiện tại

```powershell
cd D:\Web\baitap11
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Port 8082
```

Mở **http://localhost:8082/ktqt03/home**. Giữ cửa sổ server mở; Ctrl+C để dừng. Nếu đã build: thêm `-SkipBuild`. Context path `ktqt03` được giữ để tương thích source gốc; cổng 8082 tránh trùng bản cũ.

`config.local.ps1` được sao chép riêng trên máy, đổi database thành **BAITAP11_24133054**, không đưa vào Git/ZIP nộp bài. Khi chuyển máy, copy `config.example.ps1` thành `config.local.ps1` rồi cấu hình SQL và SMTP. Không có mật khẩu đăng nhập mặc định. Đăng ký → xác minh OTP qua SMTP → đăng nhập. Có thể nâng tài khoản đã kích hoạt thành admin:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\create-admin.ps1 -Username TEN_DANG_NHAP
```

## Chức năng

- `/home`: danh mục và phân trang video; `/library`: tìm bài theo tên/danh mục, 12 bài/trang.
- `/video?id=...`: phát MP4/WebM, tua, toàn màn hình theo trình duyệt, đổi tốc độ, xem lại từ đầu, nhớ tiến độ trên trình duyệt. Hai bài Java có file MP4 nội bộ. Các bài còn lại hiển thị rõ khi chưa có nguồn, có thể gắn nguồn ở `/admin/media`.
- `/shop`: bộ tài liệu thực hành được giao hàng; xem video vẫn miễn phí. Tìm kiếm sản phẩm, xem giá, tồn kho, giới hạn số lượng, thêm giỏ.
- `/cart`: giỏ lưu theo tài khoản trong SQL Server; thêm, sửa số lượng, xóa từng món, xóa tất cả. Giới hạn từ 1 đến min(tồn kho, giới hạn sản phẩm), tối đa 99. Tồn kho không được giữ chỉ bằng việc thêm giỏ.
- `/checkout`: người nhận, số điện thoại Việt Nam, địa chỉ, ghi chú; COD, phí giao hàng 0 đồng. Kiểm tra giá/giỏ thay đổi, hàng ngừng bán hoặc không đủ tồn. Giao dịch lưu đơn/chi tiết, trừ kho, xóa giỏ đồng thời. Token chống tạo đơn trùng khi gửi lại yêu cầu.
- `/orders`: lịch sử riêng mỗi tài khoản, 20 đơn/trang, lọc đủ tám trạng thái, hiển thị chi tiết và diễn biến; chủ đơn được hủy khi đơn còn mới.
- `/admin/shop`: thêm/sửa giá, tồn kho, giới hạn, ngừng bán, liên kết video.
- `/admin/orders`: xem đơn tất cả tài khoản, lọc trạng thái, chuyển theo tiến trình hợp lệ.
- `/admin/media`: gắn/gỡ URL MP4/WebM cho từng video; `/admin/videos`: CRUD video gốc.

Giá và tên sản phẩm được chụp vào chi tiết đơn khi mua; sửa sản phẩm sau đó không thay đổi đơn cũ. Giỏ/checkout/admin được bảo vệ bằng đăng nhập, quyền admin, CSRF, validation phía server và câu SQL có tham số. API đọc lịch sử không cache trạng thái.

## Quan sát trạng thái bằng database

Khởi động app một lần để tạo database/schema. Trong SSMS chọn **BAITAP11_24133054**, mở `tools/order-status-demo.sql`. Thay `@OrderId` bằng mã đơn của bạn và chọn `@Status`, chạy UPDATE rồi tải lại `/orders`. Dùng một đơn mới để thử lần lượt:

| Giá trị SQL | Trạng thái giao diện |
|---|---|
| NEW | Đơn hàng mới |
| CONFIRMED | Đã xác nhận |
| PREPARING | Chuẩn bị hàng |
| SHIPPING | Vận chuyển |
| DELIVERING | Giao hàng |
| DELIVERED | Đã giao |
| CANCELLED | Đơn hàng hủy |
| RETURNED | Đơn hàng hoàn |

Luồng quản trị: NEW → CONFIRMED → PREPARING → SHIPPING → DELIVERING → DELIVERED; hủy từ NEW/CONFIRMED/PREPARING, hoàn từ SHIPPING/DELIVERING/DELIVERED. Khi thử trong database, có thể chuyển trực tiếp giữa các trạng thái chưa kết thúc để quan sát bộ lọc. **CANCELLED và RETURNED là trạng thái kết thúc, không mở lại đơn đó**; tạo đơn khác để thử nhánh còn lại.

Trigger `TR_ShopOrders_Status` ghi diễn biến và `UpdatedAt` cả khi UPDATE bằng SSMS. Chuyển sang hủy/hoàn sẽ trả số lượng vào kho đúng một lần (`StockRestored`); hỗ trợ UPDATE nhiều đơn trong một câu lệnh. Actor của cập nhật là tài khoản kết nối SQL; sự kiện tạo mới lưu username ứng dụng. Chỉ cập nhật cột `Status` khi thử; không sửa `StockRestored`, chi tiết đơn hoặc tổng tiền bằng tay.

`database.sql` là bản đầy đủ để chạy SSMS. Các migration trong `src/main/resources/db/` chạy khi `KTQT_DB_INIT=true`, không xóa dữ liệu cũ và không seed trùng. Nếu tự chạy SQL, đặt `KTQT_DB_INIT=false` sau khi đã chạy đầy đủ. Đổi SQL nguồn thì tạo lại bản tổng hợp:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\export-database.ps1
```

## Build và kiểm thử

```powershell
& D:\Web\apache-maven-3.9.16\bin\mvn.cmd "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" -DforkCount=0 package
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\commerce-smoke-test.ps1
```

WAR: `target/ktqt03.war`. Unit test kiểm tra OTP, validation, giới hạn số lượng, URL media, chuyển trạng thái. Integration test chạy SQL Server thật, Tomcat/JSP và SMTP giả lập trên loopback, tạo DB riêng `BAITAP11_test_...`, không gửi email ra Internet. Báo cáo ở `target/commerce-results.json`, log ở `target/commerce-server.log`. Test giữ DB riêng để quan sát; không thay dữ liệu ứng dụng.

## Nộp bài

Nộp source, `.mvn`, `pom.xml`, `database.sql`, `run.ps1`, `config.example.ps1`, `tools`, README. Không nộp `config.local.ps1`, `.runtime*`, log, `.git` hoặc database kiểm thử. `baitap11-source.zip` được đặt ở thư mục cha để tiện nộp.

Các mốc yêu cầu: giỏ hàng và COD trước 10:45 ngày 05/10/2026; lịch sử/lọc trạng thái trước 07:45 ngày 07/10/2026. Commit được ghi bằng thời gian thực trên máy; không chỉnh lùi ngày. Commit cục bộ cần push lên repository của môn học nếu giảng viên chấm theo Git remote.
