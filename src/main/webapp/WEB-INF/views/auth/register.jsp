<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Đăng ký</title>
</head>
<body>
  <h1>Đăng ký tài khoản</h1>
  <p>Nếu chưa kích hoạt, nhập lại đúng tên đăng nhập, email và mật khẩu cũ để tiếp tục nhận OTP.</p>
  <c:if test="${not empty error}">
    <p class="error"><c:out value="${error}"/></p>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/register">
    <label>Tên đăng nhập:
      <input type="text" name="username" required maxlength="50" pattern="[A-Za-z0-9_.-]{3,50}" title="3-50 ký tự" value="<c:out value='${username}'/>">
    </label>
    <label>Mật khẩu:
      <input type="password" name="password" required minlength="8" maxlength="128">
    </label>
    <label>Xác nhận mật khẩu:
      <input type="password" name="confirmPassword" required minlength="8" maxlength="128">
    </label>
    <label>Họ và tên:
      <input type="text" name="fullName" required maxlength="50" value="<c:out value='${fullName}'/>">
    </label>
    <label>Email:
      <input type="email" name="email" required maxlength="150" value="<c:out value='${email}'/>">
    </label>
    <label>Số điện thoại:
      <input type="text" name="phone" maxlength="15" value="<c:out value='${phone}'/>">
    </label>
    <button type="submit" style="margin-top: 15px;">Đăng ký</button>
  </form>
  <p>Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
</body>
</html>
