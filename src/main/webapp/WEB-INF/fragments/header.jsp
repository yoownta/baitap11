<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header><nav style="display: flex; align-items: center; justify-content: space-between;">
  <div style="display: flex; gap: 20px; align-items: center;">
    <a href="${pageContext.request.contextPath}/home" style="margin-right: 0;">Trang Chủ</a>
    <a href="${pageContext.request.contextPath}/shop">Cửa hàng</a>
    <a href="${pageContext.request.contextPath}/library">Thư viện</a>
    <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>
    <a href="${pageContext.request.contextPath}/orders">Đơn hàng</a>
    <c:if test="${not empty sessionScope.authUser and sessionScope.authUser.admin}">
      <a href="${pageContext.request.contextPath}/admin/videos" style="margin-right: 0;">Trang quản trị</a>
      <a href="${pageContext.request.contextPath}/admin/shop">Quản lý cửa hàng</a>
      <a href="${pageContext.request.contextPath}/admin/orders">Quản lý đơn hàng</a>
      <a href="${pageContext.request.contextPath}/admin/media">Nguồn video</a>
    </c:if>
  </div>
  <div style="display: flex; gap: 15px; align-items: center;">
    <c:choose>
      <c:when test="${empty sessionScope.authUser}">
        <a href="${pageContext.request.contextPath}/login" style="margin-right: 0;">Đăng nhập</a>
        <a href="${pageContext.request.contextPath}/register" style="margin-right: 0;">Đăng ký</a>
      </c:when>
      <c:otherwise>
        <span><c:out value="${sessionScope.authUser.fullName}"/></span>
        <form method="post" action="${pageContext.request.contextPath}/logout" class="inline" style="margin: 0;">
          <button type="submit" style="padding: 6px 16px;">Đăng xuất</button>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</nav></header>
