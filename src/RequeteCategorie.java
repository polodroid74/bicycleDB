import java.sql.*;
import java.util.Scanner;

public class RequeteCategorie {

	// Variables pour la connexion
	static final String CONN_URL = "jdbc:oracle:thin:@ensioracle1.imag.fr:1521:ensioracle1";
	static final String USER = "vincenky"; // A remplacer pour votre compte
	static final String PASSWD = "vincenky";

	static final String PRE_STMT1 = "SELECT MAX((current_date - DateNaissance)/365) FROM Abonnes";
	static final String PRE_STMT2 = "SELECT c.Categorie " +
			"FROM Categorie c, Location l, Abonnes a, Vehicule v " +
			"WHERE c.Categorie = v.Categorie " +
			"AND v.IdVehicule = l.IdVehicule " +
			"AND a.NumeroCB = l.NumeroCB " +
			"AND l.DateArrivee IS NOT NULL " +
			"AND ((l.DateDepart - a.DateNaissance)/365) BETWEEN ? and ? " +
			"GROUP BY c.Categorie " +
			"HAVING SUM(l.DateArrivee - l.DateDepart) = (SELECT MAX(SUM(l.DateArrivee - l.DateDepart)) " +
			"FROM Categorie c, Location l, Abonnes a, Vehicule v " +
			"WHERE c.Categorie = v.Categorie " +
			"AND v.IdVehicule = l.IdVehicule " +
			"AND a.NumeroCB = l.NumeroCB " +
			"AND l.DateArrivee IS NOT NULL " +
			"AND ((l.DateDepart - a.DateNaissance)/365) BETWEEN ? and ? " +
			"GROUP BY c.Categorie)";

	public RequeteCategorie() {
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
			PreparedStatement stmt = conn.prepareStatement(PRE_STMT1);
			ResultSet rset = stmt.executeQuery();

			//Pour une tranche d'age spécifique ?
			System.out.println("\nVoulez vous renseigner une tranche d'age spécifique ? (o = oui, Autre entrée = non)\nSi non, les résultats seront affichés par tranche d'âge de 10 ans jusqu'au plus vieil abonné enregistré.");
			String entree = sc.nextLine();
			if (entree.equals("o")) {
				System.out.println("\n\n\nVeuillez rentrer la borne inférieure de la tranche d'age à considérer :");
				Integer borneInf = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne
				System.out.println("Et sa borne supérieure :");
				Integer borneSup = sc.nextInt();
				sc.nextLine(); //Vidage de la ligne

				//Execution de la requete
				stmt = conn.prepareStatement(PRE_STMT2);
				stmt.setInt(1, borneInf);
				stmt.setInt(2, borneSup);
				stmt.setInt(3, borneInf);
				stmt.setInt(4, borneSup);
				rset = stmt.executeQuery();

				if (rset.next()) {
					System.out.println("\n\n\nLa catégorie la plus utilisée pour la tranche d'âge " + borneInf + "-" + borneSup + " ans est '" + rset.getString(1) + "'");
				} else {
					System.out.println("\n\n\nAucune location pour la tranche d'âge " + borneInf + "-" + borneSup + "ans");
				}

			} else {         	
				//Calcul de l'age du plus vieux abonné
				Integer trancheMax = 0;
				if (rset.next()) {
					trancheMax = (rset.getInt(1)/10) + 1;
				} else {
					System.out.println("Aucun abonné dans la base");
				}

				//Calcul de l'utilisation des catégories en fonction de la tranche d'age des utilisateurs
				String categorie = "";
				System.out.println("\n\n\nLes catégories les plus utilisées en fonction des tranches d'age sont :");

				for (int i=0; i < trancheMax; i++){
					stmt = conn.prepareStatement(PRE_STMT2);
					stmt.setInt(1, i*10);
					stmt.setInt(2, (i+1)*10);
					stmt.setInt(3, i*10);
					stmt.setInt(4, (i+1)*10);
					rset = stmt.executeQuery();

					//Affichage du resultat
					if (rset.next()) {
						categorie = rset.getString(1);
						System.out.println("  *  " + i*10 + "-" + (i+1)*10 + "  \t--->\t  " + categorie);
					} else {
						System.out.println("  *  " + i*10 + "-" + (i+1)*10 + "  \t--->\t  Pas de locations");
					}
				}
			}
			rset.close(); // Fermeture de tous les résultats (dès que plus nécessaire)
			stmt.close(); // Fermeture de toutes les requetes (dès que plus nécessaire)
			conn.close(); // Fermeture de la connexion (fin du constructeur)
		} catch (SQLException e) {
			// En cas d'erreur
			System.err.println("Failed!");
			e.printStackTrace(System.err);
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

	public static void main(String args[]) {
		new RequeteCategorie();
	}
}

