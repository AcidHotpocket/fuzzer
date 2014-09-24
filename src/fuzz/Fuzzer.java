package fuzz;

import java.io.BufferedReader;
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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Fuzzer {

	private List<Page> _pages;
	private List<String> _cookies;
	private ArrayList<String> commonWords;
	
	private CookieManager cm;
	
	private WebClient client;
	
	public Fuzzer(String commonWordsFile){
		_pages = new ArrayList<Page>();
		_cookies = new ArrayList<String>();
		commonWords = loadCommonWordsFile(commonWordsFile);
		cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
	    CookieHandler.setDefault(cm);
		client = new WebClient();
	}
	
	public ArrayList<String> loadCommonWordsFile(String fileName){
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null){
				if(!commonWords.contains(s)){
					commonWords.add(s);
				}
			}
			fr.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return commonWords;
	}
	
	private void discoverPages(String baseUrl){
		//get links via recursion
		discoverLinks(baseUrl, parseURL(baseUrl));
		//initial page guessing
		guessPages(baseUrl);
	}
	
	private void guessPages(String baseUrl){
		for(String guess: commonWords){
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
	
	private void discoverInputs(String baseUrl){
		//find all forms from each page discovered
		for (Page page: _pages){
			try{
				HtmlPage html = client.getPage(baseUrl + page.getURL());
				
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
	
	private void discoverLinks(String baseUrl, Page page){
		try{
			HtmlPage html = client.getPage(baseUrl);
			List<HtmlAnchor> links = html.getAnchors();
			for (HtmlAnchor link: links){
				URL temp_url = new URL(baseUrl + "/" + link.getHrefAttribute());
				if (!_pages.contains(new Page(temp_url.getPath()))) {
					Page temp_page = parseURL(baseUrl + "/" + link.getHrefAttribute());
					discoverCookies(temp_url);
					discoverLinks(baseUrl + "/" + link.getHrefAttribute(), temp_page);
				}
				else {
					parseURL(baseUrl + "/" + link.getHrefAttribute());
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
	
	private Page parseURL(String baseUrl){
		Page page = null;
		try{
			URL url = new URL(baseUrl);
			//if page is already in the list
			if (_pages.contains(new Page(url.getPath()))) {
				for (Page check: _pages){
					if (check.getURL().equals(url.getPath())) {
						page = check;
						break;
					}
				}
			}
			else {
				page = new Page(url.getPath());
				_pages.add(page);
			}
			
			//get query and find fizzed inputs
			if (url.getQuery() != null) {
				for (String query: url.getQuery().split("&")) {
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
			System.out.println("\t" + c);
		}
		System.out.println("\nValid pages discovered: ");
		for(Page p: _pages){
			System.out.println(p.getURL());
			System.out.println("URL Inputs: " + p.getInputs().size());
			for(String url: p.getInputs()){
				System.out.println("\t" + url);
			}
			System.out.println("Form Inputs: " + p.getForms().size());
			for(HtmlForm form: p.getForms()){
				System.out.println("\t" + form.getId() + ", " + form.getNameAttribute());
			}
		}
	}
	
	public void fuzz(String baseUrl){
		System.out.println("Starting discovery process for: " + baseUrl);
		discoverPages(baseUrl);
		discoverInputs(baseUrl);
		printDiscovery();
	}
}
