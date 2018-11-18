package java_automate_stuff;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
* This simple program is using Selenium WebDriver (geckodriver - Firefox) 
* for the scraping process.
*/
public class SeleniumFirefoxTest {
	
	/**
	 * The main function. 
	 * Using 'Selenium WebDriver' to scrape a web page. 
	 */
	public static void main(String args[]) throws TimeoutException
	{
		String givenUrl = "https://www.bing.com";  // URL to get
		String expectedTitle = "Selenium - Web Browser Automation";  // expected title to find
		// path to geckodriver.exe
		System.setProperty("webdriver.gecko.driver", "src/geckodriver.exe");
		// creates a new instance of the WebDriver (FirefoxDriver).
		WebDriver driver = new FirefoxDriver();
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
				.elementToBeClickable(By.partialLinkText("Web Browser Automation")));
		
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