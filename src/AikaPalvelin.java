// AikaPalvelin.java
// palvelin tehtÃ¤viin 31-35
// tehtÃ¤vÃ¤Ã¤n 35 ota pois kommentit pÃ¤Ã¤tÃ¶ksen puskemisesta metodista
// Sopiminen.teePaatos()


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class AikaPalvelin {


    private final HashSet<Yhteys> aktiivisetYhteydet = new HashSet<>();

    // Aktiiviset asiakkaat (sÃ¤ikeet)
    // Kaikki sopimiset
    private final HashMap<String, Sopiminen> sopimiset = new HashMap<>();
    Timer timer = new Timer(true);
    // kuunteleva palvelinsoketti
    private ServerSocket ss = null;


    // konstruktorit avaavat yhteyden kuuntelulle
    private AikaPalvelin(int portti) {

        try {
            ss = new ServerSocket(portti);
            System.out.println("Kuunnellaan porttia " + portti);
        } catch (Exception e) {
            System.err.println("" + e);
            ss = null;
        }
    }

    public static void main(String[] args) {

        AikaPalvelin p;

        if (args.length > 0)
            p = new AikaPalvelin(Integer.parseInt(args[0]));
        else
            p = new AikaPalvelin(AikaProto.A_OLETUSPORTTI);

        p.odotaYhteyksia();

    } // main()

    /**
     * kuuntelu "sÃ¤ie", pÃ¤Ã¤ohjelman elinkaari
     */
    private void odotaYhteyksia() {

        if (ss == null)
            return;

        BufferedReader kayttaja = new BufferedReader(new InputStreamReader(System.in));

        try {
            ss.setSoTimeout(1 * 1000);

            System.out.println("Paina ENTER lopettaaksesi palvelimen");

            while (true) {

                // odotetaan uutta yhteyttÃ¤, vÃ¤lillÃ¤ kÃ¤ydÃ¤Ã¤n katsomassa kÃ¤yttÃ¤jÃ¤Ã¤
                Socket cs = null;
                try {
                    cs = ss.accept();
                } catch (SocketTimeoutException ignored) { }

                if (kayttaja.ready()) {
                    lopeta();
                    return;
                }
                if (cs == null) // jos tultiin accept:stÃ¤ aikakatkaisulla
                    continue;

                // luodaan ja kÃ¤ynnistetÃ¤Ã¤n uusi palvelijasÃ¤ie

                // saatiin uusi yhteydenotto, perustetaan ja kÃ¤ynnistetÃ¤Ã¤n sÃ¤ie
                Yhteys uusiYhteys = new Yhteys(cs);
                aktiivisetYhteydet.add(uusiYhteys);
                uusiYhteys.setDaemon(true);
                uusiYhteys.start();
            }
        } catch (Exception e) {
            System.err.println("" + e);
            ss = null;
        }
    }   // kuuntele()


    /**
     * Uuden sopimisen aloittaminen.
     *
     * @param komento koko syÃ¶terivi joka on saatu kÃ¤yttÃ¤jÃ¤ltÃ¤
     * @param yhteys  yhteys jonka kautta pyyntÃ¶ on saatu
     * @return vastaus kÃ¤yttÃ¤jÃ¤lle
     */
    String aloitaSopiminen(String komento, Yhteys yhteys) {

        // try-catch jos Scanner havaitsee virheitÃ¤
        try {
            Scanner sc = new Scanner(komento);

            sc.next(); // komento
            String tunnus = sc.next().trim();
            int viive = sc.nextInt();

            System.out.println("aloitaSopiminen: tunnus=" + tunnus + " viive=" + viive);

            if (tunnus.length() < 1)
                return AikaProto.A_VIRHE + " tyhjÃ¤ tunnus";
            if (sopimiset.get(tunnus) != null)
                return AikaProto.A_VIRHE + " sopimistunnus on jo kaytÃ¶ssÃ¤";

            // uusi sopiminen
            Sopiminen sop = new Sopiminen(tunnus, viive);
            sopimiset.put(tunnus, sop);

            sop.yhteydet.add(yhteys);
            aktiivisetYhteydet.add(yhteys);
            return AikaProto.A_OK;

        } catch (NoSuchElementException e) {
            System.out.println("aloitaSopiminen epÃ¤onnistui: " + e);
            return AikaProto.A_VIRHE;
        }
    }

    /**
     * Aikojen lisÃ¤Ã¤minen yhteen sopimiseen
     *
     * @param komento koko syÃ¶terivi joka on saatu kÃ¤yttÃ¤jÃ¤ltÃ¤
     * @param yhteys  yhteys jonka kautta pyyntÃ¶ on saatu
     * @return vastaus kÃ¤yttÃ¤jÃ¤lle
     */
    String lisaaAjat(String komento, Yhteys yhteys) {

        try {
            Scanner sc = new Scanner(komento);

            sc.next(); // komento
            String tunnus = sc.next();

            if (tunnus.length() < 1)
                return AikaProto.A_VIRHE + " tyhjÃ¤ sopimistunnus";
            if (!sopimiset.containsKey(tunnus))
                return AikaProto.A_VIRHE + " sopimistunnusta ei ole";


            List<Integer> l = new ArrayList<>();
            while (sc.hasNextInt())
                l.add(sc.nextInt());

            if (l.size() < 1)
                return AikaProto.A_VIRHE + " tyhjÃ¤ sopivien aikojen lista";

            System.out.println("lisaaAjat: tunnus=" + tunnus + " ajat=" + l);

            Sopiminen sop = sopimiset.get(tunnus);
            sop.lisaaAjat(l);

            sop.yhteydet.add(yhteys);
            aktiivisetYhteydet.add(yhteys);

            return AikaProto.A_OK;
        } catch (NoSuchElementException e) {
            System.out.println("lisaaAjat epÃ¤onnistui: " + e);
            return AikaProto.A_VIRHE;
        }

    }

    /**
     * PÃ¤Ã¤tÃ¶ksen pyytÃ¤minen
     *
     * @param komento koko syÃ¶terivi joka on saatu kÃ¤yttÃ¤jÃ¤ltÃ¤
     * @param yhteys  yhteys jonka kautta pyyntÃ¶ on saatu
     * @return vastaus kÃ¤yttÃ¤jÃ¤lle
     */
    String haePaatos(String komento, Yhteys yhteys) {
        System.out.println("Päätös: ");

        try {
            Scanner sc = new Scanner(komento);

            sc.next(); // komento
            String tunnus = sc.next();

            if (tunnus.length() < 1)
                return AikaProto.A_VIRHE + " tyhjÃ¤ sopimistunnus";
            if (!sopimiset.containsKey(tunnus))
                return AikaProto.A_VIRHE + " sopimistunnusta ei ole";

            Sopiminen s = sopimiset.get(tunnus);

            System.out.println("haePaatos: tunnus=" + tunnus + " paatos=" + s.haePaatos());
            s.yhteydet.add(yhteys);
            return s.haePaatos();

        } catch (NoSuchElementException e) {
            System.out.println("haePaatos epÃ¤onnistui: " + e);
            return AikaProto.A_VIRHE;
        }

    }


    private void lopeta() {
        // sykronoidaan lÃ¤hetys jotta toisaalta uusia kÃ¤yttÃ¤jiÃ¤ ei lisÃ¤ttÃ¤isi
        // kesken lÃ¤pikÃ¤ynnin ja toisaalta jotta kaikki saisivat viestit
        // samassa jÃ¤rjestyksessÃ¤
        System.out.println("LÃ¤hetetÃ¤Ã¤n kaikille lopetusviesti");
        synchronized (this) {
            for (Yhteys kohde : aktiivisetYhteydet) {
                kohde.lahetaViesti(AikaProto.A_LOPPU);
            }
        }
        // odotetaan hetki jotta asiakkaat vastaavat
        try {
            Thread.sleep(1 * 1000);
        } catch (Exception ignored) {
        }

        for (Yhteys kohde : aktiivisetYhteydet) {
            kohde.sulje();
        }

    }


    // TODO nÃ¤mÃ¤ ei ole kÃ¤ytÃ¶ssÃ¤
    private void lahetaViestiKaikille(String viesti, String lahettaja) {
        // sykronoidaan lÃ¤hetys jotta toisaalta uusia kÃ¤yttÃ¤jiÃ¤ ei lisÃ¤ttÃ¤isi
        // kesken lÃ¤pikÃ¤ynnin ja toisaalta jotta kaikki saisivat viestit
        // samassa jÃ¤rjestyksessÃ¤
        System.out.println("Viesti kaikille: " + lahettaja + " > " + viesti);
        synchronized (this) {
            for (Yhteys kohde : aktiivisetYhteydet) {
                if (lahettaja == null || !lahettaja.equals(kohde.nimi))
                    kohde.lahetaViesti(AikaProto.A_SOPIVAT + " " + lahettaja + AikaProto.EOL + viesti);
            }
        }
    }

    private void poistaYhteys(Yhteys p) {
        System.out.println("Poistetaan " + p.nimi);
        synchronized (this) {
            aktiivisetYhteydet.remove(p);
        }
    }

    private void lisaaYhteys(Yhteys p) {
        System.out.println("Lisataan " + p.nimi);
        synchronized (this) {
            aktiivisetYhteydet.add(p);
        }
    }


    private boolean onkoVarattu(String nimi) {
        synchronized (aktiivisetYhteydet) {
            for (Yhteys kohde : aktiivisetYhteydet) {
                if (nimi.equals(kohde.nimi))
                    return true;
            }
            return false;
        }
    }


    /**
     * Luokka johon paketoidaan yhden ajansopimisen (yksilÃ¶itynÃ¤ sopimistunnuksella) sopimiset
     */
    class Sopiminen {

        String sopimistunnus;
        long paatoshetki;
        boolean paatosTehty = false;
        int paatos = -2;

        // kaikille sopivat ajat
        NavigableSet<Integer> kaikilleSopivat = null;

        // tÃ¤hÃ¤n sopimiseen kÃ¤ytetyt yhteydet (joista osa on voinut jo katketa)
        Set<Yhteys> yhteydet = new HashSet<>();

        /**
         * Luo uuden sopimisen jonka pÃ¤Ã¤tÃ¶s tehdÃ¤Ã¤n viive sekunnin kuluttua
         *
         * @param tunnus sopimistunnus
         * @param viive   viive pÃ¤Ã¤tÃ¶ksen tekemiseen, sekunteina
         */
        public Sopiminen(String tunnus, int viive) {
            sopimistunnus = tunnus;
            paatoshetki = System.currentTimeMillis() + 1000L * viive;

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    teePaatos();
                }
            }, 1000L * viive);
        }


        /**
         * Tekee pÃ¤Ã¤tÃ¶ksen valiten kaikille sopivista ajoista.
         */
        synchronized void teePaatos() {
            if (kaikilleSopivat == null || kaikilleSopivat.isEmpty())
                paatos = -1;    // ei aikoja (ollutkaan tai jÃ¤ljellÃ¤)
            else
                paatos = kaikilleSopivat.first();
            paatosTehty = true;

            System.out.println("teePaatos: tunnus=" + sopimistunnus + " paatos=" + paatos);

            // lÃ¤hetetÃ¤Ã¤n pÃ¤Ã¤tÃ¶s tÃ¤hÃ¤n ajansopimiseen liittyville yhteyksille (jos
            // ne vielÃ¤ ovat aktiivisia)
            // tÃ¤mÃ¤ ei koske tehtÃ¤viÃ¤ 32-34.
            // kun testaat tehtÃ¤vÃ¤Ã¤ 35, niin ota kommentit tÃ¤stÃ¤ pois
/*
            for (Yhteys y : yhteydet)
                if (aktiivisetYhteydet.contains(y))
                    y.lahetaPaatos(paatos);

 */
        }

        /**
         * Palauttaa pÃ¤Ã¤tÃ¶ksen lÃ¤hetettÃ¤vÃ¤nÃ¤ merkkijonona
         *
         * @return virhe tai aika
         */
        synchronized String haePaatos() {
            if (!paatosTehty)
                return AikaProto.A_KESKEN;
            return AikaProto.A_PAATOS + " " + paatos;
        }

        /**
         * LisÃ¤Ã¤ ajansopimiseen yhden kÃ¤yttÃ¤jÃ¤n sopivat ajat
         *
         * @param sopivatAjat sopivat ajat listana
         */
        synchronized void lisaaAjat(List<Integer> sopivatAjat) {
            if (kaikilleSopivat == null) {  // ensimmÃ¤inen lisÃ¤Ã¤jÃ¤
                kaikilleSopivat = new TreeSet<>(sopivatAjat);
            } else  // myÃ¶hempi lisÃ¤Ã¤jÃ¤
                kaikilleSopivat.retainAll(sopivatAjat);
        }

    } // class Sopiminen


    /**
     * niputettuna yhden aikaasiakkaan yhteyden ja sÃ¤ie joka hanskaa toiminnan yhden asiakkaan suuntaan
     */
    class Yhteys extends Thread {

        Socket asiakas = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String nimi = "";
        boolean lopeta = false;

        Yhteys() {
            super();
        }

        Yhteys(Socket cs) {
            super();
            asiakas = cs;
            nimi = asiakas.getInetAddress() + " " + asiakas.getPort();
        }

        /**
         * LÃ¤hettÃ¤Ã¤ pÃ¤Ã¤tÃ¶ksen yhdelle asiakkaalle
         *
         * @param paatos pÃ¤Ã¤tettu aika
         */
        void lahetaPaatos(int paatos) {
            try {
                lahetaViesti(AikaProto.A_PAATOS + " " + paatos);
            } catch (Exception e) {
                // TODO
            }
        }


        /**
         * Sulkee asiakasyhteyden
         */
        void sulje() {

            try {
                lopeta = true;
                asiakas.close();
            } catch (IOException ignored) {
            }
        }

        @Override
        public void run() {

            if (asiakas == null || asiakas.isClosed())
                return;

            try {

                // uuden asiakkaan kÃ¤sittely

                System.out.println("Uusi yhteys: " + asiakas.getInetAddress() +
                        ":" + asiakas.getPort());

                // virrat kÃ¤yttÃ¶kelpoiseen muotoon
                in = new BufferedReader(new InputStreamReader(asiakas.getInputStream()));
                out = new PrintWriter(asiakas.getOutputStream(), true);

                while (true) {

                    // luetaan rivi kerrallaan, tunnistetaan komento, kutsutaan
                    // asianmukaista toimintoa

                    String komento = in.readLine();
                    komento = komento.trim();
                    String vastaus = "";

                    System.out.println("Saatiin viesti: \"" + komento + "\"");

                    if (komento.startsWith(AikaProto.A_ALOITA)) {
                        vastaus = aloitaSopiminen(komento, this);
                    } else if (komento.startsWith(AikaProto.A_SOPIVAT)) {
                        vastaus = lisaaAjat(komento, this);
                    } else if (komento.startsWith(AikaProto.A_PAATOS)) {
                        vastaus = haePaatos(komento, this);

                    }
                    else if (komento.startsWith(AikaProto.A_ONKOSOPIMISIA)){
                        vastaus = String.valueOf(sopimiset);
                    }
                    else
                        vastaus = AikaProto.A_VIRHE;

                    // lÃ¤hetetÃ¤Ã¤n asiakkaalle toimnnon mukainen vastaus

                    lahetaViesti(vastaus);

                }

                // TODO ei poistu kovin kauniisti
            } catch (Exception e) {
                // System.err.println("Yhteys.run: " + e);
            } finally {
                sulje();
                poistaYhteys(this);
            }


        }   // run()


        // lÃ¤hettÃ¤Ã¤ valmiin viestin
        // tÃ¤tÃ¤ kÃ¤yttÃ¤Ã¤ sekÃ¤ oma sÃ¤ie, ettÃ¤ muut (lahetaKaikille)
        synchronized void lahetaViesti(String viesti) {
            System.out.println("LÃ¤hetetÃ¤Ã¤n viesti: \"" + viesti + "\"");
            out.print(viesti.trim());
            out.print(AikaProto.EOL);
            out.flush();
        }

    } // class Yhteys

}   // class AikaPalvelin
