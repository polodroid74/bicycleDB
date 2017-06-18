1)MISE EN PLACE DE LA BASE DE DONNÉES

Pour mettre en place la base de données, se connecter à oracle (depuis le dossier racine du projet)
et lancer les scripts sql du dossier "ScriptsSQL" avec la syntaxe @{ScriptsSQL/"nomduscript"}
 - Pour créer la base de données : creationbdd.sql
 - Pour supprimer la base de données : suppressionbdd.sql
 - Pour insérer les données de test dans la base de données : PeuplageTables.sql
 - Pour afficher toutes les relations de la base de données : affichagebdd.sql


2)FONCTIONNALITES

NOS FONCTIONNALITES SE BASENT SUR UN MAKEFILE

!!!!Pensez à modifier les ID de connections à oracle pour se connecter sur la bonne base !!!!!!

*** FONCTIONNALITE "FACTURATION" ***

Lancer la commande "make exeFacturation" dans le dossier racine du projet. Se laisser guider par les indications du terminal.



*** FONCTIONNALITE "TEMPS D'UTILISATION MOYEN PAR VEHICULE PAR MOIS" ***

Dans le dossier racine du projet, exécuter la ligne de commande "make exeTMVehiculeMois"

Il est possible de modifier l'affichage. Pour cela, ouvrir le fichier src/TMVehiculeMois.java, se rendre à la ligne indiquée ci-dessous et décommenter les lignes mentionnées dans le fichier.

Ligne 33 : afficher la liste des locations terminées
Ligne 106 : afficher la table de hachage (cf. compte-rendu et commentaire ligne 55)
Ligne 151 : afficher le résultat en jours, heures ou minutes



*** FONCTIONNALITE "TEMPS D'UTILISATION MOYEN PAR CATEGORIE DE VEHICULE PAR MOIS" ***

Lancer la commande "make exeTMCategorieMois" dans le dossier racine du projet. Se laisser guider par les indications du terminal.



*** FONCTIONNALITE "TAUX D'OCCUPATION DES STATIONS SUR LA JOURNÉE" ***

Dans le dossier racine du projet, exécuter la ligne de commande "make exeTauxStation". Se laisser guider par les indications du terminal.



*** FONCTIONNALITE "CATEGORIE DE VEHICULE LA PLUS UTILISEE PAR TRANCHE D'AGE DE 10 ANS" ***

Dans le dossier racine du projet, exécuter la ligne de commande "make exeCategorieUtilisee". Se laisser guider par les indications du terminal, il est possible de renseigner une tranche d'âge spécifique en entrant "o" lorsque proposé, sinon le programme s'éxecutera pour toutes les tranches d'âge d'abonnés.



3)INSERTION DANS LA BASE D'UN NOUVEAU TUPLE

Pour éxecuter le script d'insertion, éxecuter la ligne de commande "make exeInsertionDonnees". Se laisser guider par les indications du terminal.
