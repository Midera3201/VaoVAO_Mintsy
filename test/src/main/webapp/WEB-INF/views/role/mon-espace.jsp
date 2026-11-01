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
    String message = (String) request.getAttribute("message");
    String utilisateur = (String) request.getAttribute("utilisateur");
    String role = (String) request.getAttribute("role");
    String sessionId = (String) request.getAttribute("sessionId");
    String rolesAutorises = (String) request.getAttribute("rolesAutorises");
    String attributRole = (String) request.getAttribute("attributRole");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><%= esc(titre) %></title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 820px; margin: 40px auto; padding: 0 20px; color: #2c3e50; }
        h1 { color: #2c3e50; }
        fieldset { border: 1px solid #ddd; border-radius: 6px; margin: 20px 0; padding: 16px 20px; }
        legend { font-weight: bold; color: #34495e; padding: 0 8px; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        .ok { background: #e8f8ef; border-left: 4px solid #27ae60; padding: 10px 14px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        table { border-collapse: collapse; margin: 10px 0; width: 100%; }
        th, td { border: 1px solid #d5dbe1; padding: 7px 10px; text-align: left; }
        th { background: #2c3e50; color: #fff; }
        .avatar { display: inline-block; width: 42px; height: 42px; line-height: 42px; text-align: center;
                  border-radius: 50%; background: #2980b9; color: #fff; font-weight: bold; font-size: 20px; margin-right: 10px; }
        button, .bouton { background: #3498db; color: #fff; border: 0; padding: 9px 16px; border-radius: 4px;
                           cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        button:hover, .bouton:hover { background: #2980b9; }
        .danger { background: #c0392b; }
    </style>
</head>
<body>

<h1><%= esc(titre) %></h1>

<p class="ok">
    <%= esc(message) %>
    Votre role <strong><%= esc(role) %></strong> est autorise
    (<code><%= esc(rolesAutorises) %></code>).
</p>

<p class="note">
    Cette page est reservee au role <strong>USER</strong> avec
    <code>@RolesAutorises({"USER"})</code>. Un administrateur connecte qui
    tente d'ouvrir cette URL recoit une erreur HTTP 403, car ADMIN n'est pas
    dans la liste des roles autorises pour cette methode.
</p>

<fieldset>
    <legend>Mon espace personnel</legend>
    <p>
        <span class="avatar"><%= esc(utilisateur == null || utilisateur.isBlank() ? "?" : utilisateur.substring(0, 1).toUpperCase()) %></span>
        <strong><%= esc(utilisateur) %></strong>
    </p>
    <table>
        <tr><th>Utilisateur</th><td><%= esc(utilisateur) %></td></tr>
        <tr><th>Role</th><td><%= esc(role) %></td></tr>
        <tr><th>Attribut de role</th><td><code><%= esc(attributRole) %></code></td></tr>
        <tr><th>Identifiant de session</th><td><code><%= esc(sessionId) %></code></td></tr>
    </table>
</fieldset>

<fieldset>
    <legend>Navigation</legend>
    <p>
        <a class="bouton" href="<%= ctx %>/espace-partage">Espace partage (ADMIN + USER)</a>
        <a class="bouton" href="<%= ctx %>/admin">Espace protege (Sprint 10)</a>
        <a class="bouton" href="<%= ctx %>/session">Session</a>
    </p>
    <form method="post" action="<%= ctx %>/session/deconnexion" style="display:inline;">
        <button type="submit" class="danger">Se deconnecter</button>
    </form>
</fieldset>

<p><a href="<%= ctx %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
