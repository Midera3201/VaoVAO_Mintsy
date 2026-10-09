<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Test Web API</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 900px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        h2 { color: #34495e; border-bottom: 1px solid #ddd; padding-bottom: 6px; margin-top: 32px; }
        button { background: #3498db; color: #fff; border: 0; padding: 9px 16px; margin: 4px 6px 4px 0;
                 border-radius: 4px; cursor: pointer; font-size: 14px; }
        button:hover { background: #2980b9; }
        input { padding: 8px; margin: 4px 6px 4px 0; border: 1px solid #ccc; border-radius: 4px; }
        pre { background: #1e1e1e; color: #9cdcfe; padding: 14px; border-radius: 6px;
              overflow-x: auto; white-space: pre-wrap; word-break: break-all; min-height: 40px; }
        .retour { font-weight: bold; color: #27ae60; }
        .note { background: #fff8e1; border-left: 4px solid #f39c12; padding: 10px 14px; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>Test de la Web API</h1>

<p class="note">
    Une requete <strong>POST ne peut pas etre envoyee depuis la barre d'adresse</strong> :
    le navigateur envoie toujours un GET quand on saisit une URL. Les boutons ci-dessous
    utilisent <code>fetch()</code> pour envoyer la bonne methode HTTP.
</p>

<p><a href="<%= request.getContextPath() %>/">&larr; Retour a l'accueil</a></p>

<h2>GET /api/employes</h2>
<button onclick="appeler('GET', 'api/employes')">Lister les employes</button>

<h2>GET /api/stats</h2>
<button onclick="appeler('GET', 'api/stats')">Statistiques</button>

<h2>POST /api/employes</h2>
<div>
    <input id="nom" placeholder="nom" value="Rakoto">
    <input id="prenom" placeholder="prenom" value="Jean">
    <input id="email" placeholder="email" value="jean@email.com">
</div>
<br>
<button onclick="creerEmploye()">Creer un employe</button>

<h2>POST /api/employes/supprimer</h2>
<input id="idSuppr" placeholder="id a supprimer" value="1">
<button onclick="supprimerEmploye()">Supprimer</button>

<h2>Test d'erreur : GET /api/inconnu</h2>
<button onclick="appeler('GET', 'api/inconnu')">Doit renvoyer 404 en JSON</button>

<h2>Reponse</h2>
<div class="retour" id="statut"></div>
<pre id="reponse">Clique sur un bouton...</pre>

<script>
    function afficherStatut(texte) {
        document.getElementById('statut').textContent = texte;
    }

    async function appeler(methode, chemin, corps) {
        afficherStatut('Envoi de la requete...');
        document.getElementById('reponse').textContent = '';
        try {
            const options = { method: methode };
            if (corps) {
                options.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
                options.body = corps;
            }
            const reponse = await fetch('<%= request.getContextPath() %>/' + chemin, options);
            const texte = await reponse.text();
            afficherStatut('HTTP ' + reponse.status + '  |  Content-Type: '
                          + reponse.headers.get('Content-Type'));
            try {
                document.getElementById('reponse').textContent = JSON.stringify(JSON.parse(texte), null, 2);
            } catch (e) {
                document.getElementById('reponse').textContent = texte;
            }
        } catch (e) {
            afficherStatut('Erreur reseau');
            document.getElementById('reponse').textContent = e;
        }
    }

    function creerEmploye() {
        const corps = 'nom=' + encodeURIComponent(document.getElementById('nom').value)
                    + '&prenom=' + encodeURIComponent(document.getElementById('prenom').value)
                    + '&email=' + encodeURIComponent(document.getElementById('email').value);
        appeler('POST', 'api/employes', corps);
    }

    function supprimerEmploye() {
        const corps = 'id=' + encodeURIComponent(document.getElementById('idSuppr').value);
        appeler('POST', 'api/employes/supprimer', corps);
    }
</script>

</body>
</html>