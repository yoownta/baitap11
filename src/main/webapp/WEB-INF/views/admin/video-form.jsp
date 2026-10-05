<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>${mode == 'create' ? 'Thêm video' : 'Sửa video'}</title>
</head>
<body>
  <h1>${mode == 'create' ? 'Thêm video mới' : 'Cập nhật video'}</h1>
  <c:if test="${not empty error}">
    <p class="error"><c:out value="${error}"/></p>
  </c:if>
  <c:if test="${empty categories}">
    <p class="error">Chưa có category nào, vui lòng thêm category trước.</p>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/admin/videos">
    <input type="hidden" name="action" value="${mode}">
    <c:if test="${mode == 'update'}">
      <input type="hidden" name="id" value="<c:out value='${video.videoId}'/>">
      <label>Mã video:
        <input type="text" value="<c:out value='${video.videoId}'/>" readonly disabled>
      </label>
    </c:if>
    <label>Tiêu đề:
      <input type="text" name="title" required maxlength="200" value="<c:out value='${video.title}'/>">
    </label>
    <label>Poster:
      <input type="text" name="poster" list="posters" maxlength="50" value="<c:out value='${video.poster}'/>" placeholder="/assets/images/poster.svg">
      <datalist id="posters"><option value="/assets/images/poster.svg"/><option value="/assets/images/java.svg"/><option value="/assets/images/web.svg"/><option value="/assets/images/database.svg"/></datalist>
    </label>
    <label>Category:
      <select name="categoryId" required>
        <c:forEach var="cat" items="${categories}">
          <option value="${cat.categoryId}" ${cat.categoryId == video.categoryId ? 'selected' : ''}>
            <c:out value="${cat.categoryName}"/>
          </option>
        </c:forEach>
      </select>
    </label>
    <label>Views:
      <input type="number" name="views" min="0" max="2147483647" value="<c:out value='${not empty viewsInput ? viewsInput : video.views}'/>">
    </label>
    <label>Mô tả:
      <textarea name="description" maxlength="500" rows="4"><c:out value="${video.description}"/></textarea>
    </label>
    <label>
      <input type="checkbox" name="active" value="1" ${video.active ? 'checked' : ''}> Hoạt động
    </label>
    <button type="submit" style="margin-top: 15px;">Lưu</button>
    <a class="button-secondary" href="${pageContext.request.contextPath}/admin/videos">Hủy</a>
  </form>
</body>
</html>
