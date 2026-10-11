<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Resultat du binding</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 760px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        table { border-collapse: collapse; margin: 18px 0; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 9px 14px; text-align: left; }
        th { background: #2c3e50; color: #fff; }
        tr:nth-child(even) { background: #f7f9fb; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        .ok { color: #1a7f37; font-weight: bold; }
        .defaut { color: #b5541c; font-weight: bold; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>Resultat du binding</h1>

<p>Valeurs recues par la methode du controleur :</p>

<table>
    <tr><th>Parametre</th><th>Type Java</th><th>Valeur recue</th><th>Origine</th></tr>
    <tr>
        <td><code>nom</code></td><td>String</td>
        <td><code><%= request.getAttribute("nom") %></code></td>
        <td class="ok">valeur du formulaire</td>
    </tr>
    <tr>
        <td><code>age</code></td><td>int</td>
        <td><code><%= request.getAttribute("age") %></code></td>
        <td><% if (Boolean.TRUE.equals(request.getAttribute("ageAbsent"))) { %>
                <span class="defaut">valeur par defaut (absent ou non convertible)</span>
            <% } else { %><span class="ok">converti en int</span><% } %>
        </td>
    </tr>
    <tr>
        <td><code>taille</code></td><td>double</td>
        <td><code><%= request.getAttribute("taille") %></code></td>
        <td class="ok">converti en double</td>
    </tr>
    <tr>
        <td><code>actif</code></td><td>boolean</td>
        <td><code><%= request.getAttribute("actif") %></code></td>
        <td class="ok">converti en boolean</td>
    </tr>
</table>

<p>Aucun <code>request.getParameter()</code> dans le controleur : les arguments
   <code>(String nom, int age, double taille, boolean actif)</code> ont ete remplis
   automatiquement par <code>ParamBinder</code>.</p>

<p>
    <a href="<%= request.getContextPath() %>/binding">&larr; Refaire un essai</a> &nbsp;|&nbsp;
    <a href="<%= request.getContextPath() %>/">Accueil</a>
</p>

</body>
</html>
