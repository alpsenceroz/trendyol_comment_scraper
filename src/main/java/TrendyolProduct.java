import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.sql.*;
import java.io.IOException;
import java.util.List;

public class TrendyolProduct {

    public static void main(String args[]) throws IOException, InterruptedException, ClassNotFoundException, SQLException {

        //String genders = "1,2,3";
        String genders = "";

        // String brands = "38,37,271,101990,43,44,136,230,33,124,160,101439,436,189,257,146279,131,634,150,859";
        String brands = "";

        //int[] categories = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,20,40,60,80};
        int[] categories = {}; 

        /*Products
            wg: genders
                1: woman
                2: man
                3: kid
            wb: brands
            wc: category
            os:
        */

        // url search keys
        String genderFilter = "wg=1,2,3"; // all genders
        String brandFilter = ""; // all brands
        int CATEGORY_RANGE = 250; // categories from 1 to N

        if (!genders.isEmpty()){
            genderFilter = "wg=" + genders;
        }

        if (!brands.isEmpty()){
            brandFilter = "&wb=" + brands;
        }

        if (categories.length == 0){
            categories = new int[CATEGORY_RANGE];
            for (int i = 0; i < CATEGORY_RANGE; i++){
                categories[i] = i + 1;
            }
        }


        for (int category: categories){
            String categoryFilter = "&wc="+category;
            String url = "https://www.trendyol.com/sr?"+genderFilter+brandFilter+categoryFilter+"&os=1&pi=";

            System.setProperty("LD_BIND_NOW", "1");
            System.setProperty("NO_AT_BRIDGE", "1");

            // sqlite
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:TrendyolProduct.sqlite3");
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();
            // TODO: check if there is relates results for the search query
            

            // Iterate over the pages until the end
            int page = 0;
            while(true) {
                Document doc = Jsoup.connect(url+String.valueOf(page)).get();
                System.out.println("Page : "+String.valueOf(page));
                //System.out.println(doc.html());

                // Get the product link from the listed products
                List<Element> links = doc.select("div.p-card-chldrn-cntnr > a");

                // if there is no product in the page, terminate the loop
                if(links.size() == 0) {
                    System.out.println("No search result for category " + category);
                    break;
                }

                for (Element link : links)
                {
                    // Initialize TrendyolProductComment threads for every single product in the page
                    new TrendyolProductComment(link, stmt, c).start();

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // increment the page number
                page++;
            }

            
        stmt.close();
        c.close();
            
        } 

    }

}
