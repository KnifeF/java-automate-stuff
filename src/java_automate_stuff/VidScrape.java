package java_automate_stuff;

import java.io.File;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Random;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sikuli.script.*;


/**
* This simple program can be used to download MP3 files from required youtube videos (by a search term, 
* minimum viewers, and maximum length for each video).
* The program is trying to automate human behaviour and relies on desktop GUI (keyboard&mouse) using 'sikulixapi', 
* to visit websites and using 'jsoup' & 'regex' to parse HTML data.
* 
* The given inputs (args): 
* ************************
* 1. String - path to a file, that contains the search terms, to search on youtube.
* 2. String - maximum video duration (in minutes) of required videos to download - Optional.
* 3. String - minimum views on required videos to download - Optional.
* 
* input arguments --> "[path to text file]" "max_len [N]" "min_views [N]"
* For example --> "C:\Users\MyUser\Desktop\search_terms.txt" "max_len 8" "min_views 5000000"
* 
* The output: music files (mp3) in the 'downloads' directory of Chrome browser (if the the process went properly).
* 
* short explanation of the process:
* *********************************
* The terms from the given file are searched (visually) on 'youtube' with Google Chrome browser --> the required 
* URLs are parsed from search results --> the required URLs are downloaded as '.mp3' files 
* through 'onlinevideoconverter'.
* 
* Note:
* *****
* 1. Currently, the program is working just on Windows OS.
* 
* 2. It requires: 'sikulixapi.jar' & 'jsoup-1.11.3.jar' (external libraries), 'Google Chrome' browser, 
* and a set of images (images of web elements to identify and use with 'sikulix-api').
* 
* 3. this program is "visiting" 'youtube' and 'onlinevideoconverter' without a use of APIs.
* 
* Disclaimer:
* ***********
* This simple program has been written for educational purposes only, and it might violate 
* 'onlinevideoconverter' and/or 'youtube' TOS. Use at your own risk.
* 
* 
* @author  KnifeF
*/
public class VidScrape
{
	// the name of the OS in LowerCase
	public static String OS = System.getProperty("os.name").toLowerCase();
	
	// represent paths of files or directories (String)
	public static String BASE_PATH = Paths.get(System.getProperty("user.dir")).toString();
	public static String IMGS_PATH = Paths.get(BASE_PATH, "src", "imgs").toString();
	
	// 'youtube' URL
	public static String YOUTUBE_URL = "https://www.youtube.com";
	// 'onlinevideoconverter' URL
	public static String VID_CONVERTER_URL = "https://www.onlinevideoconverter.com/mp3-converter";
	
	// string of characters ordered like in keyboard (qwerty)
	public static String KEYBOARD_KEYS = "1234567890qwertyuiopasdfghjkl;zxcvbnm,"; 
	
	public static Random RAND = new Random();  // create Random object (java.util.Random)
	public static Screen SCR = new Screen();  // create Screen object (org.sikuli.script.Screen)
	
	// Constructs a new Scanner (that produces values scanned from the specified input stream)
	public static Scanner INPUT_READER = new Scanner(System.in);
	
	
	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to - open Chrome browser, navigate to 'youtube' and search for the required terms,
	 * parse the required youtube URLs from search results (according to the given arguments 
	 * and search terms), and download all URLs to '.mp3' files through 'onlinevideoconverter.com' 
	 * @param kw_list - list of terms to search on youtube (LinkedList<String>)
	 * @param max_duration - maximum length, in minutes, for each required video to download (integer)
	 * @param min_views - minimum views, for each required video to download (integer)
	 */
	public static void scrape_youtube(LinkedList<String> kw_list, int max_duration, int min_views) {
		
		// attempts to open 'Google Chrome' through command-line
		int connection_attempts = 0;
		boolean opened_browser = false;
		while(connection_attempts < 2 && !opened_browser)  // while loop
		{
			opened_browser = open_browser();  // trying to open 'Google Chrome'
			connection_attempts++;  // counter (int)
		}
		
		// indicates that the browser is opened
		if (opened_browser)
		{
			// type youtube's URL and check if 'search button' appears on screen
			if (get_youtube())
			{	
				// Constructs an empty list, to contain all youtube urls to download 
				LinkedList<String> links_to_download = new LinkedList<String>();
				
				int counter = 0;
				for (String keyword : kw_list) {  // for loop on list of keywords
					if (keyword.length() > 1)
					{
						System.out.println(keyword);
					    
						// search given keywords in youtube and save results
						if (search_kw_in_youtube(keyword, counter)) 
						{  
							sleep(RAND.nextInt(6) + 6, "");
							scroll_down_page();  // randomly scroll page down, hitting 'PAGE_DOWN' key
							
							// inspect result page, and copy html source to clipboard
							if (inspect_and_copy_html()) 
							{
								try {
									// get data from clipboard (should contain copied html element from search results) 
									String clipboard_data = (String) Toolkit.getDefaultToolkit()
									        .getSystemClipboard().getData(DataFlavor.stringFlavor);
									
									/* parses required youtube links and adds them to the list.
						    		 * they parsed by - keyword (string), maximum video length (integer), 
						    		 * and minimum views (integer) 
						    		 */
									links_to_download.addAll(parse_links_from_html(clipboard_data, 
											keyword, max_duration, min_views));
									
								} catch (HeadlessException | UnsupportedFlavorException | IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
							}
					    }
					    counter++;
					    
					    sleep(RAND.nextInt(5000) + 2000, "");  // random delay in milliseconds
					}
				}
				sleep(10, "sec");
				SCR.type(Key.F4, Key.ALT);  // should close google chrome browser
				
				if (!links_to_download.isEmpty()) {  // check if the list of URLs is not empty
					// trying to download the required youtube URLs to mp3
	    			download_youtube_urls(links_to_download);
	    		}
			}
		}
	}
	
	/**
	 *  This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * to open 'cmd' using keyboard shortcuts, and start 'Google Chrome' browser on 'incognito' mode
	 * @return true if the browser is opened, or false (boolean)
	 */
	public static boolean open_browser() {
		String incognito_path = Paths.get(IMGS_PATH, "incognito_logo.png").toString();
		
		if (isWindows()) {  // check if the OS is windows
			sleep(2, "sec");
			SCR.type("r", Key.WIN);  // shortcut that opens 'Run' new window
			sleep(2, "sec");
			SCR.type("cmd");  // type 'cmd'
			SCR.type(Key.ENTER); // type enter to open the command line (cmd)
			sleep(2, "sec");
			
			/* type 'start chrome --incognito & exit' command in cmd, to open Google Chrome browser 
			 * on 'incognito' mode, and exit cmd window  
			 */
			SCR.type("start chrome --incognito & exit");
			
			// type enter to excecute the command
			SCR.type(Key.ENTER);
			
			try {
				sleep(2, "sec");
				SCR.wait(incognito_path);  // wait for the target image to appear on screen
				return true;
			} catch (FindFailed e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Your OS won't run this script properly..");
			System.exit(0);
		}
		return false;
		
		/*
		else if (isLinux()) {  // check if the OS is linux
			scr.type("t", Key.CTRL + Key.ALT);  // shortcut that opens 'terminal' new window
			sleep(2);
			scr.type("firefox");  // type 'firefox' command
			
			// type enter to excecute the command and open Firefox browser
			scr.type(Key.ENTER);
		}
		*/
	}
	
	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to type&search youtube's URL in Chrome browser, and make sure that 'youtube' appears on screen
	 * @return true or false (boolean)
	 */
	public static boolean get_youtube() {
		
		// path to an image of youtube search button
		String youtube_search_btn = Paths.get(IMGS_PATH, "youtube_search_btn.png").toString();
		
		String res = type_string(YOUTUBE_URL, false);  // type the url without mistakes
		sleep(500, "");  // delay in milliseconds
		if (!res.equals(YOUTUBE_URL)) {
			SCR.type(YOUTUBE_URL);
		}
		
		SCR.type(Key.ENTER);  // type enter and navigate to youtube url 
		sleep(RAND.nextInt(10) + 5, "sec");  // delay in seconds
		
		try {
			// wait for target image to appear on screen (a search button in youtube)
			SCR.wait(youtube_search_btn);
			return true;
		} catch (FindFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
		
	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to search a term in youtube (trying to simulate human behaviour)
	 * @param keyword - search term to search on youtube
	 * @param counter - counter to know when the query search box might not be empty (integer)
	 * @return - true or false (boolean)
	 */
	public static boolean search_kw_in_youtube(String keyword, int counter) {
		
		String youtube_search_btn = Paths.get(IMGS_PATH, "youtube_search_btn.png").toString();
		String youtube_q_box = Paths.get(IMGS_PATH, "youtube_query_box.png").toString();

		try {
			sleep(RAND.nextInt(15000) + 4000, "");  // random delay in milliseconds
			
			SCR.wait(youtube_search_btn);  // wait for target image to appear on screen (search button)
			
			// trying to find and click on target image (query search box)
			if (find_and_click(youtube_q_box, 1))
			{
				sleep(RAND.nextInt(2500) + 500, "");  // random delay in milliseconds
				
				if(counter > 0) { 
					
					if (RAND.nextInt(5) + 1 == 2) {  // if random number is 2 (integer)
						SCR.type(Key.LEFT, Key.ALT);  // navigate to previous page (keyboard shortcut)
					}
					
					del_q_box();  // delete youtube's query search box
					
				}
				
				boolean sloppy_typing = true;
				// typing may include mistakes (trying to simulate human behaviour)
				String res = type_string(keyword, sloppy_typing);
				
				// the entered keyword is not equal to the original (typing mistakes)
				if (!res.equals(keyword))
				{
					sleep(RAND.nextInt(1000) + 500, "");  // random delay in milliseconds
					del_q_box();  // delete youtube's query search box after typing a mistake
					sleep(RAND.nextInt(1000) + 500, "");  // random delay in milliseconds
					
					res = type_string(keyword, sloppy_typing=false);  // typing keyword without mistakes
				}
				sleep(RAND.nextInt(1200) + 500, "");  // random delay in milliseconds
				SCR.type(Key.ENTER);  // type enter to search the term on youtube
				
				return true;
			}
		} catch (FindFailed e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;
	}
	
	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to visually find and click patterns of images (from image files) - using Screen Object
	 * @param file_path - the path for an image to find and click using 'sikuli' (string)
	 * @param click - determines whether to make a 'click', or a 'rightClick' (integer)
	 * @return true when operation succeed, or false (boolean)
	 */
	public static boolean find_and_click(String file_path, int click) {
		if (file_path.endsWith(".png")) {
			try {
				SCR.wait(file_path);  // wait for target image to appear on screen
				SCR.find(file_path);  // find image on screen by image file path (.png)
				if (click == 0) {
				    SCR.rightClick(file_path);  // right click on target image
				}
				else {
					SCR.click(file_path);  // click on target image
				}
				
				return true;
			} catch (FindFailed e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
		
	}
	
	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to delete all selected text area.
	 */
	public static void del_q_box() {
		SCR.type("a", Key.CTRL);  // select all text area (keyboard shortcut)
		sleep(RAND.nextInt(1000) + 1000, "");  // random delay in milliseconds
		SCR.type(Key.BACKSPACE);  // delete selected text with backspace
	}

	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to type a term, sometimes with 'human like' typing errors (trying to simulate human behaviour)
	 * @param keyword - the original string to type in query search box (String)
	 * @param sloppy_typing - true (may lead to typing errors), or false (boolean)
	 * @return the typed string - original or modified (String)
	 */
	public static String type_string(String keyword, boolean sloppy_typing) 
	{
		String res = "";
		
		for (int i = 0; i < keyword.length(); i++)  // for loop over characters of the string
		{
			char current_ch = keyword.charAt(i);  // char value at the specified index
			
			// the index of the first occurrence of the given character, or -1 if it is not found.
			int index_on_keyboard = KEYBOARD_KEYS.indexOf(current_ch);
			
			// random number (integer) is 1, and 'index_on_keyboard' is not -1 (it is found)
			if (sloppy_typing && (RAND.nextInt(10) + 1 == 1) && (index_on_keyboard != -1))
			{
				// 'index_on_keyboard' is not the first or the last index inside 'KEYBOARD_KEYS'
				if(0 < index_on_keyboard && index_on_keyboard < KEYBOARD_KEYS.length()-1) 
				{
					// simple conditional assignment statement
					int x = (RAND.nextInt(2) + 1 == 2) ? 1 : -1;
					// assign char with the next or previous char on keyboard
					current_ch = KEYBOARD_KEYS.charAt(index_on_keyboard + x);
				}
			}
			res += current_ch;  // add current character (modified or not) into string 'res'
			
			// convert char to string - type() function don't accept char (only String)
			String tmp_key = "" + current_ch;
			SCR.type(tmp_key);  // type a string that holds a character
			
			if (tmp_key.equals(" "))  // the character is 'space'
			{
				sleep(RAND.nextInt(1500) + 500, "");  // random sleep in milliseconds
			}
			sleep(RAND.nextInt(250) + 250, "");  // random sleep in milliseconds
		}
		return res;
	}
	
	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to scroll down page, by randomly hitting 'PAGE_DOWN' key (more than once).
	 */
	public static void scroll_down_page() 
	{
		sleep(RAND.nextInt(2000) + 1500, "");  // random delay in milliseconds
		
		int  n = RAND.nextInt(6) + 4;  // random number (integer)
		for (int i = 0; i < n; i++)  // a for loop
		{
			SCR.type(Key.PAGE_DOWN);  // type on 'PAGE_DOWN' key
			sleep(RAND.nextInt(2500) + 500, "");  // random delay in milliseconds
		}
		
		sleep(RAND.nextInt(2000) + 1500, "");  // random delay in milliseconds
	}

	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to use the Chrome 'Inspect Element Tool' (tool that is used to examine HTML for a page), 
	 * and copy the HTML source of the current web page (copying the <html> element to Clipboard).
	 * @return - true or false (boolean)
	 */
	public static boolean inspect_and_copy_html() 
	{
			
			// path to an image of '<html' in 'Inspect Element Tool'
			String inspect_html = Paths.get(IMGS_PATH, "inspect_html.png").toString();
			// path to an image of 'Copy' option after 'rightClick' on element in 'Inspect Element Tool'
			String copy_html_opt = Paths.get(IMGS_PATH, "copy_html_opt.png").toString();
			// path to an image of 'Copy element' in 'Inspect Element Tool'
			String copy_elem = Paths.get(IMGS_PATH, "copy_element.png").toString();
			
			sleep(RAND.nextInt(6) + 4, "sec");  // random delay in seconds
	
			SCR.type("i", Key.CTRL + Key.SHIFT);  // open Inspect Element Tool (keyboard shortcut)
			sleep(RAND.nextInt(3) + 1, "sec");  // random delay in seconds
			
			if (find_and_click(inspect_html, 0))  // find and rightClick on target image ('<html')
			{
				if (find_and_click(copy_html_opt, 1)) {  // find and click on target image ('Copy') 
					
					if (find_and_click(copy_elem, 1)) {  // find and click on target image ('Copy element')
						
						sleep(RAND.nextInt(3) + 1, "sec");  // random delay in seconds
						SCR.type("i", Key.CTRL + Key.SHIFT);  // close Inspect Element Tool (keyboard shortcut)
						sleep(2, "sec");  // one second delay
						return true;
					}
				}
			}
			return false;
	}
	
	/**
	 * This method is used to take HTML data (String), and other parameters to filter only 
	 * the required links, and returns the required youtube URLs (to download) in a list.
	 * @param html - should contain the page source of youtube's search results (String)
	 * @param band_name - the given name of the band (String)
	 * @param max_duration - maximum duration a video (integer)
	 * @param min_views - minimum views on the video (integer)
	 * @return list of video links to download (LinkedList<String>)
	 */
	public static LinkedList<String> parse_links_from_html(String html, 
			String band_name, int max_duration, int min_views) 
	{	
		// Constructs an empty LinkedList (type: 'String')
        LinkedList<String> youtube_links = new LinkedList<String>();
        
     // indicates that the string should contain an html content
        if (html != null && html.startsWith("<html"))
        {
        	Document doc = Jsoup.parse(html);  // parses the input HTML into a new Document.

    		if (doc != null)
    		{
    			Elements links = doc.getElementsByTag("a");  // find all <a> tags in HTML
    	        
    			for (Element link : links)  // iterate list of elements (<a> tags) 
    			{  
    				// check if the element has attributes - 'href', 'id', 'aria-label' and contains text
    				if (link.hasAttr("href") && link.hasAttr("id") && link.hasAttr("aria-label") 
    						&& link.hasText()) 
    				{
    					// check if the attribute "id" called "video-title"
    					if (link.attr("id").equals("video-title")) 
    					{
    						// check if the text contains the band name
    						if (link.text().toLowerCase().contains(band_name.toLowerCase())) 
    						{
    							// 'aria-label' attribute in <a> tag (string)
    							String aria_label = link.attr("aria-label");
    							
    							/* check if view's count of the video is above the given argument 'min_views'
    							 * and the duration of the video is below the given argument 'max_duration'
    							 */
    							if (vid_above_min_views(min_views, aria_label) 
    									&& vid_below_max_duration(max_duration, aria_label)) {
    								// adds href attribute of <a> tag to the url (youtube.com)
    								String vid_url = "https://www.youtube.com"+link.attr("href");
    								
    								youtube_links.add(vid_url);  // adds video url (string) to LinkedList
    							}
    						}
    					}
    				}
    			}
    		}
        }
		
		return youtube_links;
	}
	
	/**
	 * This method is used to get the length of the video (in minutes) from 'aria-label' attribute, 
	 * using regular expression operations (java.util.regex)
	 * @param pattern_st - pattern to search for with regex (String)
	 * @param aria_label - aria_label, an element attribute that should 
	 * contain the duration of the video (String)
	 * @return video's length (or part of it) in minutes (integer) 
	 */
	public static int ret_minutes_of_vid(String pattern_st, String aria_label) {
		Pattern r = Pattern.compile(pattern_st);  // Create a Pattern object
		Matcher m = r.matcher(aria_label);  // create matcher object
		
		int vid_minutes = 0;
		
		// find the next subsequence of the input sequence that matches the pattern
		if (m.find( )) {
			
			// convert m.group(1) to integer (group(1) should contain string of digits)
			vid_minutes = Integer.parseInt(m.group(1));
			
			if(pattern_st.contains("hour")) {  // indicates that the video may be above one hour
				vid_minutes *= 60;  // convert hours to minutes (example: 2 hours --> 120 minutes)
			}
		}
		return vid_minutes;
	}
	
	/**
	 * This method is used to check if the length of the video 
	 * is lower than the given argument.
	 * @param max_duration - max duration of the video in minutes (String)
	 * @param aria_label - aria_label, an element attribute that should 
	 * contain the duration of the video (String)
	 * @return true or false (boolean)
	 */
	public static boolean vid_below_max_duration(int max_duration, String aria_label)
	{
		// indicates that the attribute may include the duration of the video
		if (aria_label.contains(" minute") || aria_label.contains(" hour"))
		{	
			String minutes_re = "(\\d+) minute";  // regex pattern to find number of minutes (string)
			String hours_re = "(\\d+) hour";  // regex pattern to find number of hours (string) 
			
			// search with regex pattern and find the length of the video (in minutes)
			int vid_minutes = ret_minutes_of_vid(minutes_re, aria_label)
					+ ret_minutes_of_vid(hours_re, aria_label);  // hours should be converted to minutes

			// check if the length of the video is below or equal to the given argument (max_duration)
			if (max_duration >= vid_minutes) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method is used to check if the number of views on the video 
	 * is greater than the given argument.
	 * @param min_views - minimum views on the video (integer)
	 * @param aria_label - aria_label, an element attribute that should contain view's count (String)
	 * @return true or false (boolean)
	 */
	public static boolean vid_above_min_views(int min_views, String aria_label)
	{
		
		// indicates that the attribute may include the number of views on the video
		if (aria_label.contains(" views"))
		{
			// split all words in String by space to an array
			String[] split_aria = aria_label.split(" ");
			int len = split_aria.length;  // array's length
			
			// array's length is bigger than one, and the last index in the array is the word 'views'
			if (len > 1 && split_aria[len-1].equals("views")) 
			{
				/* the index before last should be a string that represents the number of 
				 * views on the video
				 */
				String views_str = split_aria[len-2];
				
				// take out all the commas from string (when there are above thousand views, like 1,500,000)
				views_str = views_str.replaceAll(",", "");  

				int vid_views = Integer.parseInt(views_str); // convert the string to an integer
				if (vid_views > min_views)  // check if the video has more views the the given argument
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying to download all of the youtube links (URLs) to mp3 files, 
	 * through 'onlinevideoconverter.com' (convert the links to mp3 files)
	 * @param links_to_download - list that should contain the required links (from youtube) 
	 * to download (LinkedList<String>)
	 */
	public static void download_youtube_urls(LinkedList<String> links_to_download)
	{
		boolean opened_browser = open_browser();  // trying to open Chrome browser
		if (opened_browser)  // indicates that the browser is opened
		{
			// paths of images (strings), that are used to find or click on target images
			String converter_q_box = Paths.get(IMGS_PATH, "vid_converter_q_box.png").toString();
			String download_btn = Paths.get(IMGS_PATH, "download_vid_btn.png").toString();
			String area_on_page = Paths.get(IMGS_PATH, "vid_converter_area_on_page.png").toString();
			String nav_back = Paths.get(IMGS_PATH, "going_back.png").toString();
			// String vid_converter = Paths.get(IMGS_PATH, "online_vid_converter.png").toString();
			
			// type url and press 'ENTER' - navigate to 'onlinevideoconverter.com' (mp3-converter)
			SCR.type(VID_CONVERTER_URL);
			SCR.type(Key.ENTER);
			sleep(3, "sec");  // delay in seconds
			
			int count = 0;
			for (String vid_link : links_to_download)  // for loop on list
			{
				System.out.println("youtube link to download --> " + vid_link);

				int x = 10000;  // delay in milliseconds (int)
				if (count % 5 == 0)  // count is divided by 5 without remainer
					x += 5000;  // bigger starting delay in milliseconds (int)
				sleep(RAND.nextInt(5000) + x, "");  // random delay in milliseconds
				
				// SCR.wait(converter_q_box);  // wait target image to appear on screen
				
				// find and click on target image (query search box)
				if (find_and_click(converter_q_box, 1)) {  
					
					SCR.type("a", Key.CTRL);  // select all text area
					sleep(500, "");  // delay in milliseconds
					SCR.type(Key.BACKSPACE);  // type 'BACKSPACE' key
					sleep(RAND.nextInt(5000) + 500, "");  // random delay in milliseconds
					
					SCR.paste(vid_link);  // paste current video link from list to download
					sleep(RAND.nextInt(1000) + 400, "");  // random delay in milliseconds
					SCR.type(Key.ENTER);  // press enter
					sleep(RAND.nextInt(10) + 10, "sec");  // random delay in seconds
					
					// find and click on target image (download button)
					if (find_and_click(download_btn, 1)) 
					{	
						sleep(2, "sec");  // delay in seconds
						if (popup_ads_on_new_tab())  // indicates that pop-up ad appears
						{
							SCR.type("w", Key.CTRL);  // close pop-up new tab (probably advertising)
							sleep(RAND.nextInt(10000) + 1500, "");  // random delay in milliseconds
						}
						
						boolean moved_back= false;
						
						// find and rightClick on target image (area on page - one colored)
						if (find_and_click(area_on_page, 0)) 
						{
							sleep(100, "");  // delay in milliseconds
							
							// find and click on target image ('Back' option)
							if (find_and_click(nav_back, 1)) {
								System.out.println("nav back to '/mp3-converter'");
								moved_back = true;
							}
						}
						if (!moved_back) {  // failed attempt to navigate to previous page
							// navigate to previous page (keyboard shortcut)
							SCR.type(Key.LEFT, Key.ALT);
						}
					}
					
				}
				count++;
			}
			SCR.type(Key.F4, Key.ALT);  // close browser window
		}
	}

	/**
	 * This method is used to automate desktop GUI functions with 'sikuli' (keyboard & mouse), 
	 * trying find out if a pop-up ads show up in a new tab - when a new tab appears on screen, 
	 * the target image (on the original tab) should be hidden from screen.
	 * @return - false if target image was found, or true (boolean)
	 */
	public static boolean popup_ads_on_new_tab() {
		String vid_converter = Paths.get(IMGS_PATH, "online_vid_converter.png").toString();
		// String download_btn = Paths.get(IMGS_PATH, "download_vid_btn.png").toString();

		try {
			SCR.wait(vid_converter);  // wait for target image to appear on screen
			return false;
		} catch (FindFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * This method is used to make a delay between tasks when needed, using 
	 * 'java.util.concurrent.TimeUnit'
	 * @param sec - time to sleep (delay) in seconds OR milliseconds (integer)
	 */
	public static void sleep(int time, String time_unit) {
		if (time > 0 && time_unit != null) {
		    try {
		    	if (time_unit.toLowerCase().equals("sec") || time_unit.toLowerCase().equals("second"))
		    	{
		    		// delay for the given number of seconds
		    		TimeUnit.SECONDS.sleep(time);
		    	}
		    	else if (time >= 100)  // minimum delay is 100 milliseconds
		    	{
		    		// delay for the given number of milliseconds
		    		TimeUnit.MILLISECONDS.sleep(time);
		    	}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
		
	/**
	 * This method is used to check if the OS is Windows
	 * @return true or false (boolean)
	 */
	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
	
	/**
	 * This method is used to check if the OS is Linux
	 * @return true or false (boolean)
	 */
	public static boolean isLinux() { 
		return (OS.indexOf("linux") >= 0 ); 
	}
	
	/**
	 * This method is used to read lines of file from given path, and return as list
	 * @param file_path - path to the file (String)
	 * @return list of lines from given file
	 */
	public static LinkedList<String> ret_lines_from_file(String input_path){
		LinkedList<String> file_lines = new LinkedList<String>();  
		
		// the path (String) is not null and ends with '.txt' extension (of text file)
		if((input_path != null) && input_path.endsWith(".txt")) 
		{
			try {
				
				File f = new File(input_path);  // Creates a new File
				
				// Tests whether the file denoted by this abstract pathname is a normalfile
				if (f.isFile())
				{
					// Creates a buffering character-input stream
					BufferedReader br = new BufferedReader(new FileReader(f));
					String readLine = "";
					
					// loop that reads lines of text while the line is not 'null'
					while ((readLine = br.readLine()) != null) {
						if (readLine.length() < 40)  // if the length of the string is below 40 chars
						{
							file_lines.add(readLine);  // add line (String) to list (LinkedList<String>)
						}
					}
					br.close();
				}
				
			} catch (IOException | NullPointerException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return file_lines;
	}

	/**
	 * This method is used to check if a file exists in the given path, and not empty. 
	 * @param file_path - path for a file (String)
	 * @return true or false (boolean)
	 */
	public static boolean file_exists_and_not_empty(String file_path)
	{
		try 
		{
			// check if path exists, ends with a ".txt" extension, and is a regular file 
			if (file_path != null && !file_path.isEmpty() && file_path.endsWith(".txt") 
					&& Files.isRegularFile(Paths.get(file_path)))
			{
				// Creates a buffering character-input stream
				BufferedReader br = new BufferedReader(new FileReader(file_path));     
				String line = br.readLine();  // reads a line of text
				br.close();  // closes the stream
				
				if (line != null)  // line of text that is not null
					return true;
				else {
					System.err.println("invalid command - file is empty");
				}
			}
			else {
				System.err.println("invalid command");
			}
		} catch (IOException|SecurityException e) {
	        System.err.println("IOException|SecurityException");
		}
		
		return false;
	}
	
	/**
	 * This method is used to get inputs from the user and return arguments as array.
	 * @return array with the required arguments (String[])
	 */
	public static String[] ret_inputs_to_array()
	{
		String []array = new String[3];  // creates new array (String [])
		String f_path = "";
		int n1, n2;
		
		// get an input - path to text file
		do {  // do-while loop
			System.out.println("\"C:\\Users\\MyUser\\Desktop\\search_terms.txt\"\n");
			
			System.out.println("Enter path to a text file (with search terms for youtube), "
					+ "like the example above, or 'q' to quit:");
			try {
				// scanner past the current line and returns the input that was skipped
				f_path = INPUT_READER.nextLine();
			} catch (NoSuchElementException|IllegalStateException|InvalidPathException e) {
				// TODO: handle exception
			}
			
			if (f_path.toLowerCase().equals("quit") || f_path.toLowerCase().equals("q")) {
				System.exit(0);
			}	
		} while (!file_exists_and_not_empty(f_path));
		array[0] = f_path;  // set array's first index with the given path
		
		// get an input (optional) - max duration for a video
		System.out.println("5\n");
		System.out.println("Enter max duration (Optional) for video in minutes [n], like the example above:");
		try {
			n1 = Integer.parseInt(INPUT_READER.next());
		} catch (NoSuchElementException|IllegalStateException
				|InvalidPathException|NumberFormatException e) {
			// TODO: handle exception
			n1 = 0;
		}
		array[1] = "max_len " + n1;
		
		// get an input (optional) - min views for a video
		System.out.println("30000000\n");
		System.out.println("Enter min views (Optional) for video: [n], like the example above:");
		try {
			n2 = Integer.parseInt(INPUT_READER.next());
		} catch (NoSuchElementException|IllegalStateException
				|InvalidPathException|NumberFormatException e) {
			// TODO: handle exception
			n2 = 0;
		}
		array[2] = "min_views " + n2;
		
		return array;
	}
	
	/**
	 * scrape 'youtube.com' by the given arguments, and download required videos to MP3 files, 
	 * using 'onlinevideoconverter.com' (convert youtube url to mp3).
	 * @param args - array (String[]) that should include: 
	 *  1. Path to a text file with search terms for youtube.
	 *  2. Optional - maximum length (in minutes) for videos to download.
	 *  3. Optional - minimum views for videos to download.
	 * 
	 * The output: music files (mp3) in the download directory of Chrome browser 
	 * (if the the process went properly).
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		
		String[] arr;  
		
		// get inputs from user or from @param args
		if (args.length > 0) {
			arr = new String[args.length];  // new array in length of 'args' array
			System.arraycopy(args, 0, arr, 0, args.length );  // copy array 'args' to 'arr'
		}
		else {
			arr = ret_inputs_to_array();  // get inputs from user (and adds to an array)
		}
		
		// array's length is greater than 0
		if (arr.length > 0)
		{
			String file_path = arr[0];
			
			// if file exists (in path) and not empty
			if (file_exists_and_not_empty(file_path))
			{
				int max_duration = 0, min_views = 0;
				
				if (2 <= arr.length && arr.length <= 3)  // array's length is 2 or 3
				{
					for (int i = 1; i < arr.length; i++)  // for loop over array
					{
						try
						{
							// given arg starts with the specified prefix
							if (arr[i].startsWith("max_len ")) {
								// trying to parse max_duration from the given arg (string to int)
								max_duration = Integer.parseInt(arr[i].replace("max_len ", ""));
							}
							// given arg starts with the specified prefix
							else if (arr[i].startsWith("min_views ")) {
								// trying to parse minimum views from the given arg (string to int)
								min_views = Integer.parseInt(arr[i].replace("min_views ", ""));
							}
							
					    } catch (NumberFormatException e) {
					    	System.err.println("This argument must be an integer.");
					    }
					}
					
				}
				if (max_duration <= 0) {
					max_duration = 10;  // set default maximum length of video as 10 minutes
				}
				if (min_views <= 0) {	
					min_views = 15000000;  // set default minimum views on video as 15M
				}
					
				// read lines from file (in given path), and add file's lines to a list
				LinkedList<String> keywords_list = ret_lines_from_file(file_path);
				
				sleep(10, "sec");  // delay in seconds
				
				if (!keywords_list.isEmpty())  // if the list is not empty
				{
					// scrape youtube and download required links to mp3
					scrape_youtube(keywords_list, max_duration, min_views);
				}	
			}	
		}	
	}	

}
