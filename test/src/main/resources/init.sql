INSERT INTO employes (nom, prenom, email)
SELECT 'Rakoto', 'Jean', 'jean.rakoto@email.com'
WHERE NOT EXISTS (SELECT 1 FROM employes WHERE nom = 'Rakoto' AND prenom = 'Jean');

INSERT INTO employes (nom, prenom, email)
SELECT 'Rabe', 'Marie', 'marie.rabe@email.com'
WHERE NOT EXISTS (SELECT 1 FROM employes WHERE nom = 'Rabe' AND prenom = 'Marie');

INSERT INTO employes (nom, prenom, email)
SELECT 'Randria', 'Paul', 'paul.randria@email.com'
WHERE NOT EXISTS (SELECT 1 FROM employes WHERE nom = 'Randria' AND prenom = 'Paul');

INSERT INTO employes (nom, prenom, email)
SELECT 'aaaa', 'uiu', 'aaaaa@email.com'
WHERE NOT EXISTS (SELECT 1 FROM employes WHERE nom = 'aaaa' AND prenom = 'uiu');