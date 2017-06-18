ORACLE_HOME=/opt/oracle

all: DateQuery exeDateQuery


Facturation: src/Facturation.java
	javac -d bin  -sourcepath $(CLASSPATH) src/Facturation.java


CategorieUtilisee: src/RequeteCategorie.java
	javac -d bin -sourcepath $(CLASSPATH) src/RequeteCategorie.java

InsertionDonnees: src/InsertionDonnees.java
	javac -d bin -sourcepath $(CLASSPATH) src/InsertionDonnees.java

TMVehiculeMois: src/TMVehiculeMois.java
	javac -d bin -sourcepath $(CLASSPATH) src/TMVehiculeMois.java

TMCategorieMois: src/TMCategorieMois.java
	javac -d bin -sourcepath $(CLASSPATH) src/TMCategorieMois.java

TauxStation: src/TauxStation.java
	javac -d bin -sourcepath $(CLASSPATH) src/TauxStation.java

CreationBD: src/CreationBD.java
	javac -d bin -sourcepath $(CLASSPATH) src/CreationBD.java






exeFacturation: Facturation
	java -classpath bin:$(CLASSPATH)  Facturation

exeCategorieUtilisee: CategorieUtilisee
	java -classpath bin:$(CLASSPATH)  RequeteCategorie

exeInsertionDonnees: InsertionDonnees
	java -classpath bin:$(CLASSPATH)  InsertionDonnees

exeTMVehiculeMois: TMVehiculeMois
	java -classpath bin:$(CLASSPATH)  TMVehiculeMois

exeTMCategorieMois: TMCategorieMois
	java -classpath bin:$(CLASSPATH)  TMCategorieMois

exeTauxStation: TauxStation
	java -classpath bin:$(CLASSPATH) TauxStation 2016 11 18


clean:
	rm -rf bin/*.class
	rm -rf src/*.class
