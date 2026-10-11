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
        label { display: inline-block; width: 140px; }
        input[type=text] { padding: 7px; border: 1px solid #ccc; border-radius: 4px; width: 240px; }
        button { background: #3498db; color: #fff; border: 0; padding: 10px 18px; border-radius: 4px;
                 cursor: pointer; font-size: 15px; margin-top: 10px; }
        button:hover { background: #2980b9; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>${titre}</h1>

<p class="note">
    Le controleur recoit <code>enregistrerObjet(Model model, Employe employe)</code>.
    Il n'y a <strong>aucun</strong> <code>request.getParameter()</code> : le framework
    cree l'objet <code>Employe</code>, lit les champs ci-dessous et appelle les setters.
</p>

<fieldset>
    <legend>Formulaire objet (POST)</legend>
    <form method="post" action="<%= request.getContextPath() %>/binding/objet">
        <p><label for="id">id (int)</label><input type="text" id="id" name="id" value="7"></p>
        <p><label for="nom">nom (String)</label><input type="text" id="nom" name="nom" value="Midera"></p>
        <p><label for="prenom">prenom (String)</label><input type="text" id="prenom" name="prenom" value="Naivo"></p>
        <p><label for="email">email (String)</label><input type="text" id="email" name="email" value="midera@test.mg"></p>
        <button type="submit">Envoyer l'objet</button>
    </form>
</fieldset>

<fieldset>
    <legend>Variante avec parametres prefixes</legend>
    <form method="post" action="<%= request.getContextPath() %>/binding/objet">
        <p><label for="p_id">employe.id</label><input type="text" id="p_id" name="employe.id" value="9"></p>
        <p><label for="p_nom">employe.nom</label><input type="text" id="p_nom" name="employe.nom" value="Prefixe"></p>
        <p><label for="p_prenom">employe.prenom</label><input type="text" id="p_prenom" name="employe.prenom" value="Test"></p>
        <p><label for="p_email">employe.email</label><input type="text" id="p_email" name="employe.email" value="p@test.mg"></p>
        <button type="submit">Envoyer avec prefixe</button>
    </form>
</fieldset>

<fieldset>
    <legend>Cas d'erreur</legend>
    <p>
        <a href="#" onclick="document.getElementById('id').value='abc';document.forms[1].submit();return false;">
            id=abc (non convertible)
        </a>
        — le framework ne plante pas, l'attribut garde 0.
    </p>
    <form method="post" action="<%= request.getContextPath() %>/binding/objet">
        <p><label for="nomvide">nom vide</label><input type="text" id="nomvide" name="nom" value=""></p>
        <button type="submit">Envoyer un nom vide</button>
    </form>
</fieldset>

<p><a href="<%= request.getContextPath() %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
