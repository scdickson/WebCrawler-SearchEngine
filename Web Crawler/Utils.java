import java.net.URI;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class Utils 
{
	public static String makeAbsoluteURL(String url, String parentURL) 
	{
		StringBuilder result = new StringBuilder();
		result.append(parentURL);
		
		int index = url.indexOf(parentURL);
		if(index > 0)
		{
			result.append(url.substring(index));
		}
		else
		{
			if(url.charAt(0) == '/' || parentURL.charAt(parentURL.length()-1) == '/')
			{
				result.append(url);
			}
			else
			{
				result.append("/");
				result.append(url);
			}
		}
		
		return result.toString();
	}
	
	public static String getSubdomain(String url)
	{
		String subdomain = null;
		   try
		   {
		     subdomain = new URI(url).getHost();
		   }
		   catch(Exception e){}
		   
		   return subdomain.replace("www.","");
	}
	
	public static String getBaseURL(String url)
	 {
	    String prefix = "";
	    if(url.contains("http://") || url.contains("https://"))
	    {
	      prefix = url.substring(0, url.indexOf("//")+2);
	      url = url.replace("http://", "");
		  url = url.replace("https://", "");
	    }
	  
	  if(url.contains("/"))
	  {
	   url = url.substring(0, url.indexOf("/"));
	  }
	  url = prefix + url;
	  return url;
	 }
	
	public static String getAbsImageFromRel(String relativeImg, String pageURL)
	  {
	    String prefix = "";
	    String result = "";
	    
	    if(pageURL.contains("http://") || pageURL.contains("https://"))
	    {
	        prefix = pageURL.substring(0, pageURL.indexOf("//")+2);
	        pageURL = pageURL.replace("http://", "");
	        pageURL = pageURL.replace("https://", "");
	    }
	    
	    if(pageURL.charAt(pageURL.length()-1) != '/')
	    {
	      pageURL = pageURL.substring(0, pageURL.lastIndexOf("/") + 0);
	    }
	    
	    if(relativeImg.contains("../"))
	    {
	      String findStr = "../";
	      int lastIndex = 0;
	      int count =0;
	      
	      while(lastIndex != -1)
	      {
	        lastIndex = relativeImg.indexOf(findStr,lastIndex); 
	        if( lastIndex != -1)
	        {
	          count ++;
	          lastIndex+=findStr.length();
	        }
	      }
	       String urlComp[] = pageURL.split("/");
	       result = prefix + urlComp[0];
	       int index = 1;
	       
	     for(int i = 1; i < (urlComp.length - count); i++)
	     {
	       result += "/" + urlComp[index++];
	     }
	     
	     relativeImg = relativeImg.replace("../", "");
	     if(relativeImg.charAt(0) != '/')
	     {
	       relativeImg = "/" + relativeImg;
	     }
	     result += relativeImg;
	     
	    }
	    else
	    {
	      if(relativeImg.charAt(0) != '/')
	      {
	        result = prefix + pageURL + "/" + relativeImg;
	      }
	      else
	      {
	        result = prefix + getBaseURL(pageURL) + relativeImg;
	      }
	    }
	    
	    return result;
	  }
	
	public static String deepScanForImg(Document doc, String url)
	{
		Element img;
		String src;
		
		try
		{
			img = doc.select("div.main").first().select("img").first();
		}
		catch(Exception e)
		{
			img = doc.select("img").first();
		}
		
		try
		{
			src = img.attr("src");
			
			if(src.contains("http://") || src.contains("https://"))
			{
				return src;
			}
			return getAbsImageFromRel(src, url);
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	
	public static String deepScanForDescription(Document doc)
	{
		String desc = "";
		
		try
		{
			String h1 = doc.body().select("h1").first().text();
			
			if(h1 != null)
			{
				desc += " " + h1;
			}
		}
		catch(Exception e){}
		
		try
		{
			String h1 = doc.body().select("h2").first().text();
			
			if(h1 != null)
			{
				desc += " " + h1;
			}
		}
		catch(Exception e){}
		
		try
		{
			String h1 = doc.body().select("h3").first().text();
			
			if(h1 != null)
			{
				desc += " " + h1;
			}
		}
		catch(Exception e){}
		
		try
		{
			String h1 = doc.body().select("p").first().text();
			
			if(h1 != null)
			{
				desc += " " + h1;
			}
		}
		catch(Exception e){}
		
		if(desc.equals(""))
		{
			desc = doc.body().text();
		}
		
		return desc.replace("'", "''");
	}
	
	public static String urlStripExtra(String url)
	{
		if(url.contains("?"))
		{
			url = url.substring(0, url.indexOf("?"));
		}
		
		if(url.contains("#"))
		{
			url = url.substring(0, url.indexOf("#"));
		}
		
		return url;
	}
	
	//We don't want to bother entering a row for a single punctuation mark...
	public static boolean isValidWord(String word)
	{
		if(Constants.INVALID_PUNCTUATION.contains(word))
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean linkValid(String url, String domain){
		 boolean inDomain = false;
		 boolean validType = false;
		 boolean validProtocol = false;
		 
		 try{
			 String linkDomain = new URI(url).getHost();
			 if(linkDomain.contains(domain)){
				 inDomain = true;
			 }
			 
			 String protocol = url.substring(0,url.indexOf(":"));
			 if(protocol.equals("http") || protocol.equals("https")){
				 validProtocol = true;
			 }
			 
			 int index = url.lastIndexOf(".");
			 if(index == -1){
				 if(url.charAt(url.length()) == '/'){
					 validType = true;
				 }
			 }
			 else{
				 String type = url.substring(index+1);
				 if(type.equals("htm") || type.equals("html"))
				 {
					 validType = true;
				 }
			 }
			 
		 }
		 catch(Exception e){
			 
		 }
		
		 return inDomain && validProtocol;// validType && validProtocol;
	 }
	
	public static boolean isInDomain(String url, String domain)
	 {
	   url = url.replace("http://", "");
	   url = url.replace("https://", "");
	   int index = url.indexOf(domain);
	   if(index == -1)
	   {
	     return false;
	   }
	   else
	   {
	     if(index == 0 || url.charAt(index-1) == '.')
	     {
	       return true;
	     }
	   }
	   
	   return false;
	 }
	
	public static boolean isValidProtocolAndType(String url)
	{
		boolean validProtocol = false;
		boolean validType = false;
		
		String protocol = url.substring(0, url.indexOf(":"));
		if(protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https"))
		{
			validProtocol = true;
		}
		
		int index;
	    for(index = url.length()-1; index >= 0; index--)
	    {
	      if(url.charAt(index) == '.')
	      {
	        break;
	      }
	    }
	    
	    String type = url.substring(index+1);
	    if(type.equals("html") || type.equals("htm") || type.contains("/"))
	    {
	    	validType = true;
	    }
		
		return (validProtocol);// && validType);
	}
}
