import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class SearchResults
 */
@WebServlet("/SearchResults")
public class SearchResults extends HttpServlet 
{
	private DataSource ds;
	private Connection connection;
	private static final long serialVersionUID = 1L;
	
	public static boolean pageSet = false;
	public static final int RESULTS_PER_PAGE = 10;
	public static int page_number = 1;
	public static double query_time;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchResults() 
    {
        super();
        try
        {
        	Context ctx = new InitialContext();
    		ds = (DataSource)ctx.lookup("java:comp/env/jdbc/db");
        	connection = ds.getConnection();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
    }
    
    public void doMultiWordSearch(String searchParams, PrintWriter out)
    {
    	String words[] = searchParams.split(" ");
    	String baseQuery = "SELECT DISTINCT * FROM urls JOIN (SELECT urlid, count(urlid) 'count' FROM WORDS WHERE";
        for(int i = 0; i < words.length; i++)
        {
         if(i < words.length && i > 0)
         {
          baseQuery += " OR";
         }
         
         baseQuery += " word='" + words[i] + "'";
        }
        baseQuery += " GROUP BY urlid) sub ON sub.urlid=urls.urlid AND sub.count > 1";
        
        long init_time = System.currentTimeMillis();
		int num_results = 0;
		out.println("<html><head>");
		out.println("<link rel=\"icon\" type=\"image/ico\" href=\"https://www.purdue.edu/purdue/images/favicon.ico\">");
		out.println("<link href='button.css' rel='stylesheet' type='text/css'>");
		out.println("<link href='textinput.css' rel='stylesheet' type='text/css'>");
		//out.println("<link href='table.css' rel='stylesheet' type='text/css'>");
		if(searchParams != null)
		{
			out.println("<title>Search Results: " + searchParams + "</title></head><body bgcolor=#ffffff><div align=\"left\">");
			out.println("<table><tr><td><a href=\"SearchEngine\"><img width=\"91\" height=\"50\" src=\"http://cs.purdue.edu/homes/scdickso/csearch.png\"/></a></td>");
			out.println("<form action=\"SearchResults\" method=GET>");
			out.println("<td><div class=\"myTextbox\"><input type=text size=80 name=query value=\"" + searchParams + "\" autofocus></div></td><td>  <input type=button class=\"myButton\" onClick=\"submit();\" value=\"Search\"></td>");
			out.println("</form></tr></table></hr>");
			
			try
			{
				  Statement stat = connection.createStatement();
				  ResultSet rs = stat.executeQuery(baseQuery.replace("DISTINCT *", "count(*) as count"));
				  rs.next();
				  num_results = rs.getInt("count");
				  
				  if(num_results <= 0)
				  {
					  out.println("<div align=\"center\"><h1>No Results Found.</h1></div>");
					  out.println("</body></html>");
					  return;
				  }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				  Statement stat = connection.createStatement();
				  ResultSet rs = stat.executeQuery(baseQuery);
				  query_time = (System.currentTimeMillis() - init_time)/1000.0;
				  
				  //int count = rs.getInt("count");
				  //out.println("<font face=\"arial\" color=#4D4344>Search returned " + count + " results.</font><hr/>");
				  out.println("<font face=\"arial\" color=#4D4344>Showing " + num_results  + " results (" + query_time + " seconds)</font><hr/>");
				    
				  while(rs.next())
				  {
					  try
					  {
					  String title = SEUtils.processTitle(rs.getString("title"));
					  String first_img = rs.getString("first_img");
					  String description = rs.getString("description");
					  out.println("<table border=0px>");
					  if(first_img != null && !first_img.equals(""))
					  {
						  out.println("<tr>");
						  	out.println("<td bgcolor=#ffffffD>");
						  		if(first_img.contains("/images/logo.svg"))
						  		{
						  			first_img = "https://www.cs.purdue.edu/images/logo-small.png";
						  		}
						  		out.println("<a href=\"" + first_img + "\" target=\"_blank\"><img src=\"" + first_img + "\" width=100 height=100 border=0px></a>");
						  	out.println("</td>");
					  }
					  		out.println("<td>");
					  			out.println("<table border=0px>");
					  				out.println("<tr>");
					  					out.println("<td>");
					  						out.println("<font face=\"arial\" color=#4D4344><a href=\"" + rs.getString("url") + "\" target=\"_blank\">" + title + "</a></font><br/>");
					  						out.println("<font face=\"arial\" color=#00C10D>" + rs.getString("url") + "</font><br/>");
					  					out.println("</td>");
					  				out.println("</tr>");
					  if(description != null)
					  {
						  			out.println("<tr>");
						  				out.println("<td>");
						  					out.println("<font face=\"arial\" color=817b7b><i>" + description + "</i></font>");
						  					out.println("<hr/><img src=\"http://cs.purdue.edu/homes/scdickso/in.jpg\" width=15 height=15> " + rs.getInt("links_in") + " | <img src=\"http://cs.purdue.edu/homes/scdickso/out.jpg\" width=15 height=15> " + rs.getInt("links_out") + "</br>");
						  				out.println("</td>");
						  			out.println("</tr>");
					  }
					  else
					  {
						  out.println("<tr>");
			  				out.println("<td>");
			  					out.println("<font face=\"arial\" color=817b7b>No description available.</font>");
			  					out.println("<hr/><img src=\"http://cs.purdue.edu/homes/scdickso/in.jpg\" width=15 height=15> " + rs.getInt("links_in") + " | <img src=\"http://cs.purdue.edu/homes/scdickso/out.jpg\" width=15 height=15> " + rs.getInt("links_out") + "</br>");
			  				out.println("</td>");
			  			  out.println("</tr>");
					  }
					  
						  out.println("</table>");
						  out.println("</td>");
						  out.println("</tr>");
					  
					  
					  out.println("</table></br>");
					  }
					  catch(Exception ex)
					  {
						  ex.printStackTrace();
					  }
				  }
				  
				  stat.executeUpdate("CREATE TABLE IF NOT EXISTS queries (qid INT NOT NULL AUTO_INCREMENT, keywords VARCHAR(200), q_time DOUBLE, PRIMARY KEY (qid))");
				  stat.executeUpdate("INSERT INTO queries (qid, keywords, q_time) VALUES (0,'" + searchParams + "', " + query_time + ")");
				  
				  rs.close();
				  stat.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			out.println("</head><body></body></html>");
		}
		out.println("</div></body></html>");
    	
    }
    
    public void doSearch(String searchParams, PrintWriter out)
    {
    	long init_time = System.currentTimeMillis();
		int num_results = 0;
		out.println("<html><head>");
		out.println("<link rel=\"icon\" type=\"image/ico\" href=\"https://www.purdue.edu/purdue/images/favicon.ico\">");
		out.println("<link href='button.css' rel='stylesheet' type='text/css'>");
		out.println("<link href='textinput.css' rel='stylesheet' type='text/css'>");
		//out.println("<link href='table.css' rel='stylesheet' type='text/css'>");
		if(searchParams != null)
		{
			out.println("<title>Search Results: " + searchParams + "</title></head><body bgcolor=#ffffff><div align=\"left\">");
			out.println("<table><tr><td><a href=\"SearchEngine\"><img width=\"91\" height=\"50\" src=\"http://cs.purdue.edu/homes/scdickso/csearch.png\"/></a></td>");
			out.println("<form action=\"SearchResults\" method=GET>");
			out.println("<td><div class=\"myTextbox\"><input type=text size=80 name=query value=\"" + searchParams + "\" autofocus></div></td><td>  <input type=button class=\"myButton\" onClick=\"submit();\" value=\"Search\"></td>");
			out.println("</form></tr></table></hr>");
			
			try
			{
				  Statement stat = connection.createStatement();
				  String query = "SELECT DISTINCT count(urls.ID) as count FROM urls, words WHERE urls.urlid=words.urlid AND word=\"" + searchParams + "\"";
				  ResultSet rs = stat.executeQuery(query);
				  rs.next();
				  num_results = rs.getInt("count");
				  
				  if(num_results <= 0)
				  {
					  out.println("<div align=\"center\"><h1>No Results Found.</h1></div>");
					  out.println("</body></html>");
					  return;
				  }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				  Statement stat = connection.createStatement();
				  String query = "SELECT DISTINCT urls.ID, urls.url, urls.description, urls.first_img, urls.title, urls.links_out, urls.links_in FROM urls, words WHERE urls.urlid=words.urlid AND word=\"" + searchParams + "\" ORDER BY urls.urlid ASC LIMIT " + ((page_number-1) * RESULTS_PER_PAGE) + "," + RESULTS_PER_PAGE;
				  //System.out.println(query);
				  ResultSet rs = stat.executeQuery(query);
				  query_time = (System.currentTimeMillis() - init_time)/1000.0;
				  
				  //int count = rs.getInt("count");
				  //out.println("<font face=\"arial\" color=#4D4344>Search returned " + count + " results.</font><hr/>");
				  int to = ((page_number-1) * RESULTS_PER_PAGE + RESULTS_PER_PAGE);
				  if(to > num_results)
				  {
					  to = num_results;
				  }
				  out.println("<font face=\"arial\" color=#4D4344>Showing " + ((page_number-1) * RESULTS_PER_PAGE + 1) + " to " + to + " of " + num_results  + " results (" + query_time + " seconds)</font><hr/>");
				    
				  while(rs.next())
				  {
					  try
					  {
					  String title = SEUtils.processTitle(rs.getString("title"));
					  String first_img = rs.getString("first_img");
					  String description = rs.getString("description");
					  out.println("<table border=0px>");
					  if(first_img != null && !first_img.equals(""))
					  {
						  out.println("<tr>");
						  	out.println("<td bgcolor=#ffffffD>");
						  		if(first_img.contains("/images/logo.svg"))
						  		{
						  			first_img = "https://www.cs.purdue.edu/images/logo-small.png";
						  		}
						  		out.println("<a href=\"" + first_img + "\" target=\"_blank\"><img src=\"" + first_img + "\" width=100 height=100 border=0px></a>");
						  	out.println("</td>");
					  }
					  		out.println("<td>");
					  			out.println("<table border=0px>");
					  				out.println("<tr>");
					  					out.println("<td>");
					  						out.println("<font face=\"arial\" color=#4D4344><a href=\"" + rs.getString("url") + "\" target=\"_blank\">" + title + "</a></font><br/>");
					  						out.println("<font face=\"arial\" color=#00C10D>" + rs.getString("url") + "</font><br/>");
					  					out.println("</td>");
					  				out.println("</tr>");
					  if(description != null)
					  {
						  			out.println("<tr>");
						  				out.println("<td>");
						  					out.println("<font face=\"arial\" color=817b7b><i>" + description + "</i></font>");
						  					out.println("<hr/><img src=\"http://cs.purdue.edu/homes/scdickso/in.jpg\" width=15 height=15> " + rs.getInt("links_in") + " | <img src=\"http://cs.purdue.edu/homes/scdickso/out.jpg\" width=15 height=15> " + rs.getInt("links_out") + "</br>");
						  				out.println("</td>");
						  			out.println("</tr>");
					  }
					  else
					  {
						  out.println("<tr>");
			  				out.println("<td>");
			  					out.println("<font face=\"arial\" color=817b7b>No description available.</font>");
			  					out.println("<hr/><img src=\"http://cs.purdue.edu/homes/scdickso/in.jpg\" width=15 height=15> " + rs.getInt("links_in") + " | <img src=\"http://cs.purdue.edu/homes/scdickso/out.jpg\" width=15 height=15> " + rs.getInt("links_out") + "</br>");
			  				out.println("</td>");
			  			  out.println("</tr>");
					  }
					  
						  out.println("</table>");
						  out.println("</td>");
						  out.println("</tr>");
					  
					  
					  out.println("</table></br>");
					  }
					  catch(Exception ex)
					  {
						  ex.printStackTrace();
					  }
				  }
				  
				  if(!pageSet)
				  {
					  stat.executeUpdate("CREATE TABLE IF NOT EXISTS queries (qid INT NOT NULL AUTO_INCREMENT, keywords VARCHAR(200), q_time DOUBLE, PRIMARY KEY (qid))");
					  stat.executeUpdate("INSERT INTO queries (qid, keywords, q_time) VALUES (0,'" + searchParams + "', " + query_time + ")");
				  }
				  
				  if(num_results > RESULTS_PER_PAGE)
				  {
					  int num_pages_required = (int) Math.ceil(num_results / RESULTS_PER_PAGE);
					  out.println("<br/><hr/><div align=\"center\">");
					  out.println("<font face=\"arial\" color=#4D4344><a href=\"SearchResults?query=" + searchParams + "&page=1\"><--  </a></font>");
					  for(int i = 1; i <= num_pages_required ; i++)
					  {
						  out.println("<font face=\"arial\" color=#4D4344><a href=\"SearchResults?query=" + searchParams + "&page=" + i + "\">" + i + "  </a></font>");
					  }
					  out.println("<font face=\"arial\" color=#4D4344><a href=\"SearchResults?query=" + searchParams + "&page=" + num_pages_required + "\">--></a></font>");
					  out.println("</div>");
				  }
				  
				  rs.close();
				  stat.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			out.println("</head><body></body></html>");
		}
		out.println("</div></body></html>");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try
		{
			pageSet = true;
			page_number = Integer.parseInt(request.getParameter("page"));
		}
		catch(Exception e)
		{
			pageSet = false;
			page_number = 1;
		}
		
		try
		{
			String searchParams = request.getParameter("query");
			
			
			if(searchParams.split(" ").length > 1)
			{
				doMultiWordSearch(searchParams, response.getWriter());
			}
			else
			{
				doSearch(searchParams, response.getWriter());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

}
