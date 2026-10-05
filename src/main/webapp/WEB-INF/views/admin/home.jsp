<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Trang quản trị</title>
</head>
<body>
  <h1>Chào mừng quản trị viên, <c:out value="${sessionScope.authUser.fullName}"/></h1>
  <ul>
    <li><a href="${pageContext.request.contextPath}/admin/videos">Quản lý video</a></li>
    <li><a href="${pageContext.request.contextPath}/home">Về trang chủ User</a></li>
  </ul>
</body>
</html>
