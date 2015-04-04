import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CrawlThread implements Callable
{
	Database database;
	ArrayList<String> urlQueryComp;
	String url;
	int urlIndex;
	int threadID;
	long init_time;
	
	public CrawlThread(Database database)
	{
		this.database = database;
	}
	
	public void setAttributes(String url, int urlIndex, int threadID)
	{
		this.url = url;
		this.urlIndex = urlIndex;
		this.threadID = threadID;
	}
	
	public ArrayList<String> call()
	{
		try
		{
			init_time = System.currentTimeMillis();
	        Document doc = Jsoup.connect(url).timeout(Constants.CONNECT_TIMEOUT).get();
	        String title = doc.title().replace("'","''");
	        Elements links = doc.select("a[href]");
	        String body = doc.body().text();
	        String desc = Utils.deepScanForDescription(doc);
	        String first_img = Utils.deepScanForImg(doc, url);
        
	        try
	        {
	        	desc = desc.substring(0, Constants.MAX_DESC_CHARS-3) + "...";
	        }
	        catch(StringIndexOutOfBoundsException e)
	        {
	        	//Body was empty or smaller than MAX_DESC_CHARS in length
	        }
	        
	        database.updateURL(urlIndex, desc, title, first_img, links.size());
	        urlQueryComp = new ArrayList<String>();
	        String mySubdomain = Utils.getSubdomain(url);
	        for (Element link : links) 
	        {
	        	String childURL = Utils.urlStripExtra(link.attr("abs:href"));
	        	
	        	if(Utils.linkValid(childURL, Crawler.DOMAIN))
		        {
	        		database.updateUrlIncomingLink(childURL);
	        		String component = "(\"" + childURL + "\"";
	        		urlQueryComp.add(component);
	        		String otherSubdomain = Utils.getSubdomain(childURL);
	        		if(!mySubdomain.equals(otherSubdomain))
	        		{
	        			database.updateSubdomainConnection(mySubdomain, otherSubdomain);
	        		}
		        }
	        }
	        
	        Object all_words[] = new HashSet<String>(Arrays.asList(body.split(" "))).toArray();
	        ArrayList<String> wordQueryComp = new ArrayList<String>();
	        for(Object obj : all_words)
	        {
	        	String word = obj.toString();
	        	word = word.replace("'", "''");
	        	word = word.replace("\"", "\\\"");
	        	if(Utils.isValidWord(word))// && !database.wordExists(word, urlIndex))
	        	{
	        		wordQueryComp.add("(\"" + word + "\", " + urlIndex + ")");
	        		//database.insertWord(urlIndex, word);
	        	}
	        }
	        database.insertWords(wordQueryComp);
	        }
		catch(IllegalArgumentException iae)
		{
			System.err.println(iae.getMessage() + ": " + url + " (" + urlIndex + ")");
		}
		catch(org.jsoup.HttpStatusException hse)
		{
			//we got a 404 probably.
			System.err.println("URL \"" + url + "\" could not be found (404).");
			try
			{
				database.removeURL(urlIndex);
			}
			catch(Exception e){}
		}
		catch(Exception e)
		{
			try
			{
				database.removeURL(urlIndex);
			}
			catch(Exception ex){}
			
			e.printStackTrace();
			System.err.println("\t-->" + url);
		}
		finally
		{
			try
			{
				database.closeConnection();
			}
			catch(Exception ex){}
			
			if(url != null)
			{
				long end_time = System.currentTimeMillis();
				double diff = (end_time - init_time)/1000.0;
				System.err.println("Thread #" + threadID + " finished processing " + url + " in " + diff + " seconds");
			}	
		
		}
		
		return urlQueryComp;
	}
}
