<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%!
    private static String esc(Object valeur) {
        if (valeur == null) return "";
        return String.valueOf(valeur)
                .replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
%>
<%
    String titre = (String) request.getAttribute("titre");
    String message = (String) request.getAttribute("message");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><%= esc(titre) %></title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 760px; margin: 40px auto; padding: 0 20px; color: #2c3e50; }
        h1 { color: #2c3e50; }
        .ok { background: #e8f8ef; border-left: 4px solid #27ae60; padding: 12px 16px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        .bouton { background: #3498db; color: #fff; border: 0; padding: 10px 18px; border-radius: 4px;
                  cursor: pointer; font-size: 15px; text-decoration: none; display: inline-block; }
        .bouton:hover { background: #2980b9; }
    </style>
</head>
<body>

<h1><%= esc(titre) %></h1>

<p class="ok"><%= esc(message) %></p>

<p>
    <code>session.invalidate()</code> a detruit la session cote serveur.
    Le cookie JSESSIONID n'est plus valide : le navigateur repartira sur une
    nouvelle session vide a la prochaine visite.
</p>

<p>
    <a class="bouton" href="<%= ctx %>/session">Ouvrir une nouvelle session</a>
    <a class="bouton" href="<%= ctx %>/">Accueil</a>
</p>

</body>
</html>
