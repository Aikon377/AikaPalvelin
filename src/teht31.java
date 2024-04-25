import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class teht31 {


    public static void main(String[] args) {
        final String URL_STRING = "https://docs.oracle.com/javase/8/docs/api/java/net/URL.html";

        try {
            URL url = new URL(URL_STRING);
            URLConnection connection = url.openConnection();
            Document doc = Jsoup.parse(connection.getInputStream(), "UTF-8", URL_STRING);

            Elements links = doc.select("a[href]");
            int hrefCount = links.size();

            System.out.println("Hyperlinkkien määrä: " + hrefCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    }




