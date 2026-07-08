<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String titre = (String) request.getAttribute("titre");
    String message = (String) request.getAttribute("message");
%>
<!DOCTYPE html>
<html>
<head><title><%= titre != null ? titre : "Test" %></title></head>
<body>
    <h1><%= titre != null ? titre : "Page de test" %></h1>
    <p><%= message != null ? message : "" %></p>
    <p><a href="<%= request.getContextPath() %>/">Accueil</a></p>
</body>
</html>
