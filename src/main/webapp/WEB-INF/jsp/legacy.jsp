<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>JSP 原理演示</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body class="legacy-page">
<main class="narrow-card">
    <p class="eyebrow">LEGACY VIEW</p>
    <h1>JSP 本质上仍是 Servlet</h1>
    <p>该文件位于 <code>WEB-INF</code>，只能由 Servlet 转发访问。Tomcat 会把 JSP 翻译并编译为 Servlet。</p>
    <dl class="detail-list">
        <dt>Servlet 写入的 request attribute</dt>
        <dd><%= request.getAttribute("generatedAt") %></dd>
        <dt>JSP 运行时类</dt>
        <dd><%= this.getClass().getName() %></dd>
        <dt>Session ID</dt>
        <dd><%= session.getId() %></dd>
    </dl>
    <a class="button secondary" href="${pageContext.request.contextPath}/lab">返回开发者实验室</a>
</main>
</body>
</html>
