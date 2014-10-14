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
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import javax.lang.model.element.Element;

public class Fuzzer {

	private List<Page> _pages;
	private List<String> _cookies;
	private ArrayList<String> _commonWords = new ArrayList<String>();
    private ArrayList<String> _sensitiveData = new ArrayList<String>();
    private ArrayList<String> _vectors = new ArrayList<String>();
    private boolean randomFlag = false;
    private Random random;
	
	private CookieManager cm;
	
	private WebClient client;
    private String baseUrl;
	
	public Fuzzer(String commonWordsFile, String vectorsFile, String sensitiveDataFile, boolean randomFlag){
		_pages = new ArrayList<Page>();
		_cookies = new ArrayList<String>();
		_commonWords = loadCommonWordsFile(commonWordsFile);
        _sensitiveData = loadSensitiveDataFile(sensitiveDataFile);
        _vectors = loadVectorsFile(vectorsFile);
        this.randomFlag = randomFlag;
		cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
	    CookieHandler.setDefault(cm);
		client = new WebClient();
        random = new Random();
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
			System.err.println("common words file error: " + e.getMessage());
		}
		return _commonWords;
	}

    public ArrayList<String> loadSensitiveDataFile(String fileName){
        Scanner input;
        try {
            input = new Scanner(new File(fileName));
            while (input.hasNext()) {
                String s = input.nextLine();
                _sensitiveData.add(s);
            }
        } catch (FileNotFoundException e) {
            System.err.println("sensitive data file error: " + e.getMessage());
        }
        return _sensitiveData;
    }

    public ArrayList<String> loadVectorsFile(String fileName){
        Scanner input;
        try {
            input = new Scanner(new File(fileName));
            while (input.hasNext()) {
                String s = input.nextLine();
                _vectors.add(s);
            }
        } catch (FileNotFoundException e) {
            System.err.println("sensitive data file error: " + e.getMessage());
        }
        return _vectors;
    }

	private void discoverPages(){
		//get links via recursion
		discoverLinks(baseUrl, parseURL(baseUrl));
		//initial page guessing
        for(String guess: _commonWords){
            try{
                HtmlPage html = client.getPage(baseUrl + "/" + guess);
                System.out.println("DISCOVER - Valid URL guessed: " + baseUrl + "/" + guess);
                Page p = parseURL(baseUrl + "/" + guess);
                checkForBadData(p, html);
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
		for (Page p: _pages){
			try{
				HtmlPage html = client.getPage(this.baseUrl + "/" + p.getURL());
                checkForBadData(p, html);
				List<HtmlForm> forms = html.getForms();
				for (HtmlForm form: forms){
					p.getForms().add(form);
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
            checkForBadData(page, html);
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

    private List<HtmlInput> parseDOM(DomNode node){
        List<HtmlInput> inputs = new ArrayList<HtmlInput>();

        for (DomNode n : node.getChildren()) {
            if( n instanceof HtmlTextInput){
                inputs.add((HtmlInput) n);
            }else if(n instanceof HtmlHiddenInput){
                inputs.add((HtmlInput) n);
            }else if(n instanceof HtmlFileInput){
                inputs.add((HtmlInput) n);
            }else if(n instanceof HtmlPasswordInput){
                inputs.add((HtmlInput) n);
            }else if(n instanceof HtmlImageInput){
                inputs.add((HtmlInput) n);
            }

            if (n.hasChildNodes()){
                inputs.addAll(parseDOM(n));
            }
        }

        return inputs;
    }

    private void checkForBadData(Page page, HtmlPage content){
        for(String s: _sensitiveData){
            if(content.asXml().contains(s) && !page.getSensitiveData().contains(s)){
                page.getSensitiveData().add(s);
            }
        }
    }

    private List<HtmlInput> getFormInputs(HtmlForm form){
        List<HtmlInput> inputs = new ArrayList<HtmlInput>();
        for(DomNode node: form.getChildren()){
            inputs.addAll(parseDOM(node));
        }
        return inputs;
    }

    private HtmlSubmitInput getFormSubmitInput(HtmlForm form) throws ElementNotFoundException{
        for(DomNode node: form.getChildren()){
            try{
                return getFormSubmitInput(node);
            } catch (ElementNotFoundException e){
            }
        }
        throw new ElementNotFoundException("", "", "");
    }

    private HtmlSubmitInput getFormSubmitInput(DomNode node) throws  ElementNotFoundException {
        if (node instanceof HtmlSubmitInput){
            return (HtmlSubmitInput) node;
        }
        else if(node.hasChildNodes()){
            for(DomNode n: node.getChildren()){
                try{
                    return getFormSubmitInput(n);
                } catch (ElementNotFoundException e){
                }
            }
        }
        throw new ElementNotFoundException("", "", "");
    }

    private void testStart(){
        if(!randomFlag){
            for(Page p: _pages) {
                testFullPage(p);
            }
        } else {
            for(int i = 0; i < 10; i++){
                int page = random.nextInt(_pages.size());
                testRandomPage(_pages.get(page));
            }
        }
    }

    private void testFullPage(Page page){
        for(String input: page.getInputs()){
            String base = baseUrl + page.getURL() + "?" + input + "=";
            for(String s: _vectors){
                try{
                    System.out.println("Input Test - Attempting to access: " + base + s);
                    HtmlPage html = client.getPage(base + s);
                    checkForBadData(page, html);
                } catch (FailingHttpStatusCodeException e){
                    System.err.println("Input Test Failed: " + e.getMessage());
                } catch (MalformedURLException e){
                    System.err.println("Input Test Failed: " + e.getMessage());
                } catch (IOException e){
                    System.err.println("Input Test Failed: " + e.getMessage());
                }
            }
        }

        for(HtmlForm form: page.getForms()){
            try{
                List<HtmlInput> htmlInputs = getFormInputs(form);
                HtmlSubmitInput submit = getFormSubmitInput(form);
                for(String input: _vectors){
                    for(HtmlInput htmlInput: htmlInputs){
                        htmlInput.setValueAttribute(input);
                    }
                    try{
                        System.out.println("Full Input Test - Submitting form for: " + page.getURL());
                        HtmlPage html = submit.<HtmlPage> click();
                        checkForBadData(page, html);
                    } catch (IOException e) {
                        System.err.println("Full Input Test - Form Submission failed: " + e.getMessage());
                    }
                }
            } catch (ElementNotFoundException e){
                System.err.println("Full Input Test - Unable to find submit button for the form on: " + page.getURL());
            }
        }
    }

    private boolean testRandomPage(Page page){
        boolean isSelect = false;
        if(page.getForms().size() > 0 && page.getInputs().size() > 0){
            int rand = random.nextInt(2);
            if(rand > 0){
                isSelect = true;
            }
        } else if (page.getForms().size() > 0){
            isSelect = true;
        } else if(page.getInputs().size() > 0){
            isSelect = false;
        } else {
            return false;
        }

        if(isSelect){
            try{
                HtmlForm form = page.getForms().get(random.nextInt(page.getForms().size()));
                List<HtmlInput> inputs = getFormInputs(form);
                HtmlSubmitInput submit = getFormSubmitInput(form);

                for(HtmlInput input: inputs){
                    input.setValueAttribute(_vectors.get(random.nextInt(_vectors.size())));
                }

                HtmlPage html = submit.<HtmlPage> click();
                checkForBadData(page, html);
            } catch (ElementNotFoundException e){
                System.err.println("Random Input Test - Submit Button not found on page " + page.getURL());
            } catch (IOException e){
                System.err.println("Random Input Test failed on page " + page.getURL() + " for: " + e.getMessage());
            }
        } else {
            String base = baseUrl + page.getURL() + "?" + page.getInputs().get(random.nextInt(page.getInputs().size()));
            String input = _vectors.get(random.nextInt(_vectors.size()));
            base = base.concat(input);
            try{
                System.out.println("Attempting to access url: " + base);
                HtmlPage html = client.getPage(base);
                checkForBadData(page, html);
            } catch (FailingHttpStatusCodeException e){
            } catch (MalformedURLException e){
            } catch (IOException e){
            }
        }

        return true;
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
	
	private void print(){
		/*System.out.println("\nCookies: " + _cookies.size());
		for(String c: _cookies){
			System.out.println("\tcookie: " + c);
		}*/

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
            System.out.println("\tSensitive data leaked: " + p.getSensitiveData().size());
            for (String data : p.getSensitiveData()) {
                System.out.println("\t\t" + data);
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
        testStart();
		print();
        client.closeAllWindows();
	}
}
