<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, controller.Employe" %>
<%
    String titre = (String) request.getAttribute("titre");
    List<Employe> employes = (List<Employe>) request.getAttribute("employes");
    String erreur = (String) request.getAttribute("erreur");
%>
<!DOCTYPE html>
<html>
<head><title><%= titre != null ? titre : "Employes" %></title></head>
<body>
    <h1><%= titre != null ? titre : "Liste des employes" %></h1>

    <% if (erreur != null) { %>
        <p style="color:red;"><%= erreur %></p>
    <% } %>

    <table border="1">
        <tr><th>ID</th><th>Nom</th><th>Prenom</th><th>Email</th></tr>
        <% if (employes != null) {
            for (Employe e : employes) { %>
                <tr>
                    <td><%= e.getId() %></td>
                    <td><%= e.getNom() %></td>
                    <td><%= e.getPrenom() %></td>
                    <td><%= e.getEmail() %></td>
                </tr>
        <%  }
        } %>
    </table>

    <p><a href="<%= request.getContextPath() %>/emp/new">Nouvel employe</a></p>
    <p><a href="<%= request.getContextPath() %>/">Accueil</a></p>
</body>
</html>
