import java.sql.*;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.ArrayList;

public class TMVehiculeMois{
	// Variables pour la connexion
	static final String CONN_URL = "jdbc:oracle:thin:@ensioracle1.imag.fr:1521:ensioracle1";
	static final String USER = "vincenky";
	static final String PASSWD = "vincenky";
	
	static final String PRE_STMT1 = "SELECT IdVehicule, DateDepart, DateArrivee "
									+ "FROM Location "
									+ "WHERE DateArrivee IS NOT NULL";
	static final String PRE_STMT2 = "SELECT COUNT(DISTINCT IdVehicule) FROM Location WHERE DateArrivee IS NOT NULL"; 
	
	public TMVehiculeMois() {
		try {
			// Enregistrement du driver Oracle
			System.out.print("Loading Oracle driver..."); 
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("Loaded!");
			
			// Etablissement de la connexion
			System.out.print("Connecting to the database..."); 
			Connection conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
			conn.setAutoCommit(false); // Autocommit en false : toujours !
			conn.setTransactionIsolation(conn.TRANSACTION_SERIALIZABLE); // Isolation des transactions
			System.out.println("Connected!");
			
			// Selectionner les locations telles que DateArrivee =/= null et les regrouper par véhicule
			PreparedStatement stmt1 = conn.prepareStatement(PRE_STMT1);
			ResultSet rset1 = stmt1.executeQuery();
			
			/* Décommenter les six prochaines lignes pour afficher la liste des locations terminées qui seront
			 * prises en compte pour la création du tableau résultat. */

			//System.out.println("");
			//System.out.println("Liste des locations terminées :");
			//System.out.println("");
			//dumpResultSet(rset1);
			//System.out.println("");
			//rset1 = stmt1.executeQuery(); // Rechargement du résultat qui a été consummé par dumpResultSet
			
			PreparedStatement stmt2 = conn.prepareStatement(PRE_STMT2);
			ResultSet rset2 = stmt2.executeQuery();
			
			// Calcul du nombre de véhicules loués distincts
			int nbVehicules = 0;
			if (rset2.next()) {
				nbVehicules = rset2.getInt(1);
			} else {
				System.out.println("Aucun abonné dans la base");
				throw new IllegalArgumentException("La base de donnée doit contenir au moins une location terminée !");
			}
			
			/* Tableau de listes composées d'ElementListe : table de hachage.
			* tableau[i] contient la liste pour le véhicule i.
			* Chaque élément de la liste est un ElementListe composé d'un Mois et d'une Durée (cf. doc de la classe).
			* Un élément de la liste correspond à un bout de durée de location :
			* La location est découpée selon le nombre de mois couverts par la durée.
			* Exemple : si le Véhicule 1 est loué du 31 Janvier 12h00 au 1 Février 09h00,
			* la liste de tableau[1] contiendra deux éléments : (12 heures, Janvier) et (09 heures, Février).
			*/
			ArrayList<ElementListe> tableau[] = new ArrayList[nbVehicules];
			for(int i=0; i<nbVehicules; i++) {
				tableau[i] = new ArrayList<ElementListe>();
			}
			
			/* indicesVehicules est le tableau contenant les idVehicules distincts.
			 * Ainsi dans les futurs tableaux, l'élément i du tableau correspond au
			 * véhicule indicesVehicules[i]. */
			int[] indicesVehicules = new int[nbVehicules];
			int indice_courant = 0;
			while (rset1.next()) {
				int numero_courant = rset1.getInt(1);
				if (!estDans(indicesVehicules, numero_courant)) {
					indicesVehicules[indice_courant] = numero_courant;
					indice_courant += 1;
				}
			}

			rset1 = stmt1.executeQuery(); // Rechargement du Resultset consummé par le traitement précédent
			
			/* Remplissage de la table de hachage
			 * Le hashcode est le numéro du véhicule */
			while (rset1.next()) {
				int vehicule = rset1.getInt(1);
				int indice_vehicule = indiceTab(indicesVehicules, vehicule);
				Date dateDepart = rset1.getDate(2);
				Date dateArrivee = rset1.getDate(3);
				int moisDepart = dateDepart.getMonth();
				int moisArrivee = dateArrivee.getMonth();
				long duree = dateArrivee.getTime() - dateDepart.getTime(); // Durée de la location en millisecondes
				Date dateDebutMois = getPremiereDateduMois(dateArrivee); // La première date du mois d'arrivée
				Date dateFinMois = getDerniereDateduMois(dateDepart); // La dernièe date du mois de départ
				long dureeRMD = dateFinMois.getTime() - dateDepart.getTime(); // Durée Restante dans le Mois de Départ
				long dureeRMA = dateArrivee.getTime() - dateDebutMois.getTime(); // Durée Restante dans le Mois d'Arrivée
				if (moisDepart == moisArrivee) {
					// La location se déroule sur un mois : ajout d'un élément à la liste
					ElementListe e = new ElementListe(moisDepart, duree);
					tableau[indice_vehicule].add(e);
				} else {
					// La location couvre plusieurs mois : ajout d'un élément pour moisDepart et d'un autre pour moisArrivee
					ElementListe e1 = new ElementListe(moisDepart, dureeRMD);
					ElementListe e2 = new ElementListe(moisArrivee, dureeRMA);
					tableau[indice_vehicule].add(e1);
					tableau[indice_vehicule].add(e2);
					if (moisArrivee - moisDepart > 1) {
						// La location couvre plus de deux mois : on compte entièrement les mois intermédiaires
						for (int m = moisDepart+1; m<moisArrivee; m++) {
							Calendar cal = Calendar.getInstance();
							cal.setTime(dateDepart);
							cal.add(Calendar.MONTH, m);
							dateDebutMois = getPremiereDateduMois(convertToSQL(cal.getTime()));
							dateFinMois = getDerniereDateduMois(convertToSQL(cal.getTime()));
							ElementListe e = new ElementListe(m, dateFinMois.getTime() - dateDebutMois.getTime());
							tableau[indice_vehicule].add(e);
						}
					}
				}
			}
			
			/* Affichage de la table de hachage
			 * A décommenter pour afficher le contenu */
			
			// System.out.println("Affichage de la table de hachage contenant la durée d'une location selon le mois :");
			//afficherTableau(tableau);
			// System.out.println("");
			
			/* Construction du résultat final sous forme de tableau à double entrée :
			 * le temps moyen d'utilisation selon le mois et le véhicule
			 */
			int moisMin = moisMin(tableau);
			int moisMax = moisMax(tableau);
			int nbMois = moisMax - moisMin + 1;
			long[][] resultat = new long[nbVehicules][nbMois];

			for (int v=0; v<nbVehicules; v++) {
				for (int m=0; m<nbMois; m++) {
					int nbElements = 0;
					for (ElementListe e: tableau[v]) {
						if (e.getMois() == m+moisMin) {
							resultat[v][m] = resultat[v][m] + e.getDuree();
							nbElements += 1;
						}
					}
					if (nbElements > 0) {
						resultat[v][m] = resultat[v][m] / nbElements;
					} else {
						resultat[v][m] = 0;
					}
				}
			}
			
			System.out.println("");
			System.out.println("Les temps moyens d'utilisation par véhicule par mois :");
			System.out.println("");
			
			// Affichage du résultat
			System.out.print("Véhicule \t");
			for (int m=0; m<nbMois; m++) {
				System.out.print(DateFormatSymbols.getInstance().getMonths()[m+moisMin] + "\t\t\t");
			}
			System.out.println("");		
			for (int v=0; v<nbVehicules; v++) {
				System.out.print("Véhicule " + indicesVehicules[v] + "\t");
				for (int m=0; m<nbMois; m++) {
					/* Régler ici l'affichage.
					 * Affichages disponibles : jours, heures, minutes.
					 * Commenter/Décommenter les blocs selon le besoin.
					 */
					
					//System.out.printf("%.2f", ((float)resultat[v][m]/(float)(1000*60*60*24)));
					//System.out.print(" jours\t\t");
					
					System.out.printf("%.2f", ((float)resultat[v][m]/(float)(1000*60*60)));
					System.out.print(" heures\t\t");
					
					//System.out.printf("%.2f", ((float)resultat[v][m]/(float)(1000*60)));
					//System.out.print(" minutes\t\t");
				}
				System.out.println("");
			}
			
			// Fermetures des connexions, requêtes et résultats
			rset1.close();
			rset2.close();
			stmt1.close();
			stmt2.close();
			conn.close();

		} catch (SQLException e) {
			// En cas d'erreur
			System.err.println("Failed!");
			e.printStackTrace(System.err);
		}
	}
	
	/** Affiche le résultat rset d'une commande SQL */
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
	
	/** Renvoie vrai si l'entier entré en paramètre est entré dans le tableau, faux sinon */
	private static boolean estDans(int[] tableau, int element) {
		for (int i=0; i<tableau.length; i++) {
			if (tableau[i] == element) {
				return true;
			}
		}
		return false;
	}
	
	/** Renvoie l'indice de la première occurence de l'élément */
	private static int indiceTab(int[] tableau, int element) {
		for (int i=0; i<tableau.length; i++) {
			if (tableau[i] == element) {
				return i;
			}
		}
		return 100000; // Impossible en théorie
	}
	
	/** Renvoie le plus grand mois contenu dans la table de hachage */
	private static int moisMax(ArrayList<ElementListe> tableau[]) {
		int moisMax = 0;
		for (int v=0; v<tableau.length; v++) {
			for (ElementListe e: tableau[v]) {
				if (moisMax < e.getMois()) {
					moisMax = e.getMois();
				}				
			}
		}
		return moisMax;
	}
	
	/** Renvoie le plus petit mois contenu dans la table de hachage */
	private static int moisMin(ArrayList<ElementListe> tableau[]) {
		int moisMin = 12;
		for (int v=0; v<tableau.length; v++) {
			for (ElementListe e: tableau[v]) {
				if (moisMin > e.getMois()) {
					moisMin = e.getMois();
				}				
			}
		}
		return moisMin;
	}
	
	/** Affiche la table de hachage */
	private static void afficherTableau(ArrayList<ElementListe> tableau[]) {
		for (int v=0; v<tableau.length; v++) {
			System.out.println("Véhicule " + (v+1));
			for (ElementListe e: tableau[v]) {
				System.out.println("Element : Mois = " + (e.getMois()+1) + " Durée = " + e.getDuree() + " millisecondes soit  "+ (float)e.getDuree()/(float)(1000*60*60) + " heures soit " + (float)e.getDuree()/(float)(1000*60*60*24) + " jours.");
			}
		}
	}
	
	/** Renvoie la première date du mois pour la date donnée.
	 * Exemple : Si la date vaut 13/01/2016 à 12:00:00, renvoie la date 01/01/2016 à 00:00:00
	 * @param date La date dont on veut la première date du mois.
	 */
	private static Date getPremiereDateduMois(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return convertToSQL(calendar.getTime());
	}

	/** Renvoie la dernière date du mois pour la date donnée.
	 * Exemple : Si la date vaut 13/01/2016 à 12:00:00, renvoie la date 31/01/2016 à 23:59:59
	 * @param date La date dont on veut la dernière date.
	 */
	private static Date getDerniereDateduMois(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return convertToSQL(calendar.getTime());
	}
	
	/** Convertit un java.util.Date en java.sql.Date */
	public static java.sql.Date convertToSQL(java.util.Date date) {
	    return new java.sql.Date(date.getTime());
	  }
	
	public static void main(String args[]) {
		new TMVehiculeMois();
	}
}

/**
 * Contient le temps d'utilisation d'un véhicule (duree) pour un mois donné (mois), pour une même location.
 */
class ElementListe {
	private int mois; // de 0 à 11
	private long duree; // en millisecondes
	
	public ElementListe(int mois, long duree) {
		setMois(mois);
		setDuree(duree);
	}
	
	public void setMois(int mois) {
		this.mois = mois;
	}
	
	public void setDuree(long duree) {
		this.duree = duree;
	}
	
	public int getMois() {
		return this.mois;
	}
	
	public long getDuree() {
		return this.duree;
	}
}
