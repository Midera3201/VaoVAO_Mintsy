<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="controller.Employe" %>
<%
    String erreur = (String) request.getAttribute("erreur");
    Employe employe = (Employe) request.getAttribute("employe");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>${titre}</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 780px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        fieldset { border: 1px solid #ddd; border-radius: 6px; margin: 20px 0; padding: 16px 20px; }
        legend { font-weight: bold; color: #34495e; padding: 0 8px; }
        label { display: inline-block; width: 160px; }
        input[type=text], input[type=file] { padding: 7px; border: 1px solid #ccc; border-radius: 4px; width: 280px; }
        button { background: #3498db; color: #fff; border: 0; padding: 10px 18px; border-radius: 4px;
                 cursor: pointer; font-size: 15px; margin-top: 10px; }
        button:hover { background: #2980b9; }
        .note { background: #eef6ff; border-left: 4px solid #3498db; padding: 10px 14px; font-size: 14px; }
        .erreur { background: #fdecea; border-left: 4px solid #b31d28; color: #611a15; padding: 10px 14px;
                  font-size: 14px; margin-bottom: 18px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>${titre}</h1>

<% if (erreur != null) { %>
    <p class="erreur"><strong>Erreur :</strong> <%= erreur %></p>
<% } %>

<p class="note">
    Le formulaire envoie du <code>multipart/form-data</code>.
    Le framework lit les champs texte pour remplir <code>Employe</code> (Sprint 7 bis)
    et construit un <code>MapFile</code> par <code>&lt;input type="file"&gt;</code> (Sprint 8).
    Le controleur recoit
    <code>save(Model, HttpServletRequest, Employe, MapFile rectoCIN, MapFile versoCIN)</code>
    sans jamais appeler <code>request.getPart()</code>.
</p>

<fieldset>
    <legend>Envoi d'un employe et de sa CIN (POST multipart)</legend>

    <!-- enctype est INDISPENSABLE : sans lui, getPart() ne voit aucun fichier -->
    <form method="post"
          action="<%= request.getContextPath() %>/upload/save"
          enctype="multipart/form-data">

        <p><label for="id">id</label>
           <input type="text" id="id" name="id"
                  value="<%= employe != null ? String.valueOf(employe.getId()) : "" %>"></p>

        <p><label for="nom">nom</label>
           <input type="text" id="nom" name="nom"
                  value="<%= employe != null && employe.getNom() != null ? employe.getNom() : "" %>"></p>

        <p><label for="prenom">prenom</label>
           <input type="text" id="prenom" name="prenom"
                  value="<%= employe != null && employe.getPrenom() != null ? employe.getPrenom() : "" %>"></p>

        <p><label for="email">email</label>
           <input type="text" id="email" name="email"
                  value="<%= employe != null && employe.getEmail() != null ? employe.getEmail() : "" %>"></p>

        <p><label for="rectoCIN">rectoCIN (fichier)</label>
           <input type="file" id="rectoCIN" name="rectoCIN" accept="image/jpeg,image/png,application/pdf"></p>

        <p><label for="versoCIN">versoCIN (fichier)</label>
           <input type="file" id="versoCIN" name="versoCIN" accept="image/jpeg,image/png,application/pdf"></p>

        <button type="submit">Enregistrer</button>
    </form>
</fieldset>

<fieldset>
    <legend>Variante : signature exacte du sprint (POST multipart)</legend>

    <p class="note">
        Meme envoi, mais vers
        <code>save(Employe employe, MapFile rectoCIN, MapFile versoCIN)</code> :
        ni <code>Model</code> ni <code>HttpServletRequest</code>.
        La vue retournee (<code>upload/success</code> ou <code>upload/echec</code>)
        prouve que les trois arguments ont bien ete remplis par le framework.
    </p>

    <form method="post"
          action="<%= request.getContextPath() %>/upload/simple"
          enctype="multipart/form-data">

        <p><label for="s_nom">nom</label>
           <input type="text" id="s_nom" name="nom" value="Rakoto"></p>
        <p><label for="s_prenom">prenom</label>
           <input type="text" id="s_prenom" name="prenom" value="Jean"></p>
        <p><label for="s_email">email</label>
           <input type="text" id="s_email" name="email" value="jean@test.mg"></p>
        <p><label for="s_recto">rectoCIN</label>
           <input type="file" id="s_recto" name="rectoCIN" accept="image/jpeg,image/png,application/pdf"></p>
        <p><label for="s_verso">versoCIN</label>
           <input type="file" id="s_verso" name="versoCIN" accept="image/jpeg,image/png,application/pdf"></p>

        <button type="submit">Envoyer (signature exacte)</button>
    </form>
</fieldset>

<fieldset>
    <legend>Regles appliquees par le controleur</legend>
    <ul>
        <li>taille maximale : 2 Mo par fichier ;</li>
        <li>types acceptes : <code>image/jpeg</code>, <code>image/png</code>, <code>application/pdf</code> ;</li>
        <li>nom de fichier nettoye (aucun chemin, aucune traversee de repertoire) ;</li>
        <li>stockage dans <code>/WEB-INF/upload</code>, non accessible depuis le navigateur.</li>
    </ul>
</fieldset>

<fieldset>
    <legend>Erreurs a tester</legend>
    <ul>
        <li>cliquer sur <strong>Enregistrer</strong> sans choisir de fichier &rarr; « Aucun fichier recu » ;</li>
        <li>vider le champ <strong>nom</strong> &rarr; « Le champ nom est obligatoire » ;</li>
        <li>choisir un fichier &gt; 2 Mo &rarr; message de taille ;</li>
        <li>choisir un <code>.txt</code> &rarr; message de type refuse ;</li>
        <li>fichier &gt; 10 Mo &rarr; refuse par le conteneur (max-file-size du framework).</li>
    </ul>
</fieldset>

<p><a href="<%= request.getContextPath() %>/">&larr; Retour a l'accueil</a></p>

</body>
</html>
