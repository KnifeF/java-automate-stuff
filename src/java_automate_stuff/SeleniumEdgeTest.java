package java_automate_stuff;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
* This simple program is using Selenium WebDriver (EdgeDriver) 
* for the scraping process.
*/
public class SeleniumEdgeTest {
	
	/**
	 * The main function. 
	 * Using 'Selenium WebDriver' to scrape a web page. 
	 */
	public static void main(String args[]) throws TimeoutException
	{
		String givenUrl = "https://www.duckduckgo.com/";  // URL to get
		String expectedTitle = "Selenium - Wikipedia";  // expected title to find
		// path to geckodriver.exe
		System.setProperty("webdriver.edge.driver", "src/MicrosoftWebDriver.exe");
		// creates a new instance of the WebDriver (EdgeDriver).
		WebDriver driver = new EdgeDriver();
		// maximize browser's window
		driver.manage().window().maximize();
		
		// WebDriverWait will ignore instances of NotFoundException that are 
		// encountered (thrown) by default in the 'until' condition, and immediately 
		// propagate all others.
		WebDriverWait wait = new WebDriverWait(driver, 10);
		
		driver.get(givenUrl);  // Loads a web page in the current browser session.
		
		// This waits up to 10 seconds before throwing a TimeoutException, or 
		// finds the element and will return it in 0 - 10 seconds.
		WebElement searchBox = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.name("q")));
		
		searchBox.clear();  // clears text from search box
		searchBox.sendKeys("selenium", Keys.ENTER);  // send keys to search box element
		
		// This waits up to 10 seconds before throwing a TimeoutException, or 
		// finds the clickable element and will return it in 0 - 10 seconds.
		WebElement linkToClick = wait.until(ExpectedConditions
				.elementToBeClickable(By.partialLinkText("Wikipedia")));
		
		linkToClick.click();  // Clicks the element.
		
		// This waits up to 10 seconds before throwing a TimeoutException, or 
		// finds the expected page's title and will return it in 0 - 10 seconds.
		wait.until(ExpectedConditions.titleIs(expectedTitle));
		
		System.out.println("title --> " + driver.getTitle());  // web page's title
		
		// Gets the source of the current page
		String html_source = driver.getPageSource();
		System.out.println(html_source);
		
		driver.quit();  // quits the driver and closes every associated window
	}
}