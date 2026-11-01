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
    String initiales = (String) request.getAttribute("initiales");
    String sessionId = (String) request.getAttribute("sessionId");
    String attributControle = (String) request.getAttribute("attributControle");
    String derniereAction = (String) request.getAttribute("derniereAction");
    Boolean admin = (Boolean) request.getAttribute("admin");
    Integer actions = (Integer) request.getAttribute("actionsProtegees");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><%= esc(titre) %></title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 850px; margin: 40px auto; padding: 0 20px; color: #2c3e50; }
        h1 { color: #2c3e50; }
        fieldset { border: 1px solid #ddd; border-radius: 6px; margin: 20px 0; padding: 16px 20px; }
        legend { font-weight: bold; color: #34495e; padding: 0 8px; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        .ok { background: #e8f8ef; border-left: 4px solid #27ae60; padding: 10px 14px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        table { border-collapse: collapse; margin: 10px 0; width: 100%; }
        th, td { border: 1px solid #d5dbe1; padding: 7px 10px; text-align: left; }
        th { background: #2c3e50; color: #fff; }
        tr:nth-child(even) td { background: #f7f9fb; }
        .avatar { display: inline-block; width: 34px; height: 34px; line-height: 34px; text-align: center;
                  border-radius: 50%; background: #27ae60; color: #fff; font-weight: bold; margin-right: 8px; }
        button, .bouton { background: #3498db; color: #fff; border: 0; padding: 9px 16px; border-radius: 4px;
                           cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        button:hover, .bouton:hover { background: #2980b9; }
        .danger { background: #c0392b; }
        .danger:hover { background: #a93226; }
        .badge { background: #27ae60; color: #fff; padding: 2px 9px; border-radius: 10px; font-size: 12px; }
    </style>
</head>
<body>

<h1><%= esc(titre) %></h1>

<% if (message != null) { %>
    <p class="ok"><%= esc(message) %></p>
<% } %>

<p class="note">
    Cette page est produite par <code>@AuthentificationRequise</code>.
    Avant d'appeler la methode du controleur, le framework a verifie que
    l'attribut de session <code><%= esc(attributControle) %></code> existe et
    n'est pas vide. Sans connexion, la methode n'est jamais executee :
    le navigateur est redirige vers la page de connexion.
</p>
<p class="note" style="border-left-color:#e67e22;background:#fff8ee;">
    Le role est affiche a titre informatif. Ce sprint controle
    l'<strong>authentification</strong>, pas les droits lies au role :
    tout utilisateur connecte peut ouvrir cette page de demonstration.
</p>

<fieldset>
    <legend>Utilisateur authentifie</legend>
    <p>
        <span class="avatar"><%= esc(initiales) %></span>
        <strong><%= esc(utilisateur) %></strong>
        <%= Boolean.TRUE.equals(admin) ? "<span class='badge'>admin</span>" : "" %>
    </p>
    <table>
        <tr><th>Role</th><td><%= role == null ? "-" : esc(role) %></td></tr>
        <tr><th>Identifiant de session</th><td><code><%= esc(sessionId) %></code></td></tr>
        <tr><th>Attribut controle</th><td><code><%= esc(attributControle) %></code></td></tr>
        <tr><th>Actions protegees executees</th><td><%= actions == null ? 0 : actions %></td></tr>
        <tr><th>Derniere action</th><td><%= esc(derniereAction != null ? derniereAction : "-") %></td></tr>
    </table>
</fieldset>

<fieldset>
    <legend>Action protegee (POST /admin/action)</legend>
    <p>
        Cette action increment le compteur ci-dessus. Si l'acces est refuse,
        le compteur ne change pas : le code de la methode n'a pas ete execute.
    </p>
    <form method="post" action="<%= ctx %>/admin/action">
        <button type="submit">Executer l'action protegee</button>
    </form>
</fieldset>

<fieldset>
    <legend>Tester le refus automatique</legend>
    <p>
        <a class="bouton" href="<%= ctx %>/admin/utilisateurs">Gestion utilisateurs (ADMIN)</a>
        <a class="bouton" href="<%= ctx %>/mon-espace">Mon espace (USER)</a>
        <a class="bouton" href="<%= ctx %>/espace-partage">Espace partage (ADMIN + USER)</a>
    </p>
    <p>
        <a class="bouton" href="<%= ctx %>/api/admin" target="_blank">API protegee (JSON)</a>
        <a class="bouton" href="<%= ctx %>/session/profil">Profil de session</a>
        <a class="bouton" href="<%= ctx %>/session">Accueil session</a>
    </p>
    <p style="font-size:13px;color:#666;">
        Apres deconnexion, <code>/admin</code> redirige vers <code>/session</code>
        et <code>/api/admin</code> renvoie <code>401</code> avec un JSON d'erreur.
        Avec un role non autorise, les routes du Sprint 10 bis renvoient
        <code>403</code>.
    </p>
    <form method="post" action="<%= ctx %>/session/deconnexion" style="display:inline;">
        <button type="submit" class="danger">Se deconnecter puis tester /admin</button>
    </form>
</fieldset>

<p><a href="<%= ctx %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
