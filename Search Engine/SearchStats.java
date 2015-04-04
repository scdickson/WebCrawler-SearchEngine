

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.sun.image.codec.jpeg.JPEGCodec;

/**
 * Servlet implementation class SearchStats
 */
@WebServlet("/SearchStats")
public class SearchStats extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource ds;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchStats() {
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
		try
		{
			PrintWriter out = response.getWriter();
			out.println("<html><head><title>Search Engine Stats</title><link rel=\"stylesheet\" href=\"http://yui.yahooapis.com/pure/0.6.0/pure-min.css\">");
			out.println("<link rel=\"icon\" type=\"image/ico\" href=\"https://www.purdue.edu/purdue/images/favicon.ico\">");
			out.println("</head><body>");		
			out.println("<a href=\"SearchEngine\"><img width=\"50\" height=\"50\" src=\"http://cdns2.freepik.com/image/th/318-41700.png\"/></a><img width=\"91\" height=\"50\" src=\"http://cs.purdue.edu/homes/scdickso/csearch.png\"/>");
			out.println("<hr/><br/><br/>");
        	Statement stat = connection.createStatement();
        	out.println("<table border=0px>");
        	
        	out.println("<tr>");
        	out.println("<td align=\"center\" cellspacing=\"10\"><h2><u>Most Searched Keywords:</u></h2></br>");
        	//Most searched words:
        	ResultSet rs = stat.executeQuery("SELECT count(keywords) as count, keywords FROM queries GROUP BY keywords ORDER BY count(*) DESC LIMIT 10");
        	out.println("<table class=\"pure-table pure-table-horizontal\"><thead><tr><th>#</th><th>Keyword</th><th>Searches</th></tr></thead><tbody>");
        	int i = 1;
        	while(rs.next())
        	{
        		out.println("<tr><td>" + i++ + "</td><td>" + rs.getString("keywords") + "</td><td>" + rs.getInt("count") + "</td></tr>");
        	}
        	while(i <= 10)
        	{
        		out.println("<tr><td>" + i++ + "</td><td>" + "--" + "</td><td>" + "--" + "</td></tr>");
        	}
        	out.println("</tbody></table>");
        	out.println("</td>");
        	
        	//Search Times
        	out.println("<td align=\"center\" valign=\"top\" cellspacing=\"10\"><h2><u>Search Times:</u></h2></br>");
        	rs = stat.executeQuery("SELECT min(q_time), avg(q_time), max(q_time) FROM queries");
        	out.println("<table class=\"pure-table pure-table-horizontal\"><thead><tr><th></th><th>Time (sec)</th></tr></thead><tbody>");
        	DecimalFormat df = new DecimalFormat("##0.00");
        	
        	if(rs.next())
        	{
        		out.println("<tr>");
        		out.println("<td>Min:</td>");
        		out.println("<td>" + df.format(rs.getDouble("min(q_time)")) + "</td>");
        		out.println("</tr>");
        		
        		out.println("<tr>");
        		out.println("<td>Avg:</td>");
        		out.println("<td>" + df.format(rs.getDouble("avg(q_time)")) + "</td>");
        		out.println("</tr>");
        		
        		out.println("<tr>");
        		out.println("<td>Max:</td>");
        		out.println("<td>" + df.format(rs.getDouble("max(q_time)")) + "</td>");
        		out.println("</tr>");
        	}
        	out.println("</tbody></table>");
        	out.println("</td>");
        	
        	//Links
        	out.println("<td align=\"center\" valign=\"top\" cellspacing=\"10\"><h2>&nbsp&nbsp<u>Page References:</u>&nbsp&nbsp</h2></br>");
        	rs = stat.executeQuery("SELECT min(links_out), min(links_in), avg(links_out), avg(links_in), max(links_out), max(links_in) FROM urls");
        	out.println("<table class=\"pure-table pure-table-horizontal\"><thead><tr><th></th><th><img src=\"http://cs.purdue.edu/homes/scdickso/in.jpg\" width=15 height=15></th><th><img src=\"http://cs.purdue.edu/homes/scdickso/out.jpg\" width=15 height=15></th></tr></thead><tbody>");
        	if(rs.next())
        	{
        		out.println("<tr>");
        		out.println("<td>Min:</td>");
        		out.println("<td>" + rs.getInt("min(links_in)") + "</td>");
        		out.println("<td>" + rs.getInt("min(links_out)") + "</td>");
        		out.println("</tr>");
        		
        		out.println("<tr>");
        		out.println("<td>Avg:</td>");
        		out.println("<td>" + rs.getInt("avg(links_in)") + "</td>");
        		out.println("<td>" + rs.getInt("avg(links_out)") + "</td>");
        		out.println("</tr>");
        		
        		out.println("<tr>");
        		out.println("<td>Max:</td>");
        		out.println("<td>" + rs.getInt("max(links_in)") + "</td>");
        		out.println("<td>" + rs.getInt("max(links_out)") + "</td>");
        		out.println("</tr>");
        	}
        	out.println("</tbody></table>");
        	out.println("</td>");
        	
        	out.println("<td align=\"center\" valign=\"top\" cellspacing=\"10\"><h2><u>Subdomain Graph:</u></h2></br>");
        	out.println("<a href=\"SubdomainGraph\"><button class=\"pure-button pure-button-primary\">Generate Graph</button></a>");
        	out.println("</td>");
        	
        	//Subdomains
            //out.println("<td><img src=\"SubdomainGraph\"/></td>");
            
        	out.println("</tr>");
        	out.println("</table></body></html>");
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
