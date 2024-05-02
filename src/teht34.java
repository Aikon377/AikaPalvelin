import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class teht34 {


    private static String tunnus = null;
    private static int viive = -1;

    private static List<Integer> ajat = null;

    public Socket otaYhteys(String osoite, int portti){

        System.out.println("Otetaan yhteyttä palvelimeen...");
        // yhteydenotto
        Socket sock = null;
        try {
            sock = new Socket(osoite, portti);     // yhteydenotto
            System.out.println("Yhteys onnistui");
            return sock;
        } catch (Exception e) {
            // yhteys ei varmaankaan onnistunut
            System.err.println("" + e);
            return null;
        }

    }

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




        teht34 teht34 = new teht34();
        teht34.lisaaAjat(palvelimenOsoite, portti);



    } // main()

    static class kuuntelija implements Runnable {
        private BufferedReader reader;

        public kuuntelija(InputStream inputStream) {
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public void run() {
            try {
                String vastaus;
                while ((vastaus = reader.readLine()) != null){
                    System.out.println("Saatiin vastaus: " + vastaus);
                    vastaus = null;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean kysySopimisia(Socket socket){
        if (!socket.isConnected())
            try {
                socket = connectSocket(AikaProto.A_OLETUSOSOITE, AikaProto.A_OLETUSPORTTI);
            } catch (Exception e) {
                System.err.println(e);
                return false;
            }
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.print(AikaProto.A_ONKOSOPIMISIA + " " + AikaProto.EOL);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            System.out.println("Vastaus: " + response);

        } catch (IOException e) {
            System.err.println("" + e);
            return false;
        }
        return true;
    }



    // yhteydenotto ja keskustelun kutsuminen
    private boolean lisaaAjat(String osoite, int portti) {

        Socket sock = otaYhteys(osoite, portti);


        try {
            Thread kuuntelijaThread = new Thread(new kuuntelija(sock.getInputStream()));
           // kuuntelijaThread.start();

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

            kysySopimisia(sock);
            System.out.println("Kysytäänkö päätös? Y/N: ");
            String vastausPaatos = kayttaja.readLine();


            if (vastausPaatos.equals("y")) {
                kysyPaatos(tunnus, sock);
            }
            else {
                sock.close();
            }

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

    public boolean kysyPaatos(String tunnus, Socket socket) throws IOException {
        if (!socket.isConnected())
            try {
                socket = connectSocket(AikaProto.A_OLETUSOSOITE, AikaProto.A_OLETUSPORTTI);
            } catch (Exception e) {
                System.err.println(e);
                return false;
            }
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.print(AikaProto.A_PAATOS + " " + tunnus + " " + AikaProto.EOL);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            System.out.println("Vastaus: " + response);

        } catch (IOException e) {
            System.err.println("" + e);
            return false;
        }
        return true;
    }

    public Socket connectSocket(String osoite, int portti){
        // yhteydenotto
        Socket sock = null;
        try {
            sock = new Socket(osoite, portti);     // yhteydenotto
            System.out.println("Yhteys onnistui");
            return sock;
        } catch (Exception e) {
            // yhteys ei varmaankaan onnistunut
            System.err.println("" + e);
            return sock;
        }
    }


}
