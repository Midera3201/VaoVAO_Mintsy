# VaoVAO_Mintsy

Framework web Java (Jakarta Servlet) avec un `FrontControllerServlet`, des
annotations de routage (`@controller`, `@UrlMapping`, `@ApiJson`), le binding
automatique des parametres et la generation de vues JSP.

## Compilation

```bash
cd framework
mvn clean install

cd ../test
mvn clean package
```

Le fichier `test/target/test-app.war` est ensuite deploye dans Tomcat.

## Sprint 9 : gestion des sessions

Le framework fournit `com.framework.util.Session`, une enveloppe de
`HttpSession`. Une methode de controleur qui declare un parametre `Session`
le recoit automatiquement : aucun `request.getSession()` n'est necessaire.

```java
import com.framework.util.Session;
import com.framework.util.Model;

@UrlMapping(url = "/profil", method = "GET")
public String profil(Model model, Session session) {
    // Lecture
    String nom = session.getString("nom");

    // Ajout / modification
    session.setAttribute("nom", "Midera");
    session.setAttribute("role", "admin");

    // Suppression
    session.removeAttribute("temporaire");

    model.setAttribute("nom", nom);
    return "profil";
}
```

### API principale

| Methode | Role |
|---|---|
| `setAttribute(nom, valeur)` | Ajoute ou remplace une donnee |
| `getAttribute(nom)` | Lit une donnee (ou `null`) |
| `getAttribute(nom, Type.class)` | Lecture typee, sans `ClassCastException` |
| `getString(nom)`, `getInt(nom, defaut)`, `getBoolean(nom, defaut)` | Lectures converties |
| `hasAttribute(nom)` / `contient(nom)` | Verifie la presence |
| `removeAttribute(nom)` | Supprime une donnee |
| `getDonnees()` | Copie de toutes les donnees |
| `clear()` / `vider()` | Supprime toutes les donnees |
| `invalidate()` / `detruire()` | Detruit la session |
| `getId()` | Identifiant de session (cookie JSESSIONID) |
| `estNouvelle()`, `getDateCreation()`, `getDateDernierAcces()` | Informations de session |
| `getDureeInactivite()` / `setDureeInactivite(secondes)` | Expiration |
| `connecter(nom, role)`, `deconnecter()` | Raccourcis utilisateur connecte |
| `getUtilisateur()`, `getRole()`, `estConnecte()`, `estAdmin()` | Informations utilisateur |

Les donnees sont stockees dans la `HttpSession` du conteneur : elles sont
conservees d'une requete a l'autre pour un meme navigateur, jusqu'a expiration
de la session (voir `<session-config>` dans `web.xml`) ou appel de
`invalidate()`.

### Classe Session personnalisee

Une application peut etendre `Session`. La sous-classe doit exposer un
constructeur public acceptant `HttpSession` ; le framework l'instancie
automatiquement.

```java
public class SessionUtilisateur extends Session {
    public SessionUtilisateur(HttpSession httpSession) {
        super(httpSession);
    }

    public boolean estAdministrateur() {
        return estAdmin();
    }
}

// Dans un controleur :
public String profil(SessionUtilisateur session) {
    return session.estAdministrateur() ? "admin/profil" : "profil";
}
```

### Demonstration

L'application de test expose :

- `GET  /session` : formulaire de connexion et etat de la session ;
- `POST /session/connexion` : ajoute l'utilisateur et son role ;
- `GET  /session/profil` : relecture dans une nouvelle requete, avec une
  `SessionUtilisateur` personnalisee ;
- `POST /session/modifier` : modification des donnees ;
- `POST /session/attribut` : ajout/modification d'une donnee libre ;
- `POST /session/attribut/supprimer` : suppression d'une donnee ;
- `GET  /session/compteur` : compteur conserve entre les requetes ;
- `POST /session/deconnexion` : `invalidate()` ;
- `GET  /api/session` : etat de la session en JSON.

## Sprint 10 : verification de l'authentification

L'annotation `@AuthentificationRequise` protege une methode d'action. Le
front controller verifie l'authentification **avant** d'instancier le
controleur ou d'invoquer la methode.

```java
import com.framework.annotation.AuthentificationRequise;
import com.framework.util.Model;
import com.framework.util.Session;

@AuthentificationRequise
@UrlMapping(url = "/admin", method = "GET")
public String dashboard(Model model, Session session) {
    return "admin/dashboard";
}
```

### Configuration

Dans `web.xml` :

```xml
<context-param>
    <param-name>auth.sessionAttribute</param-name>
    <param-value>utilisateur</param-value>
</context-param>

<!-- Optionnel : redirection au lieu d'une erreur 401 -->
<context-param>
    <param-name>auth.loginUrl</param-name>
    <param-value>/session</param-value>
</context-param>
```

- `auth.sessionAttribute` : nom de la variable de session qui indique qu'un
  utilisateur est authentifie. Par defaut : `utilisateur`.
- `auth.loginUrl` : page de connexion. Si elle est configuree, une requete HTML
  refusee est redirigee vers cette page ; sinon le framework renvoie `401`.

Une methode peut aussi utiliser un autre nom d'attribut :

```java
@AuthentificationRequise(attribut = "membre")
```

### Valeurs considerees comme authentifiees

| Valeur en session | Resultat |
|---|---|
| attribut absent ou `null` | refuse |
| chaine vide ou composee d'espaces | refuse |
| `false` | refuse |
| `true` | autorise |
| chaine non vide (`"Midera"`, id utilisateur...) | autorise |

### Reponse en cas de refus

- Methode `@ApiJson` : statut **401** et JSON
  `{"erreur":"Authentification requise..."}`.
- Methode HTML avec `auth.loginUrl` : redirection **302** vers la page de
  connexion.
- Methode HTML sans `auth.loginUrl` : erreur HTTP **401**.

### Demonstration

- `GET  /admin` : page protegee, redirige vers `/session` si non connecte ;
- `POST /admin/action` : action protegee avec compteur d'executions ;
- `GET  /api/admin` : API protegee, renvoie 401 en JSON si non connecte.

Toute personne authentifiee peut ouvrir `/admin` : ce sprint controle
l'authentification, pas les droits lies au role. Le role est seulement affiche
a titre informatif.

Scenario de test : ouvrir `/admin` sans session (refus), se connecter via
`/session`, puis rouvrir `/admin` (acces autorise).

