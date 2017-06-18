set autocommit off;

--Abonnes
INSERT INTO Abonnes VALUES (1, 'Martin', 'Claude', TO_DATE('1950-05-25', 'yyyy-mm-dd'), '12 rue du ruisseau 33000 Bordeaux');
INSERT INTO Abonnes VALUES (2, 'Bati', 'Mégane', TO_DATE('1995-07-12', 'yyyy-mm-dd'), '13 rue du bois taillis 38610 Gieres');
INSERT INTO Abonnes VALUES (3, 'Donzeau', 'Anais', TO_DATE('1996-01-08', 'yyyy-mm-dd'), 'Perpignan');
INSERT INTO Abonnes VALUES (4, 'Bélot', 'Matthieu', TO_DATE('1995-07-24', 'yyyy-mm-dd'), 'Dans le froid du Nord');
INSERT INTO Abonnes VALUES (5, 'Thovex', 'Candide', TO_DATE('1912-06-10', 'yyyy-mm-dd'), 'La Meije');
INSERT INTO Abonnes VALUES (6, 'Shinshui', 'Mao', TO_DATE('1910-03-16', 'yyyy-mm-dd'), 'Sur la grande muraille');
INSERT INTO Abonnes VALUES (7, 'Coptere', 'Elie', TO_DATE('1892-12-12', 'yyyy-mm-dd'), '4L Rue de laltiport');


--Station
INSERT INTO Station VALUES('Lavilette', '3 rue Lavilette 38000 Grenoble');
INSERT INTO Station VALUES('Latablette', '10bis rue du chocolat 38000 Grenoble');
INSERT INTO Station VALUES('VH', '9 avenue des misérables 38100 Grenoble');
INSERT INTO Station VALUES('Vaujany', 'Centre Ville 38114 Vaujany');
INSERT INTO Station VALUES('Bergers', '6 Allee des bergers 38750 Alpe Huez');


--Categorie
INSERT INTO Categorie VALUES('Voiture Electrique', 14, 15.99, 190000);
INSERT INTO Categorie VALUES('Velo', 5, 4.99, 99);
INSERT INTO Categorie VALUES('Velo Avec Remorque', 70, 8.99, 4500);
INSERT INTO Categorie VALUES('Velo Electrique', 365, 9.99, 7500);
INSERT INTO Categorie VALUES('Petit Utilitaire', 366, 18.99, 20000);


--Vehicule
INSERT INTO Vehicule VALUES(001, 1, 'Velo');
INSERT INTO Vehicule VALUES(002, 2, 'Voiture Electrique');
INSERT INTO Vehicule VALUES(003, 3, 'Velo Avec Remorque');
INSERT INTO Vehicule VALUES(004, 2, 'Velo Electrique');
INSERT INTO Vehicule VALUES(005, 5, 'Petit Utilitaire');
INSERT INTO Vehicule VALUES(006, 5, 'Petit Utilitaire');
INSERT INTO Vehicule VALUES(007, 1, 'Velo');
INSERT INTO Vehicule VALUES(008, 4, 'Petit Utilitaire');
INSERT INTO Vehicule VALUES(009, 6, 'Petit Utilitaire');


-- PeutContenir
INSERT INTO PeutContenir VALUES('Lavilette', 'Velo', 3);
INSERT INTO PeutContenir VALUES('Latablette', 'Velo', 4);
INSERT INTO PeutContenir VALUES('VH', 'Velo Avec Remorque', 2);
INSERT INTO PeutContenir VALUES('Latablette', 'Velo Avec Remorque', 4);
INSERT INTO PeutContenir VALUES('Lavilette', 'Velo Electrique', 10);
INSERT INTO PeutContenir VALUES('Latablette', 'Velo Electrique', 5);
INSERT INTO PeutContenir VALUES('Lavilette', 'Voiture Electrique', 3);
INSERT INTO PeutContenir VALUES('Latablette', 'Voiture Electrique', 3);
INSERT INTO PeutContenir VALUES('Lavilette', 'Petit Utilitaire', 2);
INSERT INTO PeutContenir VALUES('VH', 'Petit Utilitaire', 1);
INSERT INTO PeutContenir VALUES('Latablette', 'Petit Utilitaire', 15);
INSERT INTO PeutContenir VALUES('VH', 'Velo', 5);
INSERT INTO PeutContenir VALUES('Vaujany', 'Velo', 25);
INSERT INTO PeutContenir VALUES('Bergers', 'Petit Utilitaire', 1);
INSERT INTO PeutContenir VALUES('Bergers', 'Velo Avec Remorque', 15);


-- EstDans
INSERT INTO EstDans VALUES(1, 'Lavilette');
INSERT INTO EstDans VALUES(2, 'Latablette');
INSERT INTO EstDans VALUES(4, 'VH');
INSERT INTO EstDans VALUES(5, 'VH');
INSERT INTO EstDans VALUES(6, 'Latablette');
INSERT INTO EstDans VALUES(7, 'VH');
INSERT INTO EstDans VALUES(8, 'Bergers');


-- Forfait
INSERT INTO Forfait VALUES(1,1);
INSERT INTO Forfait VALUES(2,2);
INSERT INTO Forfait VALUES(3,3);
INSERT INTO Forfait VALUES(4,1);
INSERT INTO Forfait VALUES(5,2);
INSERT INTO Forfait VALUES(6,4);
INSERT INTO Forfait VALUES(7,5);
INSERT INTO Forfait VALUES(8,6);


 -- ForfaitLimite
INSERT INTO ForfaitLimite VALUES(1, 10,2,1);
INSERT INTO ForfaitLimite VALUES(2, 15,3,2);
INSERT INTO ForfaitLimite VALUES(5, 25,5,5);
INSERT INTO ForfaitLimite VALUES(7, 25,0,5);


-- ForfaitIllimite
INSERT INTO ForfaitIllimite VALUES(4,'Jour', TO_DATE('2016-11-13', 'yyyy-mm-dd'), 20, 0.15, 1);
INSERT INTO ForfaitIllimite VALUES(3, 'Mois', TO_DATE('2016-11-03', 'yyyy-mm-dd'), 80, 0.3, 3);
INSERT INTO ForfaitIllimite VALUES(6, 'Annee', TO_DATE('2016-02-15', 'yyyy-mm-dd'), 200, 0.5, 4);
INSERT INTO ForfaitIllimite VALUES(8, 'Annee', TO_DATE('2016-02-18', 'yyyy-mm-dd'), 200, 0.5, 6);

--AffectationForfait
INSERT INTO AffectationForfait VALUES(1, 'Velo',4);
INSERT INTO AffectationForfait VALUES(2, 'Voiture Electrique', 1);
INSERT INTO AffectationForfait VALUES(2, 'Petit Utilitaire', 2);
INSERT INTO AffectationForfait VALUES(2, 'Velo', 5);
INSERT INTO AffectationForfait VALUES(3, 'Voiture Electrique', 3);
INSERT INTO AffectationForfait VALUES(4, 'Velo Avec Remorque', 6);
INSERT INTO AffectationForfait VALUES(5, 'Velo Avec Remorque', 7);
INSERT INTO AffectationForfait VALUES(6, 'Velo', 8);

--LOCATIONS

----------------------------ANAIS----------------------------------
-- Insertion a faire en plus pour tester pour la journée du 18/11/2016
INSERT INTO Location VALUES(005, TO_DATE('2016-11-12 10:25', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-18 02:25', 'yyyy-mm-dd hh24:mi'), 5, 'Lavilette', 'VH');
INSERT INTO Location VALUES(005, TO_DATE('2016-11-18 10:25', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-19 02:25', 'yyyy-mm-dd hh24:mi'), 5, 'Latablette', 'VH');
INSERT INTO Location VALUES(007, TO_DATE('2016-11-16 10:26', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-17 02:25', 'yyyy-mm-dd hh24:mi'), 6, 'Latablette', 'VH');
INSERT INTO Location VALUES(007, TO_DATE('2016-11-18 07:00', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-18 08:25', 'yyyy-mm-dd hh24:mi'), 6, 'VH', 'Lavilette');
INSERT INTO Location VALUES(007, TO_DATE('2016-11-18 08:30', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-18 12:10', 'yyyy-mm-dd hh24:mi'), 6, 'Lavilette', 'Latablette');
INSERT INTO Location VALUES(001, TO_DATE('2016-11-18 12:30', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-18 14:10', 'yyyy-mm-dd hh24:mi'), 6, 'Latablette', 'VH');
INSERT INTO Location VALUES(007, TO_DATE('2016-11-18 15:30', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-18 15:50', 'yyyy-mm-dd hh24:mi'), 6, 'Latablette', 'VH');


----------------------------MATTHIEU----------------------------------
-- Insertion pour les tests de temps moyens par véhicule par mois :

-- Location de trois heures
INSERT INTO Location VALUES(006, TO_DATE('2016-06-01 09:00', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-06-01 12:00', 'yyyy-mm-dd hh24:mi'), 6, 'Latablette', 'VH');
-- Location d'une heure
INSERT INTO Location VALUES(006, TO_DATE('2016-06-02 09:00', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-06-02 10:00', 'yyyy-mm-dd hh24:mi'), 6, 'VH', 'Latablette');
-- En moyenne, le véhicule 6 est loué 2 heures en Juin

-- Location sur Juillet et Aout pour vingt-cinq et douze heures respectivement
INSERT INTO Location VALUES(006, TO_DATE('2016-07-30 23:00', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-08-01 12:00', 'yyyy-mm-dd hh24:mi'), 6, 'Latablette', 'VH');

-- Location sur Septembre, Octobre, Novembre, Décembre : une heure en Septembre, douze heures en Décembre, les deux mois complets en Octobre/Novembre
INSERT INTO Location VALUES(006, TO_DATE('2016-09-30 23:00', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-12-01 12:00', 'yyyy-mm-dd hh24:mi'), 6, 'Latablette', 'VH');

-------------------------PAUL----------------------------------------
INSERT INTO Location VALUES(001, TO_DATE('2016-11-11 11:25', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-13 15:25', 'yyyy-mm-dd hh24:mi'), 1, 'Lavilette', 'Latablette'); 
INSERT INTO Location VALUES(003, TO_DATE('2016-11-11 09:25', 'yyyy-mm-dd hh24:mi'), NULL, 2, 'VH', NULL);
INSERT INTO Location VALUES(002, TO_DATE('2016-11-14 08:24', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-14 11:29', 'yyyy-mm-dd hh24:mi'), 2, 'Lavilette', 'Latablette'); 
INSERT INTO Location VALUES(002, TO_DATE('2016-11-15 10:25', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-11-17 02:25', 'yyyy-mm-dd hh24:mi'), 3, 'Latablette', 'VH');
INSERT INTO Location VALUES(003, TO_DATE('2006-11-01 03:31', 'yyyy-mm-dd hh24:mi'), TO_DATE('2006-12-02 02:00', 'yyyy-mm-dd hh24:mi'), 5, 'VH', 'Latablette');


-------------------------KYLIAN--------------------------------------
INSERT INTO Location VALUES(008, TO_DATE('2016-08-15 14:07', 'yyyy-mm-dd hh24:mi'), TO_DATE('2016-08-20 12:45', 'yyyy-mm-dd hh24:mi'), 5, 'Latablette', 'Bergers');
INSERT INTO Location VALUES(009, TO_DATE('2016-11-11 09:25', 'yyyy-mm-dd hh24:mi'), NULL, 7, 'Latablette', NULL);


---------------------------------------------------------------------


--Validation des tables
commit;
