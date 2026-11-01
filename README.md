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

## Sprint 11 : instance unique des controleurs

Avant le Sprint 11, `FrontControllerServlet` creait une nouvelle instance du
controleur a chaque requete. Desormais, chaque classe annotee `@controller`
est instanciee **une seule fois**, au demarrage de l'application, puis la meme
instance est reutilisee pour toutes les requetes et toutes les methodes de ce
controleur.

```java
@controller
public class EtudiantController {

    // Une seule instance pour toute l'application
    public EtudiantController() {
        System.out.println("EtudiantController cree");
    }

    @UrlMapping(url = "/etudiants", method = "GET")
    public String liste(Model model) {
        // ...
        return "etudiant/liste";
    }

    @UrlMapping(url = "/etudiants/detail", method = "GET")
    public String detail(Model model) {
        // ...
        return "etudiant/detail";
    }
}
```

`liste()` et `detail()` utilisent le meme objet `EtudiantController`.

### Fonctionnement

1. `ApplicationListener` scanne les classes `@controller` et leurs routes.
2. `ControllerRegistry` cree une instance par classe, sans doublon.
3. Les instances sont stockees dans le `ServletContext`, sous l'attribut
   `controllerInstances` (`Map<Class<?>, Object>`).
4. A chaque requete, `FrontControllerServlet` lit l'instance deja existante
   dans ce registre. Il n'appelle plus `newInstance()`.

Si le registre n'est pas encore disponible, `ControllerRegistry` cree
l'instance a la demande de maniere thread-safe. Une seule instance est
conservee par classe.

### Important : controleurs partages

Comme une instance est partagee par toutes les requetes concurrentes, un
controleur doit rester **sans etat mutable** :

```java
// A eviter : champ modifie par les requetes
private String dernierNom;

// A preferer : donnees locales ou injectees
public String action(String nom, Model model) {
    model.setAttribute("nom", nom);
    return "resultat";
}
```

Pour un etat partage volontaire, utiliser des classes thread-safe
(`AtomicInteger`, `ConcurrentHashMap`...) ou une synchronisation adaptee.

### Demonstration

L'application de test expose :

- `GET /api/singleton` : affiche `instanceId`, `nombreInstances`,
  `nombreAppels` et la route appelee ;
- `GET /api/singleton/autre` : seconde methode du meme controleur.

Appeler plusieurs fois ces deux URLs renvoie toujours le meme `instanceId`,
avec `nombreInstances = 1` et un `nombreAppels` qui augmente.

