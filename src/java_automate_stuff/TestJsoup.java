package java_automate_stuff;


import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
* This simple program is using 'Jsoup' to fetch and parse an HTML page from given URL, 
* and parse links (a tags) & paragraphs (p tags) from fetched HTML.
*/

public class TestJsoup {

	/**
	 * This method is used to parse a tags from an HTML Document, and print the results.
	 * @param doc - an HTML Document
	 */
	public static void print_a_tags(Document doc) {
		Element body = doc.body();  // body from an HTML file
		Elements links = body.getElementsByTag("a");  // find all <a> tags in HTML's body
        
		for (Element link : links) {  // iterate list of elements (<a> tags)
			
			// element has 'href' and contains text
			if (link.hasAttr("href") && link.hasText()) {
				// href attribute of a tag
				// abs: attribute prefix to resolve an absolute URL from an attribute
				String linkHref = link.attr("abs:href");
				// text attribute of a tag
				String linkText = link.text();
				
			    System.out.println(linkHref + " | "+ linkText);
			}
		}
	}
	
	/**
	 * This method is used to parse p tags from an HTML Document, and print the results
	 * @param doc - an HTML Document
	 */
	public static void print_p_tags(Document doc) {
		Element body = doc.body();  // body from an HTML
		Elements all_p = body.getElementsByTag("p");  // find all p tags in HTML
        
		for (Element p : all_p) {  // iterate list of elements (<p> tags)
			
			if (p.hasText()) {  // element has text
				
				String pText = p.text(); // text from paragraph (p tag)
				System.out.println(pText+"\n*******************\n");
			}
		}
	}
	
	/**
	 * The main function. 
	 * Using 'Jsoup' to parse a tags and p tags from a given url. 
	 */
	public static void main(String[] args) throws IOException 
	{
		
		String givenUrl = "https://jsoup.org/";  // url to get
		String host = "MY.PROXY.HOST.IP";  // proxy host
		int port = 8080;  // proxy port (example: 8080)
		
		Document doc;
		// Creates a new Connection to a URL. Use to fetch and parse an HTML page.
		// It will throw an IOException if an error occurs.
		doc = Jsoup.connect(givenUrl)
		.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "  // sets the user-agent
				+ "AppleWebKit/537.36 (KHTML, like Gecko) "
				+ "Chrome/59.0.3071.115 Safari/537.36")
		.header("Content-Language", "en-US")  // adds header
		.timeout(10000)  // request timeout duration. throws 'SocketTimeoutException' if a timeout occurs.
		.get();  // fetches and parses a HTML file.
		
		String title = doc.title();  // title from an HTML file.
		System.out.println("web page title --->"+title+"\n\n\n");
		
		print_p_tags(doc);  // parses and prints p tags from an HTML doc
		print_a_tags(doc);  // parses and prints a tags from an HTML doc

	}

}
