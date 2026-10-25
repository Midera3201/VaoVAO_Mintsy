<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
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
    String sessionId = (String) request.getAttribute("sessionId");
    String utilisateur = (String) request.getAttribute("utilisateur");
    Integer visites = (Integer) request.getAttribute("visites");
    Boolean connecte = (Boolean) request.getAttribute("connecte");
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
        .compteur { font-size: 64px; font-weight: bold; color: #3498db; margin: 10px 0; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        button, .bouton { background: #3498db; color: #fff; border: 0; padding: 9px 16px; border-radius: 4px;
                           cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        button:hover, .bouton:hover { background: #2980b9; }
        .danger { background: #c0392b; }
    </style>
</head>
<body>

<h1><%= esc(titre) %></h1>

<p class="note">
    Chaque chargement de cette page incremente <code>visites</code> dans la session.
    Recharge plusieurs fois : le compteur continue, ce qui prouve que la donnee
    est conservee entre les requetes HTTP.
</p>

<div class="compteur"><%= visites == null ? 0 : visites %></div>
<p>visite(s) pour cette session.</p>

<p>
    Session : <code><%= esc(sessionId) %></code><br>
    Utilisateur : <strong><%= Boolean.TRUE.equals(connecte) ? esc(utilisateur) : "non connecte" %></strong>
</p>

<p>
    <a class="bouton" href="<%= ctx %>/session/compteur">Recharger / incrementer</a>
    <a class="bouton" href="<%= ctx %>/session/profil">Voir le profil</a>
    <a class="bouton" href="<%= ctx %>/session">Accueil session</a>
</p>

<p><a href="<%= ctx %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
