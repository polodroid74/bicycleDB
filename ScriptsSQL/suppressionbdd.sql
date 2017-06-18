/*
Erreurs si les tables n'existent pas mais suppression quand même de celles existantes
Ne marche pas :
IF OBJECT_ID('Abonnes') IS NOT NULL
*/

DROP TABLE Location;
DROP TABLE AffectationForfait;
DROP TABLE ForfaitIllimite;
DROP TABLE ForfaitLimite;
DROP TABLE Forfait;
DROP TABLE PeutContenir;
DROP TABLE EstDans;
DROP TABLE Vehicule;
DROP TABLE Categorie;
DROP TABLE Station;
DROP TABLE Abonnes;

commit;
