package fuzz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Fuzzer {

	private List<Page> _pages;
	private List<String> _cookies;
	private ArrayList<String> _commonWords = new ArrayList<String>();
	
	private CookieManager cm;
	
	private WebClient client;
    private String baseUrl;
	
	public Fuzzer(String commonWordsFile){
		_pages = new ArrayList<Page>();
		_cookies = new ArrayList<String>();
		_commonWords = loadCommonWordsFile(commonWordsFile);
		cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
	    CookieHandler.setDefault(cm);
		client = new WebClient();
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
	}
	
	public ArrayList<String> loadCommonWordsFile(String fileName){
		Scanner input;
		try {
			input = new Scanner(new File(fileName));
			while (input.hasNext()) {
				String s = input.nextLine();
                _commonWords.add(s);
                _commonWords.add(s + ".php");
                _commonWords.add(s + ".jsp");
			}
		} catch (FileNotFoundException e) {
			System.err.println("common words error: " + e.getMessage());
		}
		return _commonWords;
	}
	
	private void discoverPages(){
		//get links via recursion
		discoverLinks(baseUrl, parseURL(baseUrl));
		//initial page guessing
		guessPages();
	}
	
	private void guessPages(){
		for(String guess: _commonWords){
			try{
				HtmlPage html = client.getPage(baseUrl + "/" + guess);
				System.out.println("DISCOVER - Valid URL guessed: " + baseUrl + "/" + guess);
				Page page = parseURL(baseUrl + "/" + guess);
			} catch (FailingHttpStatusCodeException e) {
				System.err.println("DISCOVER - Guessed url cannot be reached: " + baseUrl + "/" + guess);
			} catch (MalformedURLException e){
				System.err.println("DISCOVER - Guessed url was invalid: " + baseUrl + "/" + guess);
			} catch (IOException e){
				System.err.println("DISCOVER - Guessed url resulted in an error: " + baseUrl + "/" + guess);
			}
		}
	}
	
	private void discoverForms(){
		//find all forms from each page discovered
		for (Page page: _pages){
			try{
				HtmlPage html = client.getPage(this.baseUrl + "/" + page.getURL());
				List<HtmlForm> forms = html.getForms();
				for (HtmlForm form: forms){
					page.getForms().add(form);
				}
				
			} catch (FailingHttpStatusCodeException e){
				System.err.println("DISCOVER - The URL guessed was invalid: " + e.getMessage());
			} catch (MalformedURLException e){
				System.err.println("DISCOVER - The URL guessed violated URL convention: " + e.getMessage());
			} catch (IOException e){
				System.err.println("DISCOVER - Error during page guessing: " + e.getMessage());
			}
		}
	}
	
	private void discoverLinks(String url, Page page){
		try{
			HtmlPage html = client.getPage(url);
			List<HtmlAnchor> links = html.getAnchors();
			for (HtmlAnchor link: links){
				URL temp_url = new URL(this.baseUrl + "/" + link.getHrefAttribute());
				if (!_pages.contains(new Page(temp_url.getPath()))) {
					Page temp_page = parseURL(this.baseUrl + "/" + link.getHrefAttribute());
					//discoverCookies(temp_url);
					discoverLinks(this.baseUrl + "/" + link.getHrefAttribute(), temp_page);
				}
				else {
					parseURL(this.baseUrl + "/" + link.getHrefAttribute());
				}
			}
		} catch (FailingHttpStatusCodeException e){
			System.err.println("DISCOVER - Invalid link: " + e.getMessage());
		} catch (MalformedURLException e){
			System.err.println("DISCOVER - Invalid link: " + e.getMessage());
		} catch (IOException e){
			System.err.println("DISCOVER - Invalid link: " + e.getMessage());
		}
	}
	
	private Page parseURL(String url){
		Page page = null;
		try{
			URL temp = new URL(url);
			//if page is already in the list
			if (_pages.contains(new Page(temp.getPath()))) {
				for (Page p: _pages){
					if (p.getURL().equals(temp.getPath())) {
						page = p;
						break;
					}
				}
			}
			else {
				page = new Page(temp.getPath());
				_pages.add(page);
			}
			//get query and find fizzed inputs
			if (temp.getQuery() != null) {
				for (String query: temp.getQuery().split("&")) {
					String input = query.split("=")[0];
					if (!page.getInputs().contains(input)) {
						page.getInputs().add(input);
					}
				}
			}
		} catch (MalformedURLException e) {
			System.err.println("Invalid URL provided: " + e.getMessage());
		}
		return page;
	}
	
	private void discoverCookies(URL url){
		CookieStore cookieJar = cm.getCookieStore();
		List<HttpCookie> cookies = cookieJar.getCookies();
		for(HttpCookie c: cookies){
			if(!_cookies.contains(c)){
				_cookies.add(c.getName() +": " + c.getPath());
			}
		}
	}
	
	private void printDiscovery(){
		System.out.println("\nCookies: " + _cookies.size());
		for(String c: _cookies){
			System.out.println("\tcookie: " + c);
		}
		System.out.println("\nValid pages discovered: " +_pages.size());
		for(Page p: _pages){
			System.out.println("url: " + p.getURL());
			System.out.println("\tURL Inputs: " + p.getInputs().size());
			for(String url: p.getInputs()){
				System.out.println("\t\turl: " + url);
			}
			System.out.println("\tForm Inputs: " + p.getForms().size());
			for(HtmlForm form: p.getForms()){
				System.out.println("\t\tform: " + form);
			}
		}
	}
	
	public void logIn(String keyword) {
        HtmlPage logIn = null;
        HtmlForm logInForm = null;

        if(keyword.toLowerCase().equals("dvwa")) {
            //connect to the login page for dvwa
            try {
            logIn = client.getPage("http://127.0.0.1/dvwa/login.php");
            } catch(Exception e) {
                e.printStackTrace();
            }
            //form[@action=login.php]
            //acquire login form, set values
            logInForm = logIn.getFirstByXPath("/html/body/div/form");
            logInForm.getInputByName("username").setValueAttribute("admin");
            logInForm.getInputByName("password").setValueAttribute("password");
            try{
                logInForm.getInputByName("Login").click();
            } catch (IOException e){
                System.out.println("Error logging in: " + e.getMessage());
            }
            
        } else if(keyword.toLowerCase().equals("bodgeit")) {
            //connect to the login page for the bodgeit application
            try {
            logIn = client.getPage("http://127.0.0.1:8080/bodgeit/register.jsp");
            }catch (Exception e) {
                e.printStackTrace();
            }
            
            //acquire login form, set values
            logInForm = logIn.getFirstByXPath(".//form[@method='POST']");
            logInForm.getInputByName("username").setValueAttribute("notarealemail@sharklasers.com");
            logInForm.getInputByName("password1").setValueAttribute("password");
            logInForm.getInputByName("password2").setValueAttribute("password");
            
            //then click the submit button, and check for success or failure
            try {
            logInForm.getInputByValue("Register").click();
            }catch(IOException e) {}
            
        } else {
            System.out.println("No hardcoded information for " + keyword +". Continuing the fuzz without login");
        }
    }
	
	public void fuzz(String baseUrl, String auth){
        this.baseUrl = baseUrl;
		System.out.println("Starting discovery process for: " + baseUrl);
		discoverPages();
        logIn(auth);
		discoverForms();
		printDiscovery();
        client.closeAllWindows();
	}
}
