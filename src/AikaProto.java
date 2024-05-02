import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Rajapinta joka esittelee muutaman merkkijonovakion jota ajansopimisessa kÃ¤ytetÃ¤Ã¤n.
 *
 * KÃ¤ytettÃ¤vÃ¤ protokolla:
 *
 * Kukin komento pÃ¤Ã¤ttyy rivinvaihtoon (CRLF) vaikka sitÃ¤ ei alla aina mainita.
 * Asiakas ottaa TCP-yhteyden palvelimen porttiin (oletusarvoisesti 2024).
 *
 * Ajansopimisen aloittaminen:
 *
 * Asiakas lÃ¤hettÃ¤Ã¤ viestin "ALOITATAPAAMINEN: sopimistunnus viive"
 *  missÃ¤ sopimistunnus on merkkijono ja viive on aika sekunteina jonka
 *  jÃ¤lkeen ajansopiminen sulkeutuu.
 * Palvelin vastaa 200 OK tai 5-alkuinen virhekoodi
 *
 * KÃ¤ynnissÃ¤ olevien ajansopimisten pyytÃ¤minen (TehtÃ¤vÃ¤ 34)
 *
 * Asiakas lÃ¤hettÃ¤Ã¤ viestin "LISTAATAPAAMISET:"
 * Palvelin vastaa lÃ¤hettÃ¤mÃ¤llÃ¤ listan vielÃ¤ kÃ¤ynnissÃ¤ olevista
 * ajansopimisista: "LISTAATAPAAMISET: tunnus1 tunnus2 tunnus3 ..."
 *
 *  Sopivien aikojen ilmoittaminen:
 *
 *  Asiakas lÃ¤hettÃ¤Ã¤ viestin "MINUNAJAT: sopimistunnus aika1 aika2 aika3..."
 *   missÃ¤ sopimistunnus on merkkijono ja aikaX on positiivinen kokonaisluku.
 * Palvelin vastaa 200 OK tai 5-alkuinen virhekoodi
 *
 * PÃ¤Ã¤tÃ¶ksen tekeminen:
 *
 * Kun sopimisen viive on kulunut umpeen, palvelin tekee pÃ¤Ã¤tÃ¶ksen
 * (pienin kokonaisluku joka kÃ¤y kaikille, tai -1 jos mikÃ¤Ã¤n aika ei kÃ¤y).
 *
 * Jos palvelimella on vielÃ¤ TCP-yhteys sellaisen asiakkaan kanssa joka on
 * tÃ¤hÃ¤n sopimiseen liittynyt, niin palvelin lÃ¤hettÃ¤Ã¤ pÃ¤Ã¤tÃ¶ksen ko. TCP-yhteyden
 * kautta.
 * Palvelin lÃ¤hettÃ¤Ã¤ "PAATOS: sopimistunnus aika"

 *
 * Asiakas pyytÃ¤Ã¤ pÃ¤Ã¤tÃ¶stÃ¤ viestillÃ¤ "PAATOS: sopimistunnus".
 * Palvelin vastaa "PAATOS: sopimistunnus aika" tai virhekoodilla jos pÃ¤Ã¤tÃ¶stÃ¤ ei ole.
 *
 *
 *
 **/
public class AikaProto {

    public static final String A_ALOITA = "ALOITATAPAAMINEN:";
    public static final String A_OLETUSOSOITE = "localhost";
    public static final String A_VIRHE = "500 VIRHE";
    public static final String A_KESKEN = "400 sopiminen on vielÃ¤ kesken";
    public static final String A_SOPIVAT = "MINUNAJAT:";
    public static final String A_ONKOSOPIMISIA = "LISTAATAPAAMISET:";
    public static final String A_PAATOS = "PAATOS:";
    public static final String A_OK = "200 OK";
    public static final String A_LOPPU = "LOPETA"; // TODO ei varsinaisesti ole kÃ¤ytÃ¶ssÃ¤
    public static final String EOL = "\r\n";    // rivin loppumerkki

    public static final int A_OLETUSPORTTI = 2024;



    // pari apumetodia, saa kÃ¤yttÃ¤Ã¤ jos haluaa
    // lisÃ¤Ã¤ vinkkejÃ¤ AikaPalvelin ja AikaAsiakasAloitus -tiedostoissa

    /**
     * merkkijonolistasta lista
     * @param aikalista vÃ¤lilyÃ¶nneillÃ¤ erotettuja kokonaislukuja
     * @return lista kokonaisluvuista
     */
    public static List<Integer> stringToList(String aikalista) {
        Scanner sc = new Scanner(aikalista);
        List<Integer> l = new ArrayList<>();
        while (sc.hasNextInt())
            l.add(sc.nextInt());
        return l;
    }

    /**
     * kokonaislukulistasta merkkijono
     * @param lista syÃ¶telista
     * @return merkkijonoesitys
     */
    public static String listToString(List<Integer> lista) {
        StringBuilder sb = new StringBuilder(lista.size() * 4);
        for (Integer x : lista) {
            sb.append(x);
            sb.append(" ");
        }
        return sb.toString();
    }

}
