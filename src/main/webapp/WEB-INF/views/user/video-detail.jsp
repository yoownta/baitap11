<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <title>Chi tiết video</title>
</head>
<body>
<c:if test="${not empty message}"><p class="success"><c:out value="${message}"/></p></c:if>
<p><a href="${pageContext.request.contextPath}/home" class="button-secondary">⬅ Về trang chủ</a></p>
<article class="video-detail">
  <div>
    <c:choose><c:when test="${not empty mediaUrl}">
      <c:url var="playUrl" value="${mediaUrl}"/>
      <video id="lesson-player" class="lesson-player" controls playsinline preload="metadata" src="<c:out value='${playUrl}'/>">Trình duyệt không hỗ trợ phát video.</video>
      <div class="player-tools"><label>Tốc độ phát <select id="playback-speed"><option value="0.75">0.75×</option><option value="1" selected>1×</option><option value="1.25">1.25×</option><option value="1.5">1.5×</option><option value="2">2×</option></select></label><button id="restart-video" type="button" class="button-secondary">Xem từ đầu</button></div>
      <p id="player-message" class="muted" role="status">Tiến độ xem được lưu trên trình duyệt này.</p>
      <script src="${pageContext.request.contextPath}/assets/js/player.js" defer></script>
    </c:when><c:otherwise><img class="lesson-player" src="${pageContext.request.contextPath}<c:out value='${video.poster}'/>" alt="Ảnh bài giảng"><p class="muted">Bài giảng chưa có file video. Quản trị viên có thể gắn nguồn MP4/WebM trong quản lý cửa hàng.</p></c:otherwise></c:choose>
  </div>
  <div>
    <h1><c:out value="${video.title}"/></h1>
    <div class="video-meta">
      <span class="badge">Mã: <c:out value="${video.videoId}"/></span>
      <span class="badge">Danh mục: <c:out value="${video.categoryName}"/></span>
      <span class="badge">👁 <c:out value="${video.views}"/> Lượt xem</span>
      <span class="badge">👍 <c:out value="${video.likeCount}"/> Thích</span>
      <span class="badge">↗ <c:out value="${video.shareCount}"/> Chia sẻ</span>
    </div>
    <div class="description">
      <h3>Mô tả</h3>
      <c:out value="${video.description}"/>
    </div>
  </div>
</article><p><a class="button-primary" href="${pageContext.request.contextPath}/shop">Mua bộ tài liệu thực hành →</a></p>
</body>
</html>
