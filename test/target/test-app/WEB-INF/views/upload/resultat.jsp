<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="controller.Employe" %>
<%@ page import="com.framework.util.MapFile" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.List" %>
<%!
    /** Description lisible d'un MapFile recu (ou absence de fichier). */
    private String texte(MapFile f) {
        if (f == null || f.isEmpty()) {
            return "(aucun fichier)";
        }
        return f.getFilename() + " - " + f.getContentType() + " - " + f.getSize() + " octets";
    }
%>
<%
    Employe employe = (Employe) request.getAttribute("employe");
    MapFile recto = (MapFile) request.getAttribute("rectoCIN");
    MapFile verso = (MapFile) request.getAttribute("versoCIN");
    String dossier = (String) request.getAttribute("dossier");
    List<File> ecrits = (List<File>) request.getAttribute("fichiersEcrits");
    List<String> infos = (List<String>) request.getAttribute("infos");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Fichiers recus</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; max-width: 820px; margin: 40px auto; padding: 0 20px; }
        h1 { color: #2c3e50; }
        table { border-collapse: collapse; margin: 18px 0; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 9px 14px; text-align: left; vertical-align: top; }
        th { background: #2c3e50; color: #fff; }
        tr:nth-child(even) { background: #f7f9fb; }
        code { background: #eef2f7; padding: 2px 6px; border-radius: 3px; word-break: break-all; }
        .entete { background: #e6f6ec; border-left: 4px solid #1a7f37; padding: 10px 14px; }
        .vide { color: #b5541c; font-style: italic; }
        ul { line-height: 1.7; }
        a { color: #3498db; }
    </style>
</head>
<body>

<h1>Fichiers recus par le controleur</h1>

<p class="entete">
    La methode <code>save(...)</code> a recu les arguments deja construits par le framework :
    un <code>Employe</code> rempli par les champs texte, et deux <code>MapFile</code>
    remplis depuis les <code>&lt;input type="file"&gt;</code>.
</p>

<h2>1. Employe (binding d'objet, Sprint 7 bis)</h2>
<table>
    <tr><th>Attribut</th><th>Type</th><th>Valeur</th></tr>
    <tr><td><code>id</code></td><td>int</td><td><%= employe.getId() %></td></tr>
    <tr><td><code>nom</code></td><td>String</td><td><%= employe.getNom() %></td></tr>
    <tr><td><code>prenom</code></td><td>String</td><td><%= employe.getPrenom() == null ? "(null)" : employe.getPrenom() %></td></tr>
    <tr><td><code>email</code></td><td>String</td><td><%= employe.getEmail() == null ? "(null)" : employe.getEmail() %></td></tr>
</table>

<h2>2. Fichiers (MapFile, Sprint 8)</h2>
<table>
    <tr><th>Parametre Java</th><th>Champ HTML</th><th>Fichier recu</th></tr>
    <tr>
        <td><code>MapFile rectoCIN</code></td>
        <td><code>rectoCIN</code></td>
        <td><%= texte(recto) %></td>
    </tr>
    <tr>
        <td><code>MapFile versoCIN</code></td>
        <td><code>versoCIN</code></td>
        <td><%= texte(verso) %></td>
    </tr>
</table>

<h2>3. Stockage sur le serveur</h2>
<p>Dossier d'ecriture : <code><%= dossier %></code></p>
<ul>
    <%
        if (infos != null) {
            for (String info : infos) {
    %>
        <li><%= info %></li>
    <%
            }
        }
    %>
</ul>

<%
    if (ecrits == null || ecrits.isEmpty()) {
%>
    <p class="vide">Aucun fichier n'a ete ecrit sur le disque.</p>
<%
    } else {
%>
    <table>
        <tr><th>Fichier ecrit</th><th>Taille sur disque</th></tr>
        <%
            for (File f : ecrits) {
        %>
        <tr>
            <td><code><%= f.getAbsolutePath() %></code></td>
            <td><%= f.length() %> octets</td>
        </tr>
        <%
            }
        %>
    </table>
<%
    }
%>

<p>
    <a href="<%= request.getContextPath() %>/upload">&larr; Refaire un essai</a> &nbsp;|&nbsp;
    <a href="<%= request.getContextPath() %>/">Accueil</a>
</p>

</body>
</html>
