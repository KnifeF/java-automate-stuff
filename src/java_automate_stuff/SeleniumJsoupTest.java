package java_automate_stuff;


import java.io.IOException;
// Selenium
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
// Jsoup
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
* This simple program is using 'Selenium' (WebDriver) for the scraping process, 
* and 'Jsoup' for HTML parsing process (from WebDriver's HTML Page Source). 
*/
public class SeleniumJsoupTest {
	/**
	 * delay for a given number of seconds.
	 * @param time - the time to wait (integer)
	 */
	public static void sleep(int time) {
		if (time > 0) {
		    try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * The main function. 
	 * Using 'Selenium' to scrape a web page, and 'Jsoup' to parse an HTML Data from it. 
	 */
	public static void main(String args[]) throws IOException
	{
		// path to chromedriver.exe
		System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
		// creates a new instance of the WebDriver (ChromeDriver).
		WebDriver driver = new ChromeDriver();
		// maximize browser's window
		driver.manage().window().maximize();
		
		// Loads a web page in the current browser session.
		driver.get("https://www.seleniumhq.org/");
		sleep(10);  // delay in seconds
		String pageTitle = driver.getTitle();  // web page's title
		System.out.println("title --> " + pageTitle);
		
		// Gets the source of the current page
		String html_source = driver.getPageSource();
		
		driver.quit();  // quits the driver and closes every associated window
		
		// loads and parses an HTML file, from WebDriver's HTML Page Source.
		Document doc = Jsoup.parse(html_source);
		System.out.println(doc);  // prints Page Source Document
		
		Elements paragraphs = doc.select("body p");  // find all p tags using a css selector
		
		for (Element pTag : paragraphs)  // iterate over list of elements
		{
			// text attribute of a tag
			System.out.println("paragraph:\n**********\n"+pTag.text()+"\n");
		}

	}
}