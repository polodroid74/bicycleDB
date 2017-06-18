import java.util.Scanner; //permet de lire les entrées de l'utilisateur
import java.sql.*;
import java.text.DecimalFormat;

public class Facturation {
	
	static final String CONN_URL = "jdbc:oracle:thin:@ensioracle1.imag.fr:1521:ensioracle1";
    static final String USER = "vincenky";
    static final String PASSWD = "vincenky";
	// Recherche de la date d'arrivée de la location et calcul de sa durée
	static final String PRE_STMT1 ="select DateArrivee,(DateArrivee -DateDepart)*24, NumeroCB from Location where IDVehicule = ? and DateDepart = TO_DATE(?,'yyyy-mm-dd hh24:mi') ";

	// recherche categorie du vehicule
	static final String PRE_STMT2 = "select v.Categorie, c.DureeMax, c.PrixHoraire, c.MontantCaution"
			+ " from Vehicule v, Categorie c where c.Categorie = v.Categorie and IdVehicule = ?";

	// recherche du numeroforfait
	static final String PRE_STMT3 = "select NumeroForfait from AffectationForfait where NumeroCB=? and Categorie =?";
	
	// Recherche du type du forfait
	static final String PRE_STMT4 = "select Duree, Remise from ForfaitIllimite where NumeroForfait = ?";
	static final String PRE_STMT5 = "select Prix, NbMaxLoc from ForfaitLimite where NumeroForfait = ?";
	
	//calcul age utilisateur
	static final String PRE_STMT8 ="select NomAbonne from Abonnes where NumeroCB=? AND ((current_date - DateNaissance)/365)  NOT BETWEEN 25 and 65";
	
	//Vérification de la validité du forfait pour la location à facturer 
	
	//cas forfait illimité un jour
	static final String PRE_STMT9= "select (DateDebut-TRUNC(?,'DDD')), (DateDebut-TRUNC(TO_DATE(?,'yyyy-mm-dd hh24:mi'),'DDD')) from ForfaitIllimite where NumeroForfait=?";
	//cas forfait illimité un mois
	static final String PRE_STMT10= "select (LAST_DAY(DateDebut)-TRUNC(?,'DDD')), DateDebut-TRUNC(TO_DATE(?,'yyyy-mm-dd hh24:mi'),'DDD') from ForfaitIllimite where NumeroForfait=?";
	//case forfait un ans
	static final String  PRE_STMT11="select LAST_DAY(ADD_MONTHS(TRUNC(DateDebut,'YEAR'), 11))-TRUNC(?,'DDD'), DateDebut-TRUNC(TO_DATE(?,'yyyy-mm-dd hh24:mi'),'DDD') from ForfaitIllimite where NumeroForfait=?";
	
	
	
	
	float remise = (float)1.0; //par défault, pas de remise
	float prixH = (float)0.0;
	float caution= (float)0.0; //la caution à payer vaut initialement 0
	float dureeLocation=(float)0.0;
	String categorie ="init";
	int dureeMax =0;
	int ncb;
	Date dateArrivee=null;

	/**
	 * @param ncb
	 * @param ID
	 * @param dateDepart
	 */
	public Facturation( int ID, String dateDepart) {
		super();
		try {
			// Enregistrement du driver Oracle

            System.out.print("Loading Oracle driver... "); 
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection

            System.out.print("Connecting to the database... "); 
            Connection conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            conn.setAutoCommit(false); // Autocommit en false : toujours !
            conn.setTransactionIsolation(conn.TRANSACTION_SERIALIZABLE); // Isolation des transactions
            System.out.println("connected");
            
			//execution de la première requete : recherche date arrivée et durée de la location
			
			PreparedStatement stmt = conn.prepareStatement(PRE_STMT1);
			stmt.setInt(1, ID);
			stmt.setString(2, dateDepart);
			ResultSet rset = stmt.executeQuery();
			if (rset.next() && rset.getDate(1) != null) {
				//Ici, on est sur que la location demandée existe
				System.out.println("-----------------FACTURE---------------");
				dureeLocation = rset.getFloat(2); //en jours
				//sauvegarde de la durée de la location à laquelle on va enlever la première heure gratuite
				//Ainsi, on pourra verifier si la durée maximale autorisée est dépassée
				Float tmp =dureeLocation; 
				dateArrivee=rset.getDate(1);
				ncb=rset.getInt(3);
				//on enlève la première heure qui est gratuite
				if(dureeLocation*(float)24 <(float)1){
					dureeLocation =(float)0.0;
				}else{ //1ere heure gratuite
					dureeLocation = dureeLocation -(float)1;
				}
				stmt.close();
				rset.close();
				// recherche categorie du vehicule
				stmt = conn.prepareStatement(PRE_STMT2);
				stmt.setInt(1, ID);
				rset = stmt.executeQuery();
				if(rset.next()){
					categorie = rset.getString(1); // categorie du véhicule loué
					dureeMax = rset.getInt(2);
					prixH = rset.getFloat(3);
					// verifier que le durée maximale n'est pas dépassée
					if ((float) dureeMax*24 <= tmp) {
						caution = rset.getFloat(4);
					}
				}
				System.out.println(" Catégorie du véhicule loué : "+categorie+
						"\n Durée Maximale autorisée (en heures) : " +dureeMax*24+
						"\n Durée de la location (en heures) :" +tmp+
						"\n Prix Horaire = " + prixH + " €"+
						"\n Caution à payer = " +caution +" €");
				stmt.close();
				rset.close();

				// recherche du NumeroForfait
				stmt = conn.prepareStatement(PRE_STMT3);
				stmt.setInt(1, ncb);
				stmt.setString(2, categorie);
				rset = stmt.executeQuery();

				if (rset.next()) {
					//Ici, l'utilisateur possède bien un forfait
					Integer numeroForfait = rset.getInt(1);
					stmt.close();
					rset.close();
					// recherche du type de forfait
					stmt = conn.prepareStatement(PRE_STMT4);
					stmt.setInt(1, numeroForfait);
					rset = stmt.executeQuery();
					if (rset.next()) { 
						// forfait de type locations illimitées
						String duree =rset.getString(1);
						float remisetmp= rset.getFloat(2);
						stmt.close();
						rset.close();			
						switch(duree){					
						case "Jour":
							stmt = conn.prepareStatement(PRE_STMT9);
							break;
						case "Mois":
							stmt = conn.prepareStatement(PRE_STMT10);
							break;
						case "Annee":
							stmt = conn.prepareStatement(PRE_STMT11);
							break;
						default :
							stmt = conn.prepareStatement(PRE_STMT9); //cas par default, forfait le plus court (JOUR)		
						}
						stmt.setDate(1,dateArrivee);
						stmt.setString(2, dateDepart);
						stmt.setInt(3, numeroForfait);
						rset=stmt.executeQuery();
						if(rset.next()){
							//test validité du forfait pour la location considérée
							if (rset.getFloat(1) >= (float)0.0 && rset.getFloat(2) <= (float)0.0 ){
								remise=(float)1-remisetmp;	
								System.out.println(" Forfait illimite en cours de validité. Remise = " + remise);
								
							}else{
								System.out.println(" Forfait illimité non valide pour la location");
							}
						}
						stmt.close();
						rset.close();
					} else { 
						//forfait de type locations limitées
						stmt.close();
						rset.close();
						stmt = conn.prepareStatement(PRE_STMT5);
						stmt.setInt(1, numeroForfait);
						rset = stmt.executeQuery();
						if (rset.next()) {
							if (rset.getInt(2) != 0) { 
								// la location est gratuite remise=0
								remise = (float) 0.0;
								System.out.println("\n Forfait limité en cours de validité. Location gratuite");
							}else{
								System.out.println("Fofait limité épuisé. Veuillez le recharger.");
							}
						}
						stmt.close();
						rset.close();

					}
				} else {
					// fermeture de la requête recherchant le numero du numeroForfait
					// dans le cas où l'on a pas de forfait
					stmt.close();
					rset.close();
				}
				//calcul de l'age utilisateur
				stmt = conn.prepareStatement(PRE_STMT8);
				stmt.setInt(1, ncb);
				rset = stmt.executeQuery();
				if (rset.next()){
					//l'utilisateur a le droit à la remise
					remise = ((float)0.75)*(remise);
					System.out.println(" Reduction 25/65 ans : OK");
				}
				stmt.close();
				rset.close();
				//calculdu coût de la location
				double cout = remise * dureeLocation * prixH + caution;
				DecimalFormat f = new DecimalFormat();
				f.setMaximumFractionDigits(2);
				
				System.out.println("\n La location coûte = " + f.format(cout) + " €");
			}else{
			System.out.println("Pas de location existante !");
			}
			//fin de la connection
			conn.commit();
			conn.close();

		} catch (SQLException e) {
			System.err.println("failed");
			e.printStackTrace(System.err);
		}
	}

	public static void main(String args[]) {
		//récupération des informations de la location
		Scanner sc = new Scanner(System.in);
		System.out.println("Quel est l'ID du véhicule loué ?");
		int ID = Integer.parseInt(sc.nextLine());
		System.out.println("Quelle est votre date de départ (YYYY-MM-DD HH24:MM) ?");
		String date = sc.nextLine();
		System.out.println("\n-------------------------------------------------------------------------------");
		//execution du calcul du coût
		new Facturation(ID, date);
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


}
