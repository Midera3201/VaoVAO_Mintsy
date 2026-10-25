<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
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
    String erreur = (String) request.getAttribute("erreur");
    String sessionId = (String) request.getAttribute("sessionId");
    String utilisateur = (String) request.getAttribute("utilisateur");
    String role = (String) request.getAttribute("role");
    String initiales = (String) request.getAttribute("initiales");
    String typeSession = (String) request.getAttribute("typeSession");
    String creation = (String) request.getAttribute("creation");
    String dernierAcces = (String) request.getAttribute("dernierAcces");
    Boolean connecte = (Boolean) request.getAttribute("connecte");
    Boolean admin = (Boolean) request.getAttribute("admin");
    Map<String, Object> attributs = (Map<String, Object>) request.getAttribute("attributs");
    boolean estConnecte = connecte != null && connecte;
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
        label { display: inline-block; width: 170px; }
        input[type=text], select { padding: 7px; border: 1px solid #ccc; border-radius: 4px; width: 240px; }
        button, .bouton { background: #3498db; color: #fff; border: 0; padding: 9px 16px; border-radius: 4px;
                           cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        button:hover, .bouton:hover { background: #2980b9; }
        .danger { background: #c0392b; padding: 5px 10px; font-size: 13px; }
        .danger:hover { background: #a93226; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        .ok { background: #e8f8ef; border-left: 4px solid #27ae60; padding: 10px 14px; }
        .erreur { background: #fdecea; border-left: 4px solid #c0392b; padding: 10px 14px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        table { border-collapse: collapse; margin: 10px 0; width: 100%; }
        th, td { border: 1px solid #d5dbe1; padding: 7px 10px; text-align: left; vertical-align: middle; }
        th { background: #2c3e50; color: #fff; }
        tr:nth-child(even) td { background: #f7f9fb; }
        .avatar { display: inline-block; width: 34px; height: 34px; line-height: 34px; text-align: center;
                  border-radius: 50%; background: #3498db; color: #fff; font-weight: bold; margin-right: 8px; }
    </style>
</head>
<body>

<h1><%= esc(titre) %></h1>

<% if (message != null) { %>
    <p class="ok"><%= esc(message) %></p>
<% } %>
<% if (erreur != null) { %>
    <p class="erreur"><%= esc(erreur) %></p>
<% } %>

<p class="note">
    Cette page est affichee par <code>profil(Model, SessionUtilisateur)</code>.
    Le parametre est une <strong>classe Session personnalisee</strong> :
    le framework l'a creee a partir de la HttpSession existante. Les valeurs
    ci-dessous viennent donc de la requete precedente.
</p>

<fieldset>
    <legend>Utilisateur de la session</legend>
    <p>
        <span class="avatar"><%= esc(initiales != null ? initiales : "?") %></span>
        <strong><%= estConnecte ? esc(utilisateur) : "Aucun utilisateur connecte" %></strong>
        <%= Boolean.TRUE.equals(admin) ? "&nbsp;—&nbsp;<em>role admin</em>" : "" %>
    </p>
    <table>
        <tr><th>Role</th><td><%= role == null ? "-" : esc(role) %></td></tr>
        <tr><th>Identifiant de session</th><td><code><%= esc(sessionId) %></code></td></tr>
        <tr><th>Creation</th><td><%= esc(creation) %></td></tr>
        <tr><th>Dernier acces</th><td><%= esc(dernierAcces) %></td></tr>
        <tr><th>Type injecte</th><td><code><%= esc(typeSession) %></code></td></tr>
    </table>
</fieldset>

<fieldset>
    <legend>Toutes les donnees de la session (getDonnees)</legend>
    <% if (attributs == null || attributs.isEmpty()) { %>
        <p>Aucune donnee en session.</p>
    <% } else { %>
        <table>
            <tr><th>Cle</th><th>Valeur</th><th>Type</th><th>Action</th></tr>
            <% for (Map.Entry<String, Object> entry : attributs.entrySet()) {
                   Object valeur = entry.getValue(); %>
                <tr>
                    <td><code><%= esc(entry.getKey()) %></code></td>
                    <td><%= esc(valeur) %></td>
                    <td><%= valeur == null ? "-" : esc(valeur.getClass().getSimpleName()) %></td>
                    <td>
                        <form method="post" action="<%= ctx %>/session/attribut/supprimer">
                            <input type="hidden" name="cle" value="<%= esc(entry.getKey()) %>">
                            <button type="submit" class="danger">Supprimer</button>
                        </form>
                    </td>
                </tr>
            <% } %>
        </table>
    <% } %>
</fieldset>

<fieldset>
    <legend>Ajouter ou modifier une donnee (setAttribute)</legend>
    <form method="post" action="<%= ctx %>/session/attribut">
        <p><label for="cle">Cle</label><input type="text" id="cle" name="cle" placeholder="ex: email"></p>
        <p><label for="valeur">Valeur</label><input type="text" id="valeur" name="valeur" placeholder="ex: midera@test.mg"></p>
        <button type="submit">Enregistrer dans la session</button>
    </form>
    <p style="font-size:13px;color:#666;">
        Une cle deja presente est simplement remplacee : c'est le meme
        <code>session.setAttribute(cle, valeur)</code> pour ajouter et modifier.
    </p>
</fieldset>

<fieldset>
    <legend>Modifier l'utilisateur connecte</legend>
    <form method="post" action="<%= ctx %>/session/modifier">
        <p>
            <label for="nom">Nom</label>
            <input type="text" id="nom" name="nom" value="<%= esc(utilisateur) %>">
        </p>
        <p>
            <label for="role">Role</label>
            <select id="role" name="role">
                <option value="utilisateur" <%= "utilisateur".equals(role) ? "selected" : "" %>>utilisateur</option>
                <option value="admin" <%= "admin".equals(role) ? "selected" : "" %>>admin</option>
            </select>
        </p>
        <button type="submit">Modifier la session</button>
    </form>
</fieldset>

<fieldset>
    <legend>Navigation</legend>
    <p>
        <a class="bouton" href="<%= ctx %>/session/compteur">Compteur de visites</a>
        <a class="bouton" href="<%= ctx %>/admin">Espace protege (Sprint 10)</a>
        <a class="bouton" href="<%= ctx %>/session">Accueil session</a>
    </p>
    <form method="post" action="<%= ctx %>/session/deconnexion" style="display:inline;">
        <button type="submit" class="danger">Se deconnecter (invalidate)</button>
    </form>
</fieldset>

<p><a href="<%= ctx %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
