package fuzz;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

public class Utils {

	private List<Page> pages;
	private List<String> cookies;
	private List<String> inputs;
	private List<String> commonWords;
	
	private CookieManager cm;
	
	private WebClient client;
	
	public Utils(){
		pages = new ArrayList<Page>();
		cookies = new ArrayList<String>();
		inputs = new ArrayList<String>();
		commonWords = new ArrayList<String>();
		cm = new CookieManager();
		client = new WebClient();
		client.setCookieManager(cm);
	}
	
	public void loadCommonWordsFile(String fileName) throws IOException{
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
		}
	}
	
	private void discoverPages(String baseUrl){
		//get links recursively
		discoverLinks(baseUrl, breakdownURL(baseUrl));
		
		//start page guessing process
		for(String guess: commonWords){
			try{
				HtmlPage html = client.getPage(baseUrl + "/" + guess);
				System.out.println("valid guess found");
				Page page = breakdownURL(baseUrl + "/" + guess);
			} catch (FailingHttpStatusCodeException e) {
				System.err.println("Guessed url cannot be reached: " + baseUrl + "/" + guess);
			} catch (MalformedURLException e){
				System.err.println("Guessed url was invalid: " + baseUrl + "/" + guess);
			} catch (IOException e){
				System.err.println("Guessed url resulted in an error: " + baseUrl + "/" + guess);
			}
		}
	}
	
	private void discoverInputs(String baseUrl){
		for (Page page: pages){
			try{
				HtmlPage html = client.getPage(baseUrl + page.getURL());
				List<HtmlForm> forms = html.getForms();
				for (HtmlForm form: forms){
					page.getForms().add(form);
				}
			} catch (FailingHttpStatusCodeException e){
				System.err.println("Invalid form discovered: " + e.getMessage());
			} catch (MalformedURLException e){
				System.err.println("Invalid url provided during form discovery: " + e.getMessage());
			} catch (IOException e){
				System.err.println("Invalid form discovered: " + e.getMessage());
			}
		}
	}
	
	private void discoverLinks(String baseUrl, Page page){
		try{
			HtmlPage html = client.getPage(baseUrl);
			List<HtmlAnchor> links = html.getAnchors();
			for (HtmlAnchor link: links){
				URL temp_url = new URL(baseUrl + "/ " + link.getHrefAttribute());
				if (!pages.contains(new Page(temp_url.getPath()))) {
					Page temp_page = breakdownURL(baseUrl + "/" + link.getHrefAttribute());
					discoverLinks(baseUrl + "/" + link.getHrefAttribute(), temp_page);
				}
				else {
					breakdownURL(baseUrl + "/" + link.getHrefAttribute());
				}
			}
		} catch (FailingHttpStatusCodeException e){
			System.err.println("Invalid link: " + e.getMessage());
		} catch (MalformedURLException e){
			System.err.println("Invalid link: " + e.getMessage());
		} catch (IOException e){
			System.err.println("Invalid link: " + e.getMessage());
		}
	}
	
	private Page breakdownURL(String baseUrl){
		Page page = null;
		try{
			URL url = new URL(baseUrl);
			//if page is already in the list
			if (pages.contains(new Page(url.getPath()))) {
				for (Page check: pages){
					if (check.getURL().equals(url.getPath())) {
						page = check;
						break;
					}
				}
			}
			else {
				page = new Page(url.getPath());
				pages.add(page);
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
	
	//cookie entry = "cookie name: cookie value"
	public void populateCookies(String baseUrl){
		Set<Cookie> c = cm.getCookies();
		for (Cookie cookie: c){
			String cookie_entry = cookie.getName() + ": " + cookie.getValue();
			if(!cookies.contains(cookie_entry))
				cookies.add(cookie_entry);
		}
	}
	
	//input entry = "input element id: input element text"
	public void populateInputs(Document doc){
		for (Element ele: doc.getElementsByTag("input")){
			String entry = ele.id() + ": " + ele.text();
			if(!inputs.contains(entry)){
				inputs.add(entry);
			}
		}		
	}
}
