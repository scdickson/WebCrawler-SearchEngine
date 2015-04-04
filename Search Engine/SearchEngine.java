

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.*;
/**
 * Servlet implementation class SearchEngine
 */
@WebServlet("/SearchEngine")
public class SearchEngine extends HttpServlet 
{
	private DataSource ds;
	private Connection connection;
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchEngine() 
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>CSearch Engine</title>");
		out.println("<link rel=\"icon\" type=\"image/ico\" href=\"https://www.purdue.edu/purdue/images/favicon.ico\">");
		out.println("<link href='button.css' rel='stylesheet' type='text/css'>");
		out.println("<link href='textinput.css' rel='stylesheet' type='text/css'>");
		out.println("</head><body bgcolor=#ffffff><div align=\"right\"><a href=\"SearchStats\"><img src=\"http://cdn2.business2community.com/wp-content/uploads/2013/01/Increase.gif\" width=30 height=30/></a></div><br/>");
		out.println("<div align=\"center\"><img width=\"365\" height=\"201\" src=\"http://cs.purdue.edu/homes/scdickso/csearch.png\"/><br/>");
		try
		{
			  Statement stat = connection.createStatement();
			  String query = "SELECT count(*) as count FROM urls";
			  ResultSet rs = stat.executeQuery(query);
			  int numUrls = 0;
			  if(rs.next())
			  {
				  numUrls = rs.getInt("count");
			  }
			  rs.close();
			  stat.close();
			  out.println("<font face=\"arial\" color=#4D4344>Searching " + numUrls + " pages.</font></br><br/>");
		}
		catch(Exception e)
		{
			//out.println(e.getMessage());
		}
		out.println("<form action=\"SearchResults\" method=GET>");
		out.println("<div class=\"myTextbox\"><input type=text size=80 name=query autofocus></div><br/><input type=button class=\"myButton\" onClick=\"submit();\" value=\"Search\">");
		out.println("</form></div></body></html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
