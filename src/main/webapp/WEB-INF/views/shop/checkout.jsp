<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="vi"><head><title>Thanh toán COD</title></head><body><fmt:setLocale value="vi_VN"/>
<span class="eyebrow">02 / THANH TOÁN</span><h1>Hoàn tất đơn hàng</h1><c:if test="${not empty error}"><p class="error" role="alert"><c:out value="${error}"/></p></c:if>
<c:choose><c:when test="${canCheckout}"><div class="checkout-grid"><section class="panel"><h2>Thông tin nhận hàng</h2>
<form method="post"><input type="hidden" name="csrf" value="${csrf}"><input type="hidden" name="checkoutToken" value="${checkoutToken}">
<label for="recipient">Họ tên người nhận</label><input id="recipient" name="recipient" maxlength="100" autocomplete="name" value="<c:out value='${checkoutFields.recipient}'/>" required>
<label for="phone">Điện thoại</label><input id="phone" name="phone" type="tel" pattern="0[0-9]{9,10}" maxlength="11" autocomplete="tel" placeholder="0901234567" value="<c:out value='${checkoutFields.phone}'/>" required>
<label for="address">Địa chỉ giao hàng đầy đủ</label><textarea id="address" name="address" rows="3" maxlength="500" autocomplete="street-address" required><c:out value="${checkoutFields.address}"/></textarea>
<label for="note">Ghi chú (tùy chọn)</label><textarea id="note" name="note" rows="2" maxlength="500"><c:out value="${checkoutFields.note}"/></textarea>
<p class="cod-box"><strong>COD — Thanh toán khi nhận hàng</strong><br>Thanh toán <fmt:formatNumber value="${total}"/> ₫ cho người giao hàng. Phí giao hàng 0 ₫.</p><button>Đặt hàng COD · <fmt:formatNumber value="${total}"/> ₫</button></form></section>
<aside class="panel"><h2>Kiểm tra đơn hàng</h2><c:forEach var="p" items="${cart}"><p><c:out value="${p.Title}"/> × ${p.Quantity}<br><strong><fmt:formatNumber value="${p.Subtotal}"/> ₫</strong></p></c:forEach><hr><p>Tổng cộng <strong class="price"><fmt:formatNumber value="${total}"/> ₫</strong></p><a href="${pageContext.request.contextPath}/cart">Chỉnh sửa giỏ hàng</a></aside></div></c:when>
<c:otherwise><p class="empty-state">Giỏ hàng trống hoặc có sản phẩm không hợp lệ. <a href="${pageContext.request.contextPath}/cart">Quay lại giỏ hàng</a></p></c:otherwise></c:choose></body></html>
