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
    String erreur = (String) request.getAttribute("erreur");
    String sessionId = (String) request.getAttribute("sessionId");
    String utilisateur = (String) request.getAttribute("utilisateur");
    String role = (String) request.getAttribute("role");
    Boolean connecte = (Boolean) request.getAttribute("connecte");
    Boolean admin = (Boolean) request.getAttribute("admin");
    Boolean nouvelle = (Boolean) request.getAttribute("nouvelle");
    Integer nbAttributs = (Integer) request.getAttribute("nbAttributs");
    Integer duree = (Integer) request.getAttribute("dureeInactivite");
    boolean estConnecte = connecte != null && connecte;
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><%= esc(titre) %></title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; color: #2c3e50; }
        h1 { color: #2c3e50; }
        fieldset { border: 1px solid #ddd; border-radius: 6px; margin: 20px 0; padding: 16px 20px; }
        legend { font-weight: bold; color: #34495e; padding: 0 8px; }
        label { display: inline-block; width: 170px; }
        input[type=text], select { padding: 7px; border: 1px solid #ccc; border-radius: 4px; width: 240px; }
        button, .bouton { background: #3498db; color: #fff; border: 0; padding: 9px 16px; border-radius: 4px;
                           cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        button:hover, .bouton:hover { background: #2980b9; }
        .danger { background: #c0392b; }
        .danger:hover { background: #a93226; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        .ok { background: #e8f8ef; border-left: 4px solid #27ae60; padding: 10px 14px; }
        .erreur { background: #fdecea; border-left: 4px solid #c0392b; padding: 10px 14px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        table { border-collapse: collapse; margin: 10px 0; width: 100%; }
        th, td { border: 1px solid #d5dbe1; padding: 7px 10px; text-align: left; }
        th { background: #2c3e50; color: #fff; }
        tr:nth-child(even) td { background: #f7f9fb; }
    </style>
</head>
<body>

<h1><%= esc(titre) %></h1>

<p class="note">
    Le controleur recoit <code>Session session</code> en argument : le framework
    appelle <code>request.getSession()</code> puis injecte l'objet. Les donnees
    ecrites ici restent disponibles dans les requetes suivantes du meme navigateur.
</p>

<% if (erreur != null) { %>
    <p class="erreur"><%= esc(erreur) %></p>
<% } %>

<fieldset>
    <legend>Etat de la session</legend>
    <table>
        <tr><th>Identifiant (JSESSIONID)</th><td><code><%= esc(sessionId) %></code></td></tr>
        <tr><th>Nouvelle session ?</th><td><%= Boolean.TRUE.equals(nouvelle) ? "oui" : "non" %></td></tr>
        <tr><th>Utilisateur</th><td><%= estConnecte ? esc(utilisateur) : "<em>non connecte</em>" %></td></tr>
        <tr><th>Role</th><td><%= role == null ? "-" : esc(role) %><%= Boolean.TRUE.equals(admin) ? " (administrateur)" : "" %></td></tr>
        <tr><th>Attributs en session</th><td><%= nbAttributs == null ? 0 : nbAttributs %></td></tr>
        <tr><th>Expiration d'inactivite</th><td><%= duree != null && duree > 0 ? (duree / 60) + " minutes" : "jamais" %></td></tr>
    </table>
</fieldset>

<fieldset>
    <legend>Connexion (POST /session/connexion)</legend>
    <form method="post" action="<%= ctx %>/session/connexion">
        <p>
            <label for="nom">Nom de l'utilisateur</label>
            <input type="text" id="nom" name="nom" value="<%= esc(utilisateur) %>" placeholder="Midera">
        </p>
        <p>
            <label for="role">Role</label>
            <select id="role" name="role">
                <option value="utilisateur" <%= "utilisateur".equals(role) ? "selected" : "" %>>utilisateur</option>
                <option value="admin" <%= "admin".equals(role) ? "selected" : "" %>>admin</option>
            </select>
        </p>
        <button type="submit">Enregistrer dans la session</button>
    </form>
</fieldset>

<% if (estConnecte) { %>
    <fieldset>
        <legend>Actions</legend>
        <p>
            <a class="bouton" href="<%= ctx %>/session/profil">Voir le profil et les attributs</a>
            <a class="bouton" href="<%= ctx %>/session/compteur">Compteur de visites</a>
        </p>
        <form method="post" action="<%= ctx %>/session/deconnexion" style="display:inline;">
            <button type="submit" class="danger">Se deconnecter (invalidate)</button>
        </form>
    </fieldset>
<% } %>

<p><a href="<%= ctx %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
