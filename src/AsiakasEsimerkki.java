// AsiakasEsimerkki.java SJ
// Ottaa yhteyden PalvelinEsimerkki:in.

import java.net.*;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;


public class AsiakasEsimerkki {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("KÃ¤yttÃ¶: java AsiakasEsimerkki " +
                    "palvelinosoite [portti]");
            return;
        }

        int portti = 1234;
        if (args.length > 1) {
            portti = Integer.valueOf(args[1]);
        }


        AsiakasEsimerkki a = new AsiakasEsimerkki();

        // yksi viesti suuntaansa
        boolean stat = a.keskustele(args[0], portti);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {}

        if (stat)
            // usea viesti
            a.keskustele2(args[0], portti);


    } // main()


    // simppeli keskustelu, palauttaa toden jos onnistui
    public boolean keskustele(String osoite, int portti) {

        Socket s = null;

        // yhteydenotto
        try {
            s = new Socket(osoite, portti);     // yhteydenotto
            System.out.println("Yhteys onnistui");
        } catch (Exception e) {
            // yhteys ei varmaankaan onnistunut
            System.err.println(e);
            return false;
        }

        try {

            // luodaan keskustelukanavat
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(s.getInputStream()));

            // lÃ¤hetÃ¤ viesti
            out.println(5);

            // vastaanota viesti
            System.out.println(in.readLine());

            // suljetaan yhteys
            s.close();
            s = null;

            // poikkeusten kÃ¤sittely
        } catch (Exception e) {
            System.err.println(e);
            if (s != null)
                try {
                    s.close();  // suljetaan varuilta vielÃ¤ tÃ¤Ã¤llÃ¤kin
                } catch (Exception e2) {}

            return false;
        } // catch

        return true;

    }   // keskustele()


    // toinen asiakas, lukee tiedot kÃ¤yttÃ¤jÃ¤ltÃ¤, monta kyselyÃ¤/asiakas
    public void keskustele2(String osoite, int portti) {

        Socket s = null;

        Scanner kayttaja = new Scanner(System.in);
        PrintWriter out = null;
        BufferedReader in = null;

        // yhteydenotto
        try {
            s = new Socket(osoite, portti);     // yhteydenotto
            System.out.println("Yhteys onnistui");

            // luodaan keskustelukanavat
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));

        } catch (Exception e) {
            // yhteys ei varmaankaan onnistunut
            System.err.println(e);
            return;
        }

        try {

            while (kayttaja.hasNext()) {

                if (! kayttaja.hasNextInt())
                    break;  // muu kuin kokonaisluku lopettaa

                int x = kayttaja.nextInt();

                // lÃ¤hetÃ¤ viesti
                out.println(x);

                // vastaanota viesti
                String rivi = in.readLine();
                if (rivi == null) {
                    System.out.println("Palvelin solki yhteyden");
                    break;
                }
                System.out.println(rivi);

            }

            // suljetaan yhteys
            s.close();
            s = null;

            // poikkeusten kÃ¤sittely
        } catch (Exception e) {
            System.err.println(e);
            if (s != null)
                try {
                    s.close();  // suljetaan varuilta vielÃ¤ tÃ¤Ã¤llÃ¤kin
                } catch (Exception e2) {}

        } // catch

    }   // keskustele2()



}