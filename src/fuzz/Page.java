package fuzz;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class Page {

	private String url;
	private List<HtmlForm> forms;
	private List<String> inputs;
	
	public Page(){
		url = new String();
		forms = new ArrayList<HtmlForm>();
		inputs = new ArrayList<String>();
	}
	
	public Page(String url){
		this.url = url;
		forms = new ArrayList<HtmlForm>();
		inputs = new ArrayList<String>();
	}
	
	public List<HtmlForm> getForms(){
		return forms;
	}
	
	public List<String> getInputs(){
		return inputs;
	}
	
	public String getURL(){
		return url;
	}
	
	public void setURL(String url){
		this.url = url;
	}
	
	public boolean equals(Object o){
		if (o instanceof Page){
			if (url.equals(((Page) o).getURL())){
				return true;
			}
		}
		return false;
	}
}
