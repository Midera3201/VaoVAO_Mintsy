<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Enregistrement refuse</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 760px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        .ko { background: #fdecea; border-left: 4px solid #b31d28; padding: 12px 16px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        li { line-height: 1.7; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>Enregistrement refuse</h1>

<p class="ko">
    La methode <code>save(Employe employe, MapFile rectoCIN, MapFile versoCIN)</code>
    a renvoye cette vue parce qu'une condition n'etait pas remplie.
</p>

<p>Cause possible :</p>
<ul>
    <li>le champ <code>nom</code> est vide ;</li>
    <li>un des deux champs <code>rectoCIN</code> / <code>versoCIN</code> est absent ;</li>
    <li>un fichier depasse 2 Mo ;</li>
    <li>un fichier n'est pas de type <code>image/jpeg</code>, <code>image/png</code> ou <code>application/pdf</code>.</li>
</ul>

<p>
    Pour un diagnostic precis (nom exact du champ, taille, type MIME), utilise la
    <a href="<%= request.getContextPath() %>/upload">version detaillee du formulaire</a>,
    qui affiche un message par cas d'erreur.
</p>

<p><a href="<%= request.getContextPath() %>/">&larr; Accueil</a></p>

</body>
</html>
