<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="controller.Employe" %>
<%
    Employe employe = (Employe) request.getAttribute("employe");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Objet lie</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 760px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        table { border-collapse: collapse; margin: 18px 0; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 9px 14px; text-align: left; }
        th { background: #2c3e50; color: #fff; }
        tr:nth-child(even) { background: #f7f9fb; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        .entete { background: #e6f6ec; border-left: 4px solid #1a7f37; padding: 10px 14px; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>Objet Employe lie automatiquement</h1>

<p class="entete">
    Le controleur a recu un objet <code>Employe</code> deja rempli.
    Verifie ci-dessous : ces valeurs viennent des setters,
    pas d'un <code>request.getParameter()</code> dans le controleur.
</p>

<table>
    <tr><th>Attribut</th><th>Type</th><th>Valeur de l'objet</th></tr>
    <tr><td><code>id</code></td><td>int</td><td><code><%= employe.getId() %></code></td></tr>
    <tr><td><code>nom</code></td><td>String</td><td><code><%= employe.getNom() == null ? "(null)" : employe.getNom() %></code></td></tr>
    <tr><td><code>prenom</code></td><td>String</td><td><code><%= employe.getPrenom() == null ? "(null)" : employe.getPrenom() %></code></td></tr>
    <tr><td><code>email</code></td><td>String</td><td><code><%= employe.getEmail() == null ? "(null)" : employe.getEmail() %></code></td></tr>
</table>

<p>Classe de l'objet recu : <code><%= employe.getClass().getName() %></code></p>

<p>
    <a href="<%= request.getContextPath() %>/binding/objet">&larr; Refaire un essai</a> &nbsp;|&nbsp;
    <a href="<%= request.getContextPath() %>/">Accueil</a>
</p>

</body>
</html>
