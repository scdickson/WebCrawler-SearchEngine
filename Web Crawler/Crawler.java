import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Crawler
{
  Database database;
  ExecutorService pool;
  Set<Future<ArrayList<String>>> thread_results;
  Set<String> overflow;
  
  public static volatile int current_urlid, max_urlid;
  public static int MAX_URLS = 1000;
  public static ArrayList<String> URL_LIST = new ArrayList<String>();
  public static String DOMAIN = null;
  
  public Crawler()
  {
    try
    {
    	long init_time = System.currentTimeMillis();
    	System.err.print("New Crawler Using:\n-Max URLs: " + MAX_URLS + "\n-Domain: " + DOMAIN + "\n-URLs: ");
    	for(String url : URL_LIST)
    	{
    		System.err.print(url + "\n       ");
    	}
    
      database = new Database();
      database.openConnection();
      
      initialize();
      crawl();
      database.createIndexes();
      
      database.closeConnection();

      long end_time = System.currentTimeMillis();
      double diff = (end_time - init_time)/1000.0/60.0;
      
      System.err.print("Crawl of domain " + DOMAIN + " from ");
      for(String url : URL_LIST)
  		{
  		System.err.print(url + ", ");
  		}
      System.err.println("with a depth of " + MAX_URLS + " took " + diff + " minutes.");
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void initialize()
  {
	  try
	  {
		  database.createDB();
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
	  }
	  
      current_urlid = 0;
      max_urlid = 0;
      overflow = new HashSet<String>();
      
      for(String url : URL_LIST)
      {
        try
        {
          database.insertURL(max_urlid, url);
          max_urlid++;
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
  }
  
  public void crawl()
  {
	  int threadID = 1;
	  
      while(current_urlid < max_urlid)
      {
    	  try
    	  {
    		  int limit = max_urlid - current_urlid;
    		  	if(limit > Constants.MAX_CONCURRENT_THREADS) 
    		  	{
    		  		limit = Constants.MAX_CONCURRENT_THREADS;
    		  	}
    		  	 System.err.println("Starting job " + (current_urlid + 1) + "/" + max_urlid + " (limit " + limit + ")");
    		  	thread_results = new HashSet<Future<ArrayList<String>>>();
    		  	pool = Executors.newFixedThreadPool(Constants.MAX_CONCURRENT_THREADS);
    		  	
	    		  	for(int i = 0; i < limit; i++)
	    		  	{
		  	        	String url = database.getURL(current_urlid);
		  	        	if(url != null)
		  	        	{
			  	        	Database newDB = new Database();
			  	        	newDB.openConnection();
			  	        	CrawlThread tmp = new CrawlThread(newDB);
			  	        	tmp.setAttributes(url, current_urlid, (current_urlid + 1));
			  	        	current_urlid++;
			  	        	Future<ArrayList<String>> future = pool.submit(tmp);
			  	        	thread_results.add(future);
		  	        	}
		  	        	else
		  	        	{
		  	        		//System.err.println("[" + current_urlid + ": N]");
		  	        		current_urlid++;
		  	        		MAX_URLS++;
		  	        	}
	    		  	}
	    		  	pool.shutdown();
	    		    while(!pool.isTerminated()){}
	    		  	
	    		  	ArrayList<String> result = null;
	    		  	try
	    		  	{
		    		  	for(Future<ArrayList<String>> future : thread_results)
		    		  	{
		    		  		result = future.get();
		    		  		if(result != null && max_urlid <= MAX_URLS)
		    		  		{
			    		  		int i = 0;
			    		  		for(String partial : result)
			    		  		{
			    		  			if(max_urlid <= MAX_URLS)
			    		  			{
				    		  			partial += ", " + max_urlid++ + ")";
				    		  			result.set(i++, partial);
			    		  			}
			    		  			else
			    		  			{
			    		  				result.subList(i,result.size()).clear();
			    		  				break;
			    		  			}
			    		  		}
			    		  		
			    		  		if(result != null && result.size() > 0)
			    		  		{
			    		  			database.insertURLs(result);
			    		  		}
		    		  		}
		    		  	}
	    		  	}
	    		  	catch(Exception e)
	    		  	{
	    		  		e.printStackTrace();
	    		  	}  
	    		  	pool = null;
    	  }
      catch(Exception e)
      {
        e.printStackTrace();
        continue;
      }
      finally
      { 	  
    		  //eh
      }
     }
      
  }
  
  public static void main(String args[])
  {
    
    if(args.length < 3)
    {
      System.err.println("Syntax: java Crawler [-u maxurls] [-d domain] url-list");
      System.exit(0);
    }
    else
    {
      try
      {
        for(int i = 0; i < args.length; i++)
        {
          if(args[i].equalsIgnoreCase("-u"))
          {
            MAX_URLS = Integer.parseInt(args[++i]);
          }
          else if(args[i].equalsIgnoreCase("-d"))
          {
            DOMAIN = args[++i];
          }
          else
          {
            URL_LIST.add(args[i]);
          }
        }
      }
      catch(Exception e)
      {
        System.err.println("Syntax: java Crawler [-u maxurls] [-d domain] url-list");
        System.exit(0);
      }
    }
    
    if(DOMAIN == null || URL_LIST.size() == 0)
    {
      System.err.println("Error. Must specify at least one domain and one root to crawl:");
      System.err.println("Syntax: java Crawler [-u maxurls] [-d domain] url-list");
      System.exit(0);
    }
    else
    {
      Crawler crawler = new Crawler();
    }
  }
}