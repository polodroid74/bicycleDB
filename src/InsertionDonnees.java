import java.sql.*;
import javax.swing.JOptionPane;
import java.util.Scanner;

public class InsertionDonnees {

	// Variables pour la connexion
	static final String CONN_URL = "jdbc:oracle:thin:@ensioracle1.imag.fr:1521:ensioracle1";
	static final String USER = "vincenky"; // A remplacer pour votre compte
	static final String PASSWD = "vincenky";	
	
	public InsertionDonnees() {
		try {

			Scanner sc = new Scanner(System.in);
			// Enregistrement du driver Oracle
			System.out.print("Loading Oracle driver..."); 
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("Loaded!");

			// Etablissement de la connexion
			System.out.print("Connecting to the database..."); 
			Connection conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
			System.out.println("Connected!");

			conn.setAutoCommit(false); // Autocommit en false : toujours !
			conn.setTransactionIsolation(conn.TRANSACTION_SERIALIZABLE); // Isolation des transactions

			//Debut du traitement de la requete

			PreparedStatement stmt;
			ResultSet rset;

			//Choix des donnees à insérer
			Integer choix = 0;
			while (choix < 1 || choix > 10) {
                            System.out.println("Quelle information souhaitez-vous insérer dans la base de données ?\n" +
                                               "1 - Abonne\n" +
                                               "2 - Forfait Locations Illimitees\n" +
                                               "3 - Forfait Locations Limitees\n" +
                                               "4 - Categorie\n" +
                                               "5 - Vehicule\n" +
                                               "6 - Location\n" +
                                               "7 - Arrivée\n" +
                                               "8 - Station\n" +
                                               "9 - Localisation véhicule\n" +
                                               "10 - Exit\n");
                            choix = sc.nextInt();
                            sc.nextLine(); //Vidage de la ligne en cours
			}
			
			stmt = null;
			rset = null;
			
			Integer numeroCB;
			Integer numeroForfait;
			Double prix;
			String categorie = "";
			String cats;
			Integer nbForfaitsDejaAcquis;
			Integer idVehicule;
			String dateDepart;
			String nomStation;
			switch (choix){
			case 1:
				//Abonné - RAS
				System.out.println("Veuillez entrer le numéro de carte bancaire de l'abonne :");
				numeroCB = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le nom de l'abonne :");
				String nomAbonne = sc.nextLine();
				System.out.println("Veuillez entrer le prenom de l'abonne :");
				String prenomAbonne = sc.nextLine();
				System.out.println("Veuillez entrer la date de naissance de l'abonne (sous la forme YYYY-MM-DD) :");
				String dateNaissance = sc.nextLine();
				System.out.println("Veuillez entrer l'adresse de l'abonne :");
				String adresseAbonne = sc.nextLine();
				String STMT_A = "INSERT INTO Abonnes VALUES(" + numeroCB + ",'" + nomAbonne + "','" + prenomAbonne + "',TO_DATE('" + dateNaissance + "', 'yyyy-mm-dd'),'" + adresseAbonne +"')";
				stmt = conn.prepareStatement(STMT_A);
				stmt.executeUpdate();
				System.out.println("\n---------------------------------------------------------\n" +
						"Abonné correctement inséré");
				conn.commit();
				break;
			case 2:
				//Forfait Nombre de Locations Illimité
				//Calcul du numero de forfait disponible le plus petit
				stmt = conn.prepareStatement("SELECT MAX(NumeroForfait) FROM Forfait");
				rset = stmt.executeQuery();
				numeroForfait = 0;
				if (rset.next()) {
					numeroForfait = rset.getInt(1) + 1;
				}

				//Récupération de la liste des catégories
				stmt = conn.prepareStatement("SELECT DISTINCT Categorie FROM Categorie");
				rset = stmt.executeQuery();
				cats = "(";
				if (rset.next()) {
					cats += rset.getString(1);
					while (rset.next()) {
						cats += "," + rset.getString(1);
					}
					cats += ")";
				} else {
					System.out.println("Aucune catégorie à laquelle associer le forfait");
					break;
				}

				System.out.println("Veuillez entrer le numéro de carte bancaire de l'abonne :");
				numeroCB = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer la duree du forfait (Jour, Mois ou Annee) :");
				String duree = sc.nextLine();
				System.out.println("Veuillez entrer la date de début du forfait (sous la forme YYYY-MM-DD) :");
				String dateDebut = sc.nextLine();
				System.out.println("Veuillez entrer le prix du forfait :");
				prix = sc.nextDouble();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer la remise allouée par le forfait :");
				Double remise = sc.nextDouble();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer la catégorie pour laquelle ce forfait est valide " + cats + " :");
				categorie = sc.nextLine();

				// 1 forfait max par categorie
				nbForfaitsDejaAcquis = calculNombreForfaitsDejaAcquis(categorie, numeroCB, stmt, rset, conn);

				// inserer un forfait
				if (nbForfaitsDejaAcquis < 1){
					String STMT_F = "INSERT INTO Forfait VALUES(" + numeroForfait + "," + numeroCB + ")";
					stmt = conn.prepareStatement(STMT_F);
					stmt.executeUpdate();
				} else {
					System.out.println("L'abonne a dejà un forfait valide pour cette catégorie de véhicule");
					conn.rollback();
					break;
				}
				
				String STMT_FI = "INSERT INTO ForfaitIllimite VALUES(" + numeroForfait + ",'" + duree + "',TO_DATE('" + dateDebut + "', 'yyyy-mm-dd')," + prix + "," + remise + "," + numeroCB + ")";
				stmt = conn.prepareStatement(STMT_FI);
				stmt.executeUpdate();
				
				// creer l'affectation forfait
				String STMT_FAI = "INSERT INTO AffectationForfait VALUES(" + numeroCB + ",'" + categorie + "'," + numeroForfait + ")";
				stmt = conn.prepareStatement(STMT_FAI);
				stmt.executeUpdate();
				System.out.println("\n---------------------------------------------------------\n" +
						"Forfait correctement inséré");
				conn.commit();
				break;
				
			case 3:
				//Forfait Nombre de Locations Limité
				//Calcul du numero de forfait disponible le plus petit
				stmt = conn.prepareStatement("SELECT MAX(NumeroForfait) FROM Forfait");
				rset = stmt.executeQuery();
				numeroForfait = 0;
				if (rset.next()) {
					numeroForfait = rset.getInt(1) + 1;
				}

				//Récupération de la liste des catégories
				stmt = conn.prepareStatement("SELECT DISTINCT Categorie FROM Categorie");
				rset = stmt.executeQuery();
				cats = "(";
				if (rset.next()) {
					cats += rset.getString(1);
					while (rset.next()) {
						cats += "," + rset.getString(1);
					}
					cats += ")";
				} else {
					System.out.println("Aucune catégorie à laquelle associer le forfait");
					break;
				}

				System.out.println("Veuillez entrer le numéro de carte bancaire de l'abonne :");
				numeroCB = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le prix du forfait :");
				prix = sc.nextDouble();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le nombre de locations gratuites :");
				Integer nbLoc = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer la catégorie pour laquelle ce forfait est valide " + cats + " :");
				categorie = sc.nextLine();

				// 1 forfait max par categorie
				nbForfaitsDejaAcquis = calculNombreForfaitsDejaAcquis(categorie, numeroCB, stmt, rset, conn);

				// inserer un forfait
				if (nbForfaitsDejaAcquis < 1){
					String STMT_Forf = "INSERT INTO Forfait VALUES(" + numeroForfait + "," + numeroCB + ")";
					stmt = conn.prepareStatement(STMT_Forf);
					stmt.executeUpdate();
				} else {
					System.out.println("L'abonne a dejà un forfait valide pour cette catégorie de véhicule");
					conn.rollback();
					break;
				}
				
				String STMT_FL = "INSERT INTO ForfaitLimite VALUES(" + numeroForfait + "," + prix + "," + nbLoc + "," + numeroCB + ")";
				stmt = conn.prepareStatement(STMT_FL);
				stmt.executeUpdate();
				
				// creer l'affectation forfait
				String STMT_FAL = "INSERT INTO AffectationForfait VALUES(" + numeroCB + ",'" + categorie + "'," + numeroForfait + ")";
				stmt = conn.prepareStatement(STMT_FAL);
				stmt.executeUpdate();
				System.out.println("\n---------------------------------------------------------\n" +
						"Forfait correctement inséré");
				conn.commit();
				break;
				
			case 4:
				//Categorie - RAS
				System.out.println("Veuillez entrer le nom de la catégorie de véhicule :");
				categorie = sc.nextLine();
				System.out.println("Veuillez entrer la duree maximale de location pour un vehicule de cette catégorie (en jours) :");
				Integer dureeMax = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le prix horaire pour un vehicule de cette catégorie :");
				Double prixHoraire = sc.nextDouble();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le montant de la caution pour un vehicule de cette catégorie:");
				Double caution = sc.nextDouble();
				sc.nextLine(); //Vidage de la ligne
				String STMT_C = "INSERT INTO Categorie VALUES('" + categorie + "'," + dureeMax + "," + prixHoraire + "," + caution + ")";
				stmt = conn.prepareStatement(STMT_C);
				stmt.executeUpdate();
				System.out.println("\n---------------------------------------------------------\n" +
						"Categorie correctement inséré");
				conn.commit();
				break;
				
			case 5:
				//Vehicule - RAS
				//Récupération de la liste des catégories
				stmt = conn.prepareStatement("SELECT DISTINCT Categorie FROM Categorie");
				rset = stmt.executeQuery();
				cats = "(";
				if (rset.next()) {
					cats += rset.getString(1);
					while (rset.next()) {
						cats += "," + rset.getString(1);
					}
					cats += ")";
				} else {
					System.out.println("Aucune catégorie à laquelle associer le véhicule");
					break;
				}
				
				System.out.println("Veuillez entrer le nom de la catégorie du véhicule " + cats + ":");
				categorie = sc.nextLine();
				System.out.println("Veuillez entrer le numéro d'indentification du véhicule :");
				idVehicule = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le nombre de places disponibles dans le véhicule :");
				Integer nbPlacesVeh = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le nom de la station où se trouve le véhicule :");
				String estDansStation = sc.nextLine();
				String STMT_V = "INSERT INTO Vehicule VALUES(" + idVehicule + "," + nbPlacesVeh + ",'" + categorie + "')";
				stmt = conn.prepareStatement(STMT_V);
				stmt.executeUpdate();
				
				// Insertion dans la table de localisation
				String STMT_ED = "INSERT INTO EstDans VALUES(" + idVehicule + ",'" + estDansStation + "')";
				stmt = conn.prepareStatement(STMT_ED);
				stmt.executeUpdate();
				
				System.out.println("\n---------------------------------------------------------\n" +
						"Vehicule correctement inséré");
				conn.commit();                             
				break;
				
			case 6:
				//Location
				System.out.println("Veuillez entrer le numéro d'indentification du véhicule :");
				idVehicule = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer la date de départ (sous la forme YYYY-MM-DD HH:MI) :");
				dateDepart = sc.nextLine();

				// Il faut vérifier qu'une autre location n'avait pas lieu pendant ce moment là ou que le véhicule soit bien rendu par le précédent locataire
				String STMT_LOC = "SELECT COUNT(*) FROM Location WHERE IdVehicule = ? AND DateDepart - TO_DATE(?, 'yyyy-mm-dd hh24:mi') <= 0 AND "
						+ "(DateArrivee IS NULL OR DateArrivee - TO_DATE(?, 'yyyy-mm-dd hh24:mi') >= 0)";
				stmt = conn.prepareStatement(STMT_LOC);
				stmt.setInt(1, idVehicule);
				stmt.setString(2, dateDepart);
				stmt.setString(3, dateDepart);
				rset = stmt.executeQuery();
				if (rset.next() && rset.getInt(1) > 0) {
					System.out.println("Le véhicule est déjà en location à cette date ou n'a pas encore été rendu");
					conn.rollback();
					break;
				}

				System.out.println("Veuillez entrer le numéro de carte bancaire de l'abonne :");
				numeroCB = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer le nom de la station de départ :");
				String nomStationDepart = sc.nextLine();
				String STMT_L = "INSERT INTO Location VALUES(" + idVehicule + ",TO_DATE('" + dateDepart + "', 'yyyy-mm-dd hh24:mi'), NULL," + numeroCB + ",'" + nomStationDepart + "', NULL)";
				stmt = conn.prepareStatement(STMT_L);
				stmt.executeUpdate();
				
				//On le retire maintenant de la table EstDans
				String STMT_SUPPR_LOC = "DELETE FROM EstDans WHERE IdVehicule = ?";
				stmt = conn.prepareStatement(STMT_L);
				stmt.setInt(1, idVehicule);
				stmt.executeUpdate();
				
				System.out.println("\n---------------------------------------------------------\n" +
						"Location correctement insérée");
				conn.commit();
				break;
				
			case 7:
				//Arrivee
				System.out.println("Veuillez entrer le numéro d'indentification du véhicule :");
				idVehicule = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Veuillez entrer la date de départ (sous la forme YYYY-MM-DD HH:MI) :");
				dateDepart = sc.nextLine();
				String STMT_GET_LOC = "SELECT DateArrivee FROM Location WHERE IdVehicule = " + idVehicule + " AND DateDepart = TO_DATE('" + dateDepart + "', 'yyyy-mm-dd hh24:mi')";
				stmt = conn.prepareStatement(STMT_GET_LOC);
				rset = stmt.executeQuery();
				if (! rset.next()) {
					System.out.println("Aucune location n'est enregistrée pour ce véhicule à cette date de départ");
					conn.rollback();
					break;
				} else if (rset.getString(1) != null) {//! rset.getString(1).equals("")) {
					System.out.println("L'arrivée de cette location est déjà enregistrée");
					conn.rollback();
					break;
				}

				System.out.println("Veuillez entrer la date d'arrivée (sous la forme YYYY-MM-DD HH:MI) :");
				String dateArrivee = sc.nextLine();
				System.out.println("Veuillez entrer le nom de la station d'arrivée :");
				String nomStationArrivee = sc.nextLine();
				
				//Verification que la station d'arrivée a assez de places
				//Recuperation de la catégorie du véhicule
				String STMT_CAT = "SELECT Categorie FROM Vehicule WHERE IdVehicule = ?";
				stmt = conn.prepareStatement(STMT_CAT);
				stmt.setInt(1, idVehicule);
				rset = stmt.executeQuery();
				if (rset.next()) {
					categorie = rset.getString(1);
				}
				
				Integer placesDispo = 0;
				String STMT_NB_PLC = "SELECT NombrePlaces FROM PeutContenir WHERE NomStation = ? AND Categorie = ?";
				stmt = conn.prepareStatement(STMT_NB_PLC);
				stmt.setString(1, nomStationArrivee);
				stmt.setString(2, categorie);
				rset = stmt.executeQuery();
				if (rset.next()) {
					placesDispo += rset.getInt(1);
				}
				String STMT_NB_VEH = "SELECT COUNT(*) FROM EstDans e, Vehicule v WHERE e.IdVehicule = v.IdVehicule AND e.NomStation = ? AND v.Categorie = ?";
				stmt = conn.prepareStatement(STMT_NB_VEH);
				stmt.setString(1, nomStationArrivee);
				stmt.setString(2, categorie);
				rset = stmt.executeQuery();
				if (rset.next()) {
					placesDispo -= rset.getInt(1);
				}
				
				if (placesDispo <= 0) {
					System.out.println("Plus de place pour cette catégorie de véhicules dans la station d'arrivée");
					conn.rollback();
					break;
				}
				//Sinon on peut mettre à jour la location correspondante
				String STMT_LA = "UPDATE Location SET DateArrivee = TO_DATE('" + dateArrivee + "', 'yyyy-mm-dd hh24:mi'), NomStationArrivee = '" + nomStationArrivee + "' WHERE IdVehicule = " + idVehicule + " AND DateDepart = TO_DATE('" + dateDepart + "', 'yyyy-mm-dd hh24:mi')";
				stmt = conn.prepareStatement(STMT_LA);
				stmt.executeUpdate();
				
				//Et mettre la station où est le véhicule, non présent alors dans la table EstDans car loué
				String STMT_EDMAJ = "INSERT INTO EstDans VALUES(" + idVehicule + ",'" + nomStationArrivee + "')";
				stmt = conn.prepareStatement(STMT_EDMAJ);
				stmt.executeUpdate();
				
				System.out.println("\n---------------------------------------------------------\n" +
						"Arrivée correctement enregistrée");
				conn.commit();
				break;
				
			case 8:
				//Station
				System.out.println("Veuillez entrer le nom de la station :");
				nomStation = sc.nextLine();
				System.out.println("Veuillez entrer son adresse :");
				String adresseStation = sc.nextLine();
				String STMT_S = "INSERT INTO Station VALUES('" + nomStation + "','" + adresseStation + "')";
				stmt = conn.prepareStatement(STMT_S);
				stmt.executeUpdate();
				
				//Contient les catégories
				Integer nbCats = 0;
				Integer nbPlaces = 0;
				categorie = "Init";
				String STMT_PC = "INSERT INTO PeutContenir VALUES(?, ?, ?)";
				System.out.println("Veuillez entrer un nom de catégorie que peut contenir la station, entrer 'End' pour terminer:");
				categorie = sc.nextLine();
				while (! (categorie.equals("End") || categorie.equals("end"))) {
					System.out.println("Veuillez entrer le nombre de places disponibles pour cette catégorie dans la station :");
					nbPlaces = sc.nextInt();
					sc.nextLine(); //Vidage de la ligne
					nbCats ++;
					
					stmt = conn.prepareStatement(STMT_PC);
					stmt.setString(1, nomStation);
					stmt.setString(2, categorie);
					stmt.setInt(3, nbPlaces);
					stmt.executeUpdate();
					System.out.println("Veuillez entrer un nom de catégorie que peut contenir la station, entrer 'End' pour terminer:");
					categorie = sc.nextLine();
				}
				if (nbCats < 1) {
					System.out.println("La station doit pouvoir accueillir au moins une catégorie de véhicules");
					conn.rollback();
					break;
				}
				System.out.println("\n---------------------------------------------------------\n" +
						"Station correctement insérée");
				conn.commit();
				break;
				
			case 9:
				//EstDans
				System.out.println("Veuillez entrer le numéro d'indentification du véhicule :");
				idVehicule = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				//Verifier que le véhicule n'est pas actuellement en location, donc qu'il est dans la table
				String STMT_WHERE = "SELECT NomStation FROM EstDans WHERE idVehicule = ?";
				stmt = conn.prepareStatement(STMT_WHERE);
				stmt.setInt(1, idVehicule);
				rset = stmt.executeQuery();
				if (! rset.next() || rset.getString(1) == null) {
					System.out.println("Le véhicule sélectionné n'est pas actuellement dans une station (loué par exemple)");
					conn.rollback();
					break;
				}
				// Sinon on entre son emplacement
				System.out.println("Veuillez entrer le nom de la station où se trouve maintenant le véhicule :");
				nomStation = sc.nextLine();
				String STMT_ED2= "UPDATE EstDans SET NomStation = '" + nomStation + "' WHERE IdVehicule = " + idVehicule;
				stmt = conn.prepareStatement(STMT_ED2);
				stmt.executeUpdate();

				System.out.println("\n---------------------------------------------------------\n" +
						"Localisation du véhicule correctement mise à jour");
				conn.commit();
				break;
			default: //Convient aussi pour l'option 10
				System.out.println("Sortie de l'outil d'insertion");
				break;
				
				
			}
			if (rset != null) {
				rset.close(); // Fermeture de tous les résultats (dès que plus nécessaire)
			}
			if (stmt != null) {
				stmt.close(); // Fermeture de toutes les requetes (dès que plus nécessaire)
			}
			conn.close(); // Fermeture de la connexion (fin du constructeur)
		} catch (SQLException e) {
			// En cas d'erreur
			System.err.println("Failed!");
			System.out.println(e);
		}
	}

	private void dumpResultSet(ResultSet rset) throws SQLException {
		ResultSetMetaData rsetmd = rset.getMetaData();
		int i = rsetmd.getColumnCount();
		for (int k=1;k<=i;k++)
			System.out.print(rsetmd.getColumnName(k) + "\t");
		System.out.println();
		while (rset.next()) {
			for (int j = 1; j <= i; j++) {
				System.out.print(rset.getString(j) + "\t");
			}
			System.out.println();
		}
	}
	
	public static Integer calculNombreForfaitsDejaAcquis(String categorie, Integer numeroCB, PreparedStatement stmt, ResultSet rset, Connection conn) throws SQLException{
		Integer nbForfaitsDejaAcquis = 0;
		stmt = conn.prepareStatement("SELECT COUNT(*) " +
				"FROM AffectationForfait af, ForfaitIllimite fi " +
				"WHERE af.Categorie = ? " +
				"AND af.NumeroForfait = fi.NumeroForfait " +
				"AND af.NumeroCB = ? " +
				"AND ((fi.Duree = 'Jour' AND (TRUNC(fi.DateDebut, 'DD')  - TRUNC(current_date, 'DD')) = 0) " +
				"OR (fi.Duree = 'Mois' AND (TRUNC(fi.DateDebut, 'MM')  - TRUNC(current_date, 'MM')) = 0) " +
				"OR (fi.Duree = 'Annee' AND (TRUNC(fi.DateDebut, 'YYYY')  - TRUNC(current_date, 'YYYY')) = 0))");
		stmt.setString(1, categorie);
		stmt.setInt(2, numeroCB);
		rset = stmt.executeQuery();
		if (rset.next()) {
			nbForfaitsDejaAcquis += rset.getInt(1);
		}
		
		stmt = conn.prepareStatement("SELECT COUNT(*) " +
				"FROM AffectationForfait af, ForfaitLimite fl " +
				"WHERE af.Categorie = ? " +
				"AND af.NumeroForfait = fl.NumeroForfait " +
				"AND af.NumeroCB = ? " +
				"AND fl.NbMaxLoc > 0");
		stmt.setString(1, categorie);
		stmt.setInt(2, numeroCB);
		rset = stmt.executeQuery();
		if (rset.next()) {
			nbForfaitsDejaAcquis += rset.getInt(1);
		}
		return nbForfaitsDejaAcquis;
	}

	public static void main(String args[]) {
		new InsertionDonnees();
	}
}

