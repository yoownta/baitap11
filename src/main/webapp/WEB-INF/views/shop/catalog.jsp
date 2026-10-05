<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="vi"><head><title>Cửa hàng học tập</title></head><body>
<fmt:setLocale value="vi_VN"/>
<section class="hero"><span class="eyebrow">LEARN • PRACTICE • BUILD</span><h1>Bộ học tập dành cho bạn</h1><p>Xem bài giảng miễn phí. Đặt bộ tài liệu thực hành và thanh toán khi nhận hàng.</p><a class="button-secondary" href="${pageContext.request.contextPath}/home">Khám phá thư viện video →</a></section>
<c:if test="${not empty message}"><p class="success" role="status"><c:out value="${message}"/></p></c:if>
<c:if test="${not empty error}"><p class="error" role="alert"><c:out value="${error}"/></p></c:if>
<form method="get" class="searchbar"><label for="q">Tìm bộ tài liệu</label><input id="q" name="q" maxlength="100" value="<c:out value='${param.q}'/>" placeholder="Java, Web, cơ sở dữ liệu…"><button>Tìm kiếm</button></form>
<c:if test="${empty products}"><p class="empty-state">Chưa có sản phẩm phù hợp.</p></c:if>
<div class="video-grid">
<c:forEach var="p" items="${products}">
 <article class="video-card"><div class="product-art"><span>STUDY KIT</span><strong>Thực hành<br>từ bài giảng</strong></div><div class="video-info">
 <h2><c:out value="${p.Title}"/></h2><p class="price"><fmt:formatNumber value="${p.Price}"/> ₫</p>
 <p>Còn <c:out value="${p.Stock}"/> bộ · Tối đa <c:out value="${p.MaxQuantity}"/> bộ/đơn</p>
 <c:if test="${not empty p.VideoId}"><c:url var="watch" value="/video"><c:param name="id" value="${p.VideoId}"/></c:url><p><a href="<c:out value='${watch}'/>">▶ Xem video liên quan</a></p></c:if>
 <c:choose><c:when test="${p.Stock > 0 and p.Active}">
 <form method="post" action="${pageContext.request.contextPath}/shop" class="purchase-form"><input type="hidden" name="csrf" value="${csrf}"><input type="hidden" name="action" value="add"><input type="hidden" name="productId" value="${p.ProductId}">
 <label for="qty-${p.ProductId}">Số lượng</label><input id="qty-${p.ProductId}" type="number" name="quantity" min="1" max="${p.Stock < p.MaxQuantity ? p.Stock : p.MaxQuantity}" value="1" required><button>Thêm vào giỏ</button></form>
 </c:when><c:otherwise><p class="badge">Tạm hết hàng / ngừng bán</p></c:otherwise></c:choose>
 <c:if test="${adminShop}"><details><summary>Sửa sản phẩm & video</summary>
 <form method="post" action="${pageContext.request.contextPath}/admin/shop"><input type="hidden" name="csrf" value="${csrf}"><input type="hidden" name="productId" value="${p.ProductId}">
 <label>Tên sản phẩm<input name="title" value="<c:out value='${p.Title}'/>" maxlength="200" required></label>
 <label>Giá (đồng)<input name="price" type="number" min="1" max="1000000000" value="${p.Price}" required></label>
 <label>Tồn kho<input name="stock" type="number" min="0" value="${p.Stock}" required></label>
 <label>Giới hạn mỗi đơn<input name="maxQuantity" type="number" min="1" max="99" value="${p.MaxQuantity}" required></label>
 <label>Mã video liên quan<input name="videoId" value="<c:out value='${p.VideoId}'/>" maxlength="50"></label>
 <label>Nguồn MP4/WebM<input name="mediaUrl" value="<c:out value='${p.MediaUrl}'/>" maxlength="1000" placeholder="/videos/java-01.mp4 hoặc https://…/video.mp4"></label>
 <label><input type="checkbox" name="active" ${p.Active ? 'checked' : ''}> Đang bán</label><button>Lưu thay đổi</button></form></details></c:if>
 </div></article>
</c:forEach></div>
<c:if test="${adminShop}"><section class="panel"><h2>Thêm sản phẩm</h2><form method="post"><input type="hidden" name="csrf" value="${csrf}"><input type="hidden" name="productId" value="0">
<label>Tên sản phẩm<input name="title" maxlength="200" required></label><div class="form-grid"><label>Giá (đồng)<input name="price" type="number" min="1" max="1000000000" value="99000" required></label><label>Tồn kho<input name="stock" type="number" min="0" value="25" required></label><label>Giới hạn/đơn<input name="maxQuantity" type="number" min="1" max="99" value="5" required></label></div>
<label>Mã video liên quan<input name="videoId" maxlength="50"></label><label>URL video (MP4/WebM)<input name="mediaUrl" maxlength="1000"></label><label><input name="active" type="checkbox" checked> Đang bán</label><button>Thêm sản phẩm</button></form></section></c:if>
</body></html>
