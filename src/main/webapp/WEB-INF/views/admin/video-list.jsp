<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Quản lý video</title>
</head>
<body>
  <h1>Danh sách video</h1>
  <c:if test="${not empty message}"><p class="success"><c:out value="${message}"/></p></c:if>
  <p>Tổng: <c:out value="${videoPage.totalItems}"/> video — 6 video/trang.</p>
  <p><a href="${pageContext.request.contextPath}/admin/videos?action=create"><button type="button">Thêm video mới</button></a></p>

  <c:if test="${empty videoPage.items}">
    <p>Chưa có video.</p>
  </c:if>
  <c:if test="${not empty videoPage.items}">
    <table>
      <thead>
        <tr>
          <th>Mã</th>
          <th>Poster</th>
          <th>Tiêu đề</th>
          <th>Category</th>
          <th>Views</th>
          <th>Active</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="v" items="${videoPage.items}">
          <c:url var="posterUrl" value="${v.poster}"/>
          <c:url var="viewUrl" value="/admin/videos"><c:param name="action" value="view"/><c:param name="id" value="${v.videoId}"/></c:url>
          <c:url var="editUrl" value="/admin/videos"><c:param name="action" value="edit"/><c:param name="id" value="${v.videoId}"/></c:url>
          <tr>
            <td><c:out value="${v.videoId}"/></td>
            <td><img src="<c:out value='${posterUrl}'/>?v=4" alt="poster" width="100"></td>
            <td><c:out value="${v.title}"/></td>
            <td><c:out value="${v.categoryName}"/></td>
            <td><c:out value="${v.views}"/></td>
            <td><c:out value="${v.active ? 'Có' : 'Không'}"/></td>
            <td>
              <a href="<c:out value='${viewUrl}'/>">Xem</a> |
              <a href="<c:out value='${editUrl}'/>">Sửa</a>
              <form method="post" action="${pageContext.request.contextPath}/admin/videos" class="inline" onsubmit="return confirm('Xóa video này?');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="<c:out value='${v.videoId}'/>">
                <input type="hidden" name="page" value="${videoPage.page}">
                <button type="submit" style="color: red;">Xóa</button>
              </form>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <c:if test="${videoPage.totalPages > 1}">
      <nav class="pagination">
        <c:if test="${videoPage.page > 1}">
          <a href="${pageContext.request.contextPath}/admin/videos?page=1">Đầu</a>
          <a href="${pageContext.request.contextPath}/admin/videos?page=${videoPage.page - 1}">Trước</a>
        </c:if>
        <c:forEach var="p" begin="1" end="${videoPage.totalPages}">
          <c:choose>
            <c:when test="${p == videoPage.page}">
              <strong><c:out value="${p}"/></strong>
            </c:when>
            <c:otherwise>
              <a href="${pageContext.request.contextPath}/admin/videos?page=${p}"><c:out value="${p}"/></a>
            </c:otherwise>
          </c:choose>
        </c:forEach>
        <c:if test="${videoPage.page < videoPage.totalPages}">
          <a href="${pageContext.request.contextPath}/admin/videos?page=${videoPage.page + 1}">Sau</a>
          <a href="${pageContext.request.contextPath}/admin/videos?page=${videoPage.totalPages}">Cuối</a>
        </c:if>
      </nav>
    </c:if>
  </c:if>
</body>
</html>
