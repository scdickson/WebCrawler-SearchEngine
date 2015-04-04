import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;


public class SubdomainGraph 
{
	public SubdomainGraph()
	{
		try 
		{
			HashMap<String, Integer> mapping = new HashMap<String, Integer>();
			Connection connection = DriverManager.getConnection( "jdbc:mysql://localhost:9999/CRAWLER", "root", "kpcofgs");
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT urlid, url FROM urls");
			while(rs.next())
			{
				try
				{
					String sd = getBaseURL(rs.getString("url"));
					Integer i = mapping.get(sd);
					if(i == null)
					{
						mapping.put(sd,1);
					}
					else
					{
						mapping.remove(sd);
						mapping.put(sd, i+1);
					}
				}
				catch(Exception e){e.printStackTrace();}
			}
			
			for(String s : mapping.keySet())
			{
				System.out.println(s + "\t" + mapping.get(s));
			}
			statement.close();
			connection.close();
			
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getBaseURL(String url)
	{
	   String subdomain = null;
	   try
	   {
	     subdomain = new URI(url).getHost();
	   }
	   catch(Exception e){}
	   
	   return subdomain.replace("www.","");
	}
	
	public static void main(String args[])
	{
		SubdomainGraph sg = new SubdomainGraph();
	}
}
