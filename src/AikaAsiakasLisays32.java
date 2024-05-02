// AikaAsiakasLisaus.java
// TehtÃ¤vÃ¤ 32

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class AikaAsiakasLisays32 {

    private static String tunnus = null;

    private static List<Integer> ajat = null;

    public static void main(String[] args) {

        // oletusarvot
        String palvelimenOsoite = "localhost";
        int portti = AikaProto.A_OLETUSPORTTI;

        if (args.length > 0) {
            palvelimenOsoite = args[0];
        }

        if (args.length > 1) {
            portti = Integer.parseInt(args[1]);
        }

        // komentoriviltÃ¤ voidaan lukea myÃ¶s sopimistunnus ja sopivat ajat
        if (args.length > 2) {
            tunnus = args[2];
        }

        if (args.length > 3) {
            ajat = new ArrayList<>(args.length);
            for (int i = 3; i < args.length; i++)
                ajat.add(Integer.valueOf(Integer.parseInt(args[i])));
        }


        AikaAsiakasLisays32 a = new AikaAsiakasLisays32();

        a.lisaaAjat(palvelimenOsoite, portti);

    } // main()


    // yhteydenotto ja keskustelun kutsuminen
    private boolean lisaaAjat(String osoite, int portti) {

        // yhteydenotto
        Socket sock = null;
        try {
            sock = new Socket(osoite, portti);     // yhteydenotto
            System.out.println("Yhteys onnistui");
        } catch (Exception e) {
            // yhteys ei varmaankaan onnistunut
            System.err.println("" + e);
            return false;
        }

        try {

            // luodaan keskustelukanavat palvelimeen
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // kÃ¤yttÃ¤jÃ¤
            BufferedReader kayttaja = new BufferedReader(
                    new InputStreamReader(System.in));

            // luetaan tiedot kÃ¤yttÃ¤jÃ¤ltÃ¤ jollei niitÃ¤ saatu komentoriviltÃ¤
            if (tunnus == null) {
                System.out.println("Anna ajansopimisen tunnus:");
                tunnus = kayttaja.readLine().trim();
            }

            if (ajat == null)  {
                System.out.println("Anna sopivat ajat merkkijonolla erotettuina:");
                String aikaMjono = kayttaja.readLine();
                ajat = AikaProto.stringToList(aikaMjono);
            }

            // lÃ¤hetetÃ¤Ã¤n aloitusviesti palvelimelle
            out.print(AikaProto.A_SOPIVAT + " " + tunnus + " " +
                    AikaProto.listToString(ajat) + AikaProto.EOL);
            out.flush();

            // luetaan vastaus ja tarkistetaan tulos
            String vastaus = in.readLine();

            if (vastaus.startsWith("2"))
                System.out.println("Ajat lÃ¤hetetty");
            else
                System.out.println("Aikojen lÃ¤hetys ei onnistunut: " + vastaus);

            sock.close();


            // poikkeusten kÃ¤sittely
        } catch (Exception e) {
            System.err.println("" + e);
            if (sock != null)
                try {
                    sock.close();  // suljetaan varuilta vielÃ¤ tÃ¤Ã¤llÃ¤kin
                } catch (Exception e2) {
                }

            return false;
        } // catch

        return true;

    }   // otaYhteys()

}