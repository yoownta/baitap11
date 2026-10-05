<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Trang chủ video</title>
</head>
<body>
<section class="hero"><span class="eyebrow">THƯ VIỆN HỌC TẬP / BÀI TẬP 11</span><h1>Học bằng video.<br>Hiểu bằng thực hành.</h1><p>Khám phá bài giảng Java, Web và cơ sở dữ liệu. Xem lại bất cứ lúc nào, luyện tập theo nhịp của bạn.</p><a class="button-primary" href="${pageContext.request.contextPath}/shop">Khám phá bộ học tập →</a></section>
<c:if test="${empty sections}"><p>Chưa có danh mục nào.</p></c:if>
<c:forEach var="section" items="${sections}">
  <section class="category" id="cat-${section.categoryId}">
    <h2><c:out value="${section.categoryName}"/> (<c:out value="${section.videoPage.totalItems}"/> video)</h2>
    <c:if test="${empty section.videoPage.items}"><p>Danh mục này chưa có video.</p></c:if>
    <div class="video-grid">
      <c:forEach var="v" items="${section.videoPage.items}">
        <c:url var="detailUrl" value="/video"><c:param name="id" value="${v.videoId}"/></c:url>
        <c:url var="posterUrl" value="${v.poster}"/>
        <article class="video-card">
          <a href="<c:out value='${detailUrl}'/>" style="display:block; width:100%; aspect-ratio:16/9; background:#000;">
            <c:choose>
                <c:when test="${v.videoFile}">
                    <video src="<c:out value='${posterUrl}'/>?v=4" style="width:100%; height:100%; object-fit:cover;" muted loop onmouseover="this.play()" onmouseout="this.pause()"></video>
                </c:when>
                <c:otherwise>
                    <img src="<c:out value='${posterUrl}'/>?v=4" alt="Poster video" style="width:100%; height:100%; object-fit:cover;">
                </c:otherwise>
            </c:choose>
          </a>
          <div class="video-info">
            <h3><a href="<c:out value='${detailUrl}'/>"><c:out value="${v.title}"/></a></h3>
            <p><c:out value="${v.categoryName}"/></p>
            <div class="video-meta" style="margin: 8px 0;">
              <span class="badge" title="Lượt xem">👁 <c:out value="${v.views}"/></span>
              <span class="badge" title="Lượt thích">👍 <c:out value="${v.likeCount}"/></span>
              <span class="badge" title="Lượt chia sẻ">↗ <c:out value="${v.shareCount}"/></span>
            </div>
          </div>
        </article>
      </c:forEach>
    </div>
    <c:if test="${section.videoPage.totalPages > 1}">
      <nav class="pagination" aria-label="Phân trang category">
        <c:if test="${section.videoPage.page > 1}">
          <a href="<c:out value='${section.firstUrl}'/>">&laquo;</a>
          <a href="<c:out value='${section.previousUrl}'/>">Trước</a>
        </c:if>
        <c:forEach var="entry" items="${section.pageUrls}">
          <c:choose>
            <c:when test="${entry.key == section.videoPage.page}">
              <a href="#" style="background: #38bdf8; color: #0f172a; pointer-events: none;"><c:out value="${entry.key}"/></a>
            </c:when>
            <c:otherwise>
              <a href="<c:out value='${entry.value}'/>"><c:out value="${entry.key}"/></a>
            </c:otherwise>
          </c:choose>
        </c:forEach>
        <c:if test="${section.videoPage.page < section.videoPage.totalPages}">
          <a href="<c:out value='${section.nextUrl}'/>">Sau</a>
          <a href="<c:out value='${section.lastUrl}'/>">&raquo;</a>
        </c:if>
      </nav>
    </c:if>
  </section>
</c:forEach>
</body>
</html>
