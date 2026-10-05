<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Đăng nhập</title>
</head>
<body>
  <h1>Đăng nhập hệ thống</h1>
  <c:if test="${not empty error}">
    <p class="error"><c:out value="${error}"/></p>
  </c:if>
  <c:if test="${not empty message}">
    <p style="color: green;"><c:out value="${message}"/></p>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/login">
    <label>Tên đăng nhập:
      <input type="text" name="username" required maxlength="50" autofocus autocomplete="username" value="<c:out value='${username}'/>">
    </label>
    <label>Mật khẩu:
      <input type="password" name="password" required maxlength="128" autocomplete="current-password">
    </label>
    <button type="submit" style="margin-top: 15px;">Đăng nhập</button>
  </form>
  <p>Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
</body>
</html>
