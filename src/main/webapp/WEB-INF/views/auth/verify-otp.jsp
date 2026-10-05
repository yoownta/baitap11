<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Xác minh OTP</title>
</head>
<body>
  <h1>Xác minh Email</h1>
  <p>Kiểm tra hộp thư đến và thư rác của <strong><c:out value="${sessionScope.pendingOtp.email}"/></strong>. OTP có hiệu lực 5 phút, tối đa 5 lần nhập sai.</p>

  <c:if test="${not empty error}">
    <p class="error"><c:out value="${error}"/></p>
  </c:if>
  <c:if test="${not empty message}">
    <p style="color: green;"><c:out value="${message}"/></p>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/verify-otp">
    <label>Mã OTP (6 số):
      <input type="text" name="otp" required inputmode="numeric" maxlength="6" pattern="[0-9]{6}">
    </label>
    <button type="submit" style="margin-top: 15px;">Xác minh</button>
  </form>

  <form method="post" action="${pageContext.request.contextPath}/verify-otp?action=resend" style="margin-top: 20px;">
    <button type="submit">Gửi lại mã OTP</button>
  </form>
</body>
</html>
