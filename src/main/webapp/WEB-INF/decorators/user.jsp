<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><sitemesh:write property="title"/></title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css?v=11">
  <sitemesh:write property="head"/>
</head>
<body>
  <%@ include file="/WEB-INF/fragments/header.jsp" %>
  <main class="container"><sitemesh:write property="body"/></main>
  <%@ include file="/WEB-INF/fragments/footer.jsp" %>
</body>
</html>
