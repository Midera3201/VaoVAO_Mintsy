<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String titre = (String) request.getAttribute("titre");
%>
<!DOCTYPE html>
<html>
<head><title><%= titre != null ? titre : "Nouvel employe" %></title></head>
<body>
    <h1><%= titre != null ? titre : "Nouvel employe" %></h1>

    <form action="<%= request.getContextPath() %>/emp/list/" method="post">
        <label>Nom : <input type="text" name="nom" required></label><br>
        <label>Prenom : <input type="text" name="prenom" required></label><br>
        <label>Email : <input type="email" name="email" required></label><br>
        <button type="submit">Creer</button>
    </form>

    <p><a href="<%= request.getContextPath() %>/emp/list/">Retour a la liste</a></p>
</body>
</html>
