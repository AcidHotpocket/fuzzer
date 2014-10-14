package fuzz;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class Page {

	private String url;
	private List<HtmlForm> _forms;
	private List<String> _inputs;
    private List<String> _sensitiveData;
	
	public Page(){
		url = new String();
		_forms = new ArrayList<HtmlForm>();
		_inputs = new ArrayList<String>();
        _sensitiveData = new ArrayList<String>();
	}
	
	public Page(String url){
		this.url = url;
		_forms = new ArrayList<HtmlForm>();
		_inputs = new ArrayList<String>();
        _sensitiveData = new ArrayList<String>();
	}
	
	public List<HtmlForm> getForms(){
		return _forms;
	}
	
	public List<String> getInputs(){
		return _inputs;
	}

    public List<String> getSensitiveData(){
        return _sensitiveData;
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
