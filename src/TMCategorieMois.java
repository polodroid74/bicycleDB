import java.sql.*;
import java.util.Scanner;

public class TMCategorieMois {
	static final String CONN_URL = "jdbc:oracle:thin:@ensioracle1.imag.fr:1521:ensioracle1";
	static final String USER = "vincenky";
	static final String PASSWD = "vincenky";

	/* Requete SQL :
	 * renvoie le temps cumulé passé par les utilisateurs sur les véhicules
	 * d'une catégorie sur un mois calendaire donnés
	 * et le nombre d'utilisateurs concernés
	 * Fonctionnement :
	 * considère les véhicules qui ont été loués avant la fin du mois
	 * le temps calculé correspond à :
	 * plus_ancien(fin_location, fin_mois) - plus_récent(début_location, début_mois) 
	 */
	static final String PRE_STMT = "SELECT SUM("
		+ " CASE WHEN l.DateArrivee > TO_DATE(TO_CHAR(Last_day(To_date(?, 'yyyy-mm-dd hh24:mi')), 'yyyy-mm-dd') || ' ' || '23:59', 'yyyy-mm-dd hh24:mi') "
		+ " THEN TO_DATE(TO_CHAR(Last_day(To_date(?, 'yyyy-mm-dd hh24:mi')), 'yyyy-mm-dd') || ' ' || '23:59', 'yyyy-mm-dd hh24:mi') "
		+ " ELSE l.DateArrivee END "
		+ " - CASE WHEN l.DateDepart < To_Date(?, 'yyyy-mm-dd hh24:mi') THEN To_Date(?, 'yyyy-mm-dd hh24:mi') "
		+ " ELSE l.DateDepart END), "
		+ " count(*) "
		+ " FROM location l, vehicule v "
		+ " WHERE l.DateArrivee IS NOT NULL "
		+ " AND v.Categorie LIKE ? "
		+ " AND v.IdVehicule = l.IdVehicule "
		+ " AND l.DateDepart < TO_DATE(TO_CHAR(Last_day(To_date(?, 'yyyy-mm-dd hh24:mi')), 'yyyy-mm-dd') || ' ' || '23:59', 'yyyy-mm-dd hh24:mi') "
		+ " AND l.DateArrivee > TO_DATE(?, 'yyyy-mm-dd hh24:mi') ";

	/* Requete SQL :
	 * renvoie les noms des différentes catégories */
	static final String PRE_STMT2 = "SELECT Categorie FROM Categorie";


		public TMCategorieMois(String category, int month, int year) {
		try {
			// Enregistrement du driver Oracle
			System.out.print("Loading Oracle driver...");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("Loaded!");

			// Etablissement de la connexion
			System.out.print("Connecting to the database...");
			Connection conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(conn.TRANSACTION_SERIALIZABLE);
			System.out.println("Connected! \n");

			// Vérification que la catégorie existe sinon il faut re-éxécuter le programme
			PreparedStatement stmt = conn.prepareStatement(PRE_STMT2);
			ResultSet rset = stmt.executeQuery();
			if (!categorieDansBD(rset, category)) {
				System.out.println("La catégorie entrée n'existe pas ! \n");
					System.out.println("Veuillez en sélectionner une parmi :");
					rset = stmt.executeQuery();
					System.out.println(listeCategoriesDansBD(rset));
					return;
			}

			// Obtention du temps total d'utilisation d'une catégorie sur un mois
			// et le nb d'utilisateurs associé
			stmt = conn.prepareStatement(PRE_STMT);
			stmt.setString(1, year+"-"+month+"-01");
			stmt.setString(2, year+"-"+month+"-01");
			stmt.setString(3, year+"-"+month+"-01 00:00");
			stmt.setString(4, year+"-"+month+"-01 00:00");
			stmt.setString(5, category);
			stmt.setString(6, year+"-"+month+"-01");
			stmt.setString(7, year+"-"+month+"-01 00:00");
		        rset = stmt.executeQuery();

			System.out.println("Temps moyen de location d'un véhicule de type "
					   + category
					   + " pendant le mois "
					   + month
					   + "/"
					   + year
					   + " par utilisation : ");

			// Calcul du temps moyen d'utilisation, affichage utilisateur
			float moy = calculMoyenne(rset);
			System.out.print(" * ");
			System.out.printf("%.2f", moy);
			System.out.print(" jours, ou encore \n * ");
			System.out.printf("%.2f", moy*24);
			System.out.println(" heures");

			// Fermetures
			rset.close();
			stmt.close();
			conn.close();

		} catch (SQLException e) {
			System.err.println("Failed!");
			e.printStackTrace(System.err);
		}
	}

	// Renvoie le temps total utilisation / nombre d'utilisateurs
	private float calculMoyenne(ResultSet rset) throws SQLException {
		rset.next();
		try {
			Float nbJours = Float.parseFloat(rset.getString(1));
			System.out.println(nbJours);
			Float nbUtilisateurs = Float.parseFloat(rset.getString(2));
			return nbJours/nbUtilisateurs;
		} catch (Exception e) {
			// aucune location ne correspond aux critères -> la requête SQL ne renvoie rien
			return 0;
		}
	}

	// Vérifie qu'une catégorie en entrée existe dans la BD ou non
	private boolean categorieDansBD(ResultSet rset, String cat) throws SQLException {
		while (rset.next()) {
			if (cat.equals(rset.getString(1))) {
				return true;
			}
		}
		return false;
	}

	// Renvoie la liste des catégories pour lequelles au moins 1 véhicule a été loué
	private String listeCategoriesDansBD(ResultSet rset) throws SQLException {
		String liste = " ";
		while (rset.next()) {
			liste = liste + "* " + rset.getString(1) + "\n ";
		}
		return liste;
	}



	public static void main(String args[]) {

		// Récupération des données utilisateur
		Scanner sc = new Scanner(System.in);
		System.out.println("Cette fonctionnalité renvoie le temps d'utilisation moyen"
				   + " d'un véhicule d'une catégorie sur le mois de votre choix");
		System.out.println("Entrez la catégorie :");
		String categorie = sc.nextLine();
		System.out.println("Choisissez l'année AAAA :");
		int annee = sc.nextInt();
		sc.nextLine();
		System.out.println("Choisissez le mois MM :");
		int mois = sc.nextInt();
		sc.nextLine();

		// Vérification naïve de la cohérence du mois et de l'année
		if (mois > 12 || mois < 1) {
			throw new IllegalArgumentException("Entrez un entier entre 1 et 12 inclus pour le mois");
		}
		if (annee < 1000 || annee > 9999) {
			throw new IllegalArgumentException("Entrez une une année avec 4 chiffres");
		}

		// Calcul du temps moyen d'utilisation d'un véhicule de la catégorie pendant le mois donnés
		new TMCategorieMois(categorie, mois, annee);
	}
}
