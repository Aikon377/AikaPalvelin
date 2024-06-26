// AikaAsiakasAloitus.java
// ottaa yhteyden AikaPalvelin:een, aloittaa yhden sopimisen, mutta ei tee muuta
// TÃ¤mÃ¤ on siis pohja ja malli tehtÃ¤viin 32-34
// tehtÃ¤vÃ¤t 33-34 harjoituksissa 6
// Sokettiohjelmointi kÃ¤sitellÃ¤Ã¤n luennolla ma 22.4.
// Vappuviikon tehtÃ¤vÃ¤t 33-35 julkaistaan myÃ¶s jo maanantaina.

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class luoAsiakas {

    private static String tunnus = null;
    private static int viive = -1;

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

        // komentoriviltÃ¤ voidaan lukea myÃ¶s sopimistunnus ja pÃ¤Ã¤tÃ¶ksentekoviive
        if (args.length > 2) {
            tunnus = args[2];
        }

        if (args.length > 3) {
            viive = Integer.parseInt(args[3]);
        }


        luoAsiakas a = new luoAsiakas();

        a.aloitaSopiminen(palvelimenOsoite, portti);

    } // main()


    // yhteydenotto ja keskustelun kutsuminen
    private boolean aloitaSopiminen(String osoite, int portti) {

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

            while (viive <= 0) {
                System.out.println("Anna pÃ¤Ã¤tÃ¶sviive sekunteina:");
                String viiveMjono = kayttaja.readLine();
                viive = Integer.parseInt(viiveMjono);
            };

            // lÃ¤hetetÃ¤Ã¤n aloitusviesti palvelimelle
            out.print(AikaProto.A_ALOITA + " " + tunnus + " " + viive + AikaProto.EOL);
            out.flush();

            // luetaan vastaus ja tarkistetaan tulos
            String vastaus = in.readLine();

            if (vastaus.startsWith("2"))
                System.out.println("Ajansopiminen kÃ¤ynnistetty");
            else
                System.out.println("Ajansopimisen kÃ¤ynnistys ei onnistunut: " + vastaus);

            sock.close();


            // poikkeusten kÃ¤sittely
        } catch (Exception e) {
            System.err.println("" + e);
            try {
                sock.close();  // suljetaan varuilta vielÃ¤ tÃ¤Ã¤llÃ¤kin
            } catch (Exception ignored) { }
            return false;
        } // catch

        return true;

    }   // otaYhteys()

}