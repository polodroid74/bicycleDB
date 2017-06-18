set autocommit off;

CREATE TABLE Abonnes(
  NumeroCB INT PRIMARY KEY,
  NomAbonne VARCHAR(30),
  PrenomAbonne VARCHAR(30),
  DateNaissance DATE,
  AdresseAbonne VARCHAR(100)
);

CREATE TABLE Station(
  NomStation VARCHAR(30) PRIMARY KEY,
  AdresseStation VARCHAR(100)
);

CREATE TABLE Categorie(
Categorie VARCHAR(20) PRIMARY KEY CHECK(Categorie IN ('Voiture Electrique', 'Velo', 'Velo Electrique', 'Velo Avec Remorque', 'Petit Utilitaire')),
DureeMax INTEGER CHECK(DureeMax > 0), --En jours
PrixHoraire FLOAT CHECK(PrixHoraire > 0.0),
MontantCaution FLOAT CHECK(MontantCaution >= 0.0)
);

CREATE TABLE Vehicule(
IdVehicule INT PRIMARY KEY,
NbPlaces INT CHECK(NbPlaces>0),
Categorie VARCHAR(20) REFERENCES Categorie NOT NULL
);


CREATE TABLE EstDans(
IdVehicule INT PRIMARY KEY REFERENCES Vehicule,
NomStation VARCHAR(30) REFERENCES Station
);

CREATE TABLE PeutContenir(
  NomStation VARCHAR(30) REFERENCES Station,
  Categorie VARCHAR(20) REFERENCES Categorie,
  NombrePlaces INT CHECK(NombrePlaces>0),
  PRIMARY KEY(NomStation, Categorie)
);

CREATE TABLE Forfait(
NumeroForfait INT PRIMARY KEY,
NumeroCB INT REFERENCES Abonnes NOT NULL
);

CREATE TABLE ForfaitLimite(
NumeroForfait INT REFERENCES Forfait PRIMARY KEY,
Prix FLOAT CHECK(Prix>=0.0),
NbMaxLoc INT CHECK(NbMaxLoc>=0),
NumeroCB INT REFERENCES Abonnes NOT NULL
);

CREATE TABLE ForfaitIllimite(
  NumeroForfait INT REFERENCES Forfait PRIMARY KEY,
  Duree VARCHAR(5) CHECK(Duree IN ('Jour', 'Mois', 'Annee')),
  DateDebut DATE,
  Prix FLOAT CHECK(Prix >= 0.0),
  Remise FLOAT CHECK (Remise >= 0.0),
  NumeroCB INT REFERENCES Abonnes
);

CREATE TABLE AffectationForfait(
NumeroCB INTEGER REFERENCES Abonnes,
Categorie VARCHAR(20) REFERENCES Categorie,
NumeroForfait INTEGER REFERENCES Forfait,
PRIMARY KEY(NumeroCB, Categorie, NumeroForfait)
);

CREATE TABLE Location(
IdVehicule INTEGER REFERENCES Vehicule,
DateDepart DATE,
DateArrivee DATE,
NumeroCB INTEGER NOT NULL REFERENCES Abonnes,
NomStationDepart VARCHAR(30) NOT NULL REFERENCES Station(NomStation),
NomStationArrivee VARCHAR(30) REFERENCES Station(NomStation),
PRIMARY KEY(IdVehicule, DateDepart)
);

--On enregistre la création de toutes les tables
commit;
