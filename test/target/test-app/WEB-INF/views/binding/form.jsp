<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>${titre}</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 760px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        fieldset { border: 1px solid #ddd; border-radius: 6px; margin: 20px 0; padding: 16px 20px; }
        legend { font-weight: bold; color: #34495e; padding: 0 8px; }
        label { display: inline-block; width: 160px; }
        input[type=text], input[type=number] { padding: 7px; border: 1px solid #ccc; border-radius: 4px; width: 220px; }
        button { background: #3498db; color: #fff; border: 0; padding: 10px 18px; border-radius: 4px;
                 cursor: pointer; font-size: 15px; margin-top: 10px; }
        button:hover { background: #2980b9; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>${titre}</h1>

<p class="note">
    Le formulaire envoie <code>nom</code>, <code>age</code>, <code>taille</code> et <code>actif</code>.
    Le controleur recoit <code>resultat(String nom, int age, double taille, boolean actif)</code>
    sans jamais appeler <code>request.getParameter()</code>.
</p>

<fieldset>
    <legend>GET — query string</legend>
    <form method="get" action="<%= request.getContextPath() %>/binding/resultat">
        <p><label for="nom">Nom</label><input type="text" id="nom" name="nom" value="Rakoto"></p>
        <p><label for="age">Age (int)</label><input type="text" id="age" name="age" value="25"></p>
        <p><label for="taille">Taille (double)</label><input type="text" id="taille" name="taille" value="1.75"></p>
        <p><label for="actif">Actif (boolean)</label><input type="checkbox" id="actif" name="actif" value="true" checked></p>
        <button type="submit">Envoyer en GET</button>
    </form>
</fieldset>

<fieldset>
    <legend>POST — corps du formulaire</legend>
    <form method="post" action="<%= request.getContextPath() %>/binding/resultat">
        <p><label for="nom2">Nom</label><input type="text" id="nom2" name="nom" value="Rabe"></p>
        <p><label for="age2">Age (int)</label><input type="text" id="age2" name="age" value="30"></p>
        <button type="submit">Envoyer en POST</button>
    </form>
</fieldset>

<fieldset>
    <legend>Cas d'erreur (valeur non convertible)</legend>
    <p><a href="<%= request.getContextPath() %>/binding/resultat?nom=Test&age=abc&taille=xyz">age=abc et taille=xyz</a>
       — le framework ne plante pas, il met 0 (valeur par defaut).</p>
    <p><a href="<%= request.getContextPath() %>/binding/resultat?nom=Test">Parametres absents</a>
       — age, taille et actif prennent leur valeur par defaut.</p>
</fieldset>

<p><a href="<%= request.getContextPath() %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
