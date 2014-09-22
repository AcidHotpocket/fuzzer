package fuzz;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Utils {

	private ArrayList<String> urlList = new ArrayList<String>();
	private ArrayList<String> commonWords = new ArrayList<String>();
	
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
	
	public void populateUrlList(String baseUrl) throws IOException{
		//start at the baseUrl and get all pages it can reach
		Document doc = Jsoup.connect(baseUrl).get();
		Elements links = doc.select("a[ref]");
		Elements media = doc.select("[src]");

		for (Element link: links){
			String link_entry = "link: <" + link.attr("abs:href") + "> (" + link.text() + ")";
			if(!urlList.contains(link_entry))
				urlList.add(link_entry);
		}
		for (Element src: media){
			if(src.tagName().equals("img")){
				String img_entry = "img: <" + src.attr("abs:src") +"> " + src.attr("width") +
						"x" + src.attr("height") + "(" + src.attr("alt") + ")";
				if(!urlList.contains(img_entry))
					urlList.add(img_entry);
			}
			else{
				String src_entry = src.tagName() + ": <" + src.attr("abs:src") + ">";
				if(!urlList.contains(src_entry))
					urlList.add(src_entry);
			}
		}
		//for each url it can reach, call populateUrlList(url)?
	}
	
	public void discoverInputs(String currentUrl){
		
	}
	
	public ArrayList<String> printUrlList(){
		if(urlList.isEmpty())
			return null;
		else
			return urlList;
	}
}
