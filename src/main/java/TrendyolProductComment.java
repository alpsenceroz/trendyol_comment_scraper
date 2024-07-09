import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.JavascriptExecutor;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TrendyolProductComment extends Thread{

    private Connection c;
    private Element link;
    private WebDriver driver;
    private Statement stmt;
    // private FirefoxOptions options;
    private FirefoxOptions options;

    public TrendyolProductComment(Element link, Statement stmts, Connection c) {

        this.c = c;
        this.link=link;
        this.stmt=stmts;

        WebDriverManager.firefoxdriver().setup();
        this.options = new FirefoxOptions();
        this.options.addArguments("--headless");
        this.driver = new FirefoxDriver(this.options);
    }

    @Override
    public void run() {

        String linkSTR = "https://www.trendyol.com"+link.attr("href");

        String[] arrOfStr = linkSTR.split("\\?boutiqueId", 2);
        linkSTR=arrOfStr[0]+"/yorumlar";

        driver.get(linkSTR);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Scroll the comments unti. reach the bottom of the page 
       int SCROLL_PAUSE_TIME = 4000; // in milliseconds

       // Get scroll height
       JavascriptExecutor js = (JavascriptExecutor) driver;
       long last_height = (long) js.executeScript("return document.body.scrollHeight");
       long new_height;

       System.out.println("last length " + last_height);
       try {
        Thread.sleep(SCROLL_PAUSE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

       while (true) {
           // Scroll down to bottom
           js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

           // Wait to load page
           try {
            Thread.sleep(SCROLL_PAUSE_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           // Calculate new scroll height and compare with last scroll height
           new_height = (long) js.executeScript("return document.body.scrollHeight");
           System.out.println("new length " + new_height);

           if (new_height == last_height) {
               break;
           }

           last_height = new_height;
       }
            

        Document docComment = Jsoup.parse(driver.getPageSource());

        try {

            String saller = String.valueOf(docComment.getElementsByClass("product-brand"));
            saller = saller.replace("<span class=\"product-brand\">","").replace(" </span>","");

            String prName = String.valueOf(docComment.getElementsByClass("product-name"));
            prName = prName.replace("<span class=\"product-name\">","").replace("</span>","");

            Elements comments = docComment.select("div.reviews").first().getElementsByClass("comment");

            boolean flag = false;
            String sql = "";
            String id = "";
            System.out.println("Comment number " + comments.size());
            for (Element comment : comments){

                int starCounter = 0;

                for(Element full : comment.getElementsByClass("ratings readonly").first().getElementsByClass("full")){

                    if (full.attr("style").toString().contains("width: 0px;")==true){
                        starCounter+=1;
                    }

                    if (full.attr("style").toString().contains("width: 0%;")==true){
                        starCounter+=1;
                    }

                }

                starCounter = 5-starCounter;

                if(flag==false) {

                    sql = "INSERT INTO TBL_Product (Product_Name,Product_Brand,Product_Link)" +
                            "VALUES ('" + prName + "', '" + saller + "', '" + arrOfStr[0] + "');";
                    stmt.executeUpdate(sql);
                    c.commit();

                    sql = "SELECT MAX(Product_Id) AS [id] FROM TBL_Product;";
                    ResultSet rs = stmt.executeQuery(sql);
                    id = rs.getString("id");

                    System.out.println(prName);

                    flag = true;

                }


                String commentSTR =  comment.select("p").toString();
                commentSTR = commentSTR.replace("<p>", "").replace("</p>","");

                sql = "INSERT INTO TBL_Comment (Product_Id,Comment_Content,Comment_Evaluation) " +
                        "VALUES ('"+id+"', '"+commentSTR+"', '"+String.valueOf(starCounter)+"');";

                stmt.executeUpdate(sql);
                c.commit();

            }

        } catch (Exception e){

        }

        driver.close();
    }

}
