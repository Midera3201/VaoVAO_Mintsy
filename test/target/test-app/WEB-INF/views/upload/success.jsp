<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Enregistrement reussi</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 760px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        .ok { background: #e6f6ec; border-left: 4px solid #1a7f37; padding: 12px 16px; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>Enregistrement reussi</h1>

<p class="ok">
    Cette page a ete retournee par
    <code>save(Employe employe, MapFile rectoCIN, MapFile versoCIN)</code>,
    la signature <strong>exacte</strong> du sprint, sans <code>Model</code>
    ni <code>HttpServletRequest</code>.
    Le framework a rempli l'<code>Employe</code> et construit les deux
    <code>MapFile</code> : les deux fichiers etaient presents et valides,
    ils ont donc ete enregistres dans <code>${catalina.base}/upload-data</code>,
    en dehors de l'application web.
</p>

<p>
    <a href="<%= request.getContextPath() %>/upload">&larr; Refaire un essai</a> &nbsp;|&nbsp;
    <a href="<%= request.getContextPath() %>/">Accueil</a>
</p>

</body>
</html>
