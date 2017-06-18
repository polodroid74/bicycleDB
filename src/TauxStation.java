
import java.sql.*;
import java.util.*;
import java.util.Scanner;
import java.text.DecimalFormat;


public class TauxStation {
  // Variables pour la connexion
  static final String CONN_URL = "jdbc:oracle:thin:@ensioracle1.imag.fr:1521:ensioracle1";
  static final String USER = "vincenky"; // A remplacer pour votre compte
  static final String PASSWD = "vincenky";

  static final String PRE_STMT1 = "SELECT IdVehicule FROM Vehicule";
  static final String PRE_STMT2 = "SELECT NomStation FROM Station";
  static final String PRE_STMT3 = "SELECT * FROM Location WHERE IdVehicule=? ORDER BY DateDepart DESC";
  static final String PRE_STMT4 = "SELECT DateDepart, IdVehicule FROM Location WHERE (TRUNC(DateDepart,'DD')=TRUNC(?,'DD') AND NomStationDepart=?) ORDER BY DateDepart";
  static final String PRE_STMT5 = "SELECT DateArrivee, IdVehicule FROM Location WHERE (TRUNC(DateArrivee,'DD')=TRUNC(?,'DD') AND NomStationArrivee=?) ORDER BY DateArrivee";
  static final String PRE_STMT6 = "SELECT SUM(NombrePlaces) FROM PeutContenir WHERE NomStation=?";

  public TauxStation(java.util.Date DAY) {
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

      PreparedStatement stmt2 = conn.prepareStatement(PRE_STMT2);
      ResultSet stationList = stmt2.executeQuery();
      System.out.println("Taux d'occupation pour la journée du " + DAY.toString() +" dans toutes les stations ");
      while (stationList.next()) {
        String nomStation = stationList.getString(1);

        System.out.println("=========="+nomStation+"==========");
        int nbVehiculeAMinuit=0;
        PreparedStatement stmt1 = conn.prepareStatement(PRE_STMT1);
        ResultSet idVlist = stmt1.executeQuery();
        while (idVlist.next()) {
          int idV = idVlist.getInt(1);
          PreparedStatement stmt3 = conn.prepareStatement(PRE_STMT3);
          stmt3.setInt(1, idV);
          ResultSet locationById = stmt3.executeQuery();
          while (locationById.next()) {
            int day = locationById.getDate(2).getDate();
            int month = locationById.getDate(2).getMonth();
            int year = locationById.getDate(2).getYear();
            java.util.Date dateLoc = new java.util.Date(year, month, day);
            if (dateLoc.compareTo(DAY)<0) {
              if (locationById.getDate(3)!=null && locationById.getDate(3).compareTo(DAY)<=0) {
                if (locationById.getString(6).equals(nomStation)){
                  nbVehiculeAMinuit++;
                }
                break;
              } else {
                break;
              }
            }
          }
        }

        /*Calcul du nombre max de véhicules dans la station */
        PreparedStatement stmt4 = conn.prepareStatement(PRE_STMT4);
        stmt4.setDate(1, convertToSQL(DAY));
        stmt4.setString(2, stationList.getString(1));
        ResultSet departureOrderByTime = stmt4.executeQuery();

        PreparedStatement stmt5 = conn.prepareStatement(PRE_STMT5);
        stmt5.setDate(1, convertToSQL(DAY));
        stmt5.setString(2, stationList.getString(1));
        ResultSet arrivalOrderByTime = stmt5.executeQuery();
        /* A CONTINUER !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
        int max = nbVehiculeAMinuit;
        int tmp = nbVehiculeAMinuit;

        arrivalOrderByTime.next();
        departureOrderByTime.next();

        while (!(arrivalOrderByTime.isAfterLast() || departureOrderByTime.isAfterLast())) {
          if (arrivalOrderByTime.getDate(1).compareTo(departureOrderByTime.getDate(1))<0) {
            tmp++;
            arrivalOrderByTime.next();
          } else {
            tmp--;
            departureOrderByTime.next();
          }
          if (tmp>max) {
            max = tmp;
          }
        }
        if(!(arrivalOrderByTime.isAfterLast())) {
          do {
            /* il ne reste que des arrivées, donc le nombre max de véhicule occupé augmente*/
            tmp++;
          } while (arrivalOrderByTime.next());
          if (tmp>max){
            max=tmp;
          }
        } else if (!(departureOrderByTime.isAfterLast())) {
          /* on ne fait rien car le nombre tmp va etre decrementé sans être ensuite incrémenté. */
        }
        PreparedStatement stmt6 = conn.prepareStatement(PRE_STMT6);
        stmt6.setString(1, nomStation);
        ResultSet nombrePlaceTotale = stmt6.executeQuery();
        nombrePlaceTotale.next();

        double taux = ((double) max/(double) nombrePlaceTotale.getInt(1));
        taux = taux*100.0;
        DecimalFormat f = new DecimalFormat();
        f.setMaximumFractionDigits(2);

        System.out.println("cette station à un taux d'occupation de " + f.format(taux) + " % \n==============================\n\n");

      }
		//fin de la connection
		conn.commit();
		conn.close();


    } catch (SQLException e) {
      // En cas d'erreur
      System.err.println("Failed!");
      e.printStackTrace(System.err);
    }
  }

  public static java.sql.Date convertToSQL(java.util.Date date) {
    return new java.sql.Date(date.getTime());
  }

  public static void main(String args[]) {
    Scanner sc = new Scanner(System.in);
    System.out.println("Cette fonctionnalité renvoit le taux d'occupation maximale"
                        +" de chaque station au cours de la journée de votre choix.");
    System.out.println("Entrez une année (ex:2015) : ");
    Integer annee = sc.nextInt();
    System.out.println("Entrez un mois (chiffre entre 1 et 12) : ");
    Integer mois = sc.nextInt();
    System.out.println("Entrez un jour : ");
    Integer jour = sc.nextInt();
    Calendar now = Calendar.getInstance();

    if (mois > 12 || mois < 1) {
			throw new IllegalArgumentException("Entrez un entier entre 1 et 12 inclus pour le mois");
		}

		if (annee < 1000 || annee > now.get(Calendar.YEAR)) {
			throw new IllegalArgumentException("Entrez une année avec 4 chiffres en dessous de l'année courante");
		}

    if (jour < 1 || jour > 31) {
      throw new IllegalArgumentException("Entrez un jour compris dans le mois");
    }

    java.util.Date d = new java.util.Date(annee-1900,
    mois-1,
    jour);
    new TauxStation(d);
  }
}
