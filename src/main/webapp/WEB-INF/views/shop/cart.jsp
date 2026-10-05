<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="vi"><head><title>Giỏ hàng</title></head><body><fmt:setLocale value="vi_VN"/>
<span class="eyebrow">01 / GIỎ HÀNG → 02 / THANH TOÁN → 03 / THEO DÕI</span><h1>Giỏ hàng của bạn</h1>
<c:if test="${not empty message}"><p class="success" role="status"><c:out value="${message}"/></p></c:if><c:if test="${not empty error}"><p class="error" role="alert"><c:out value="${error}"/></p></c:if>
<c:choose><c:when test="${empty cart}"><div class="empty-state"><h2>Giỏ hàng đang trống</h2><p>Chọn bộ tài liệu để bắt đầu thực hành.</p><a class="button-secondary" href="${pageContext.request.contextPath}/shop">Đến cửa hàng</a></div></c:when><c:otherwise>
<div class="table-scroll"><table><thead><tr><th>Sản phẩm</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th>Xóa</th></tr></thead><tbody>
<c:forEach var="p" items="${cart}"><tr><td><strong><c:out value="${p.Title}"/></strong><p>Tối đa hiện tại: ${p.Limit}</p><c:if test="${not p.Valid}"><p class="error">Hết hàng, ngừng bán hoặc vượt giới hạn. Hãy sửa/xóa sản phẩm.</p></c:if></td>
<td><fmt:formatNumber value="${p.Price}"/> ₫</td><td><form method="post" class="quantity-form"><input type="hidden" name="csrf" value="${csrf}"><input type="hidden" name="action" value="update"><input type="hidden" name="productId" value="${p.ProductId}"><input aria-label="Số lượng sản phẩm ${p.ProductId}" type="number" name="quantity" min="1" max="${p.Limit > 0 ? p.Limit : 1}" value="${p.Quantity}" required><button>Sửa</button></form></td>
<td><fmt:formatNumber value="${p.Subtotal}"/> ₫</td><td><form method="post"><input type="hidden" name="csrf" value="${csrf}"><input type="hidden" name="action" value="remove"><input type="hidden" name="productId" value="${p.ProductId}"><button class="btn-danger">Xóa</button></form></td></tr></c:forEach>
</tbody></table></div><div class="summary-bar"><a href="${pageContext.request.contextPath}/shop">← Tiếp tục mua sắm</a><div><p>Tổng thanh toán: <strong class="price"><fmt:formatNumber value="${total}"/> ₫</strong></p><p>Phí giao hàng: 0 ₫</p><c:if test="${canCheckout}"><a class="button-primary" href="${pageContext.request.contextPath}/checkout">Tiến hành thanh toán →</a></c:if></div></div>
<form method="post"><input type="hidden" name="csrf" value="${csrf}"><input type="hidden" name="action" value="clear"><button class="button-secondary">Xóa toàn bộ giỏ hàng</button></form>
</c:otherwise></c:choose></body></html>
