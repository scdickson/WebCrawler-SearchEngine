

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class SubdomainGraph
 */
@WebServlet("/SubdomainGraph")
public class SubdomainGraph extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource ds;
	private Connection connection;
	HashMap<String, Rectangle> subdomainRect = new HashMap<String,Rectangle>();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubdomainGraph() {
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
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		try 
		{
			Random generator = new Random(System.currentTimeMillis());
			HashMap<String, Integer> mapping = new HashMap<String, Integer>();
			Statement statement = connection.createStatement();
			int total = 0;
			ResultSet rs = statement.executeQuery("SELECT urlid, url FROM urls");
			while(rs.next())
			{
				try
				{
					String sd = getBaseURL(rs.getString("url"));
					Integer i = mapping.get(sd);
					total++;
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
			
			OutputStream os = response.getOutputStream();
			BufferedImage bufferedImage = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);  
	        Graphics g = bufferedImage.getGraphics();
	        g.setFont(new Font("",0,15));
	        FontMetrics fm = g.getFontMetrics();
	        int max_size = 500;
	        
	        int x = 0, y = 0;
			for(String s : mapping.keySet())
			{
				 double size = mapping.get(s) / (double) total;
				 g.setColor(Color.WHITE);
				 int x_coord = generator.nextInt(2000-max_size);
				 int y_coord = generator.nextInt(2000-max_size);
				 while(intersects(x_coord, y_coord, fm.stringWidth( s ), fm.getHeight()))
				 {
					 x_coord = generator.nextInt(2000-max_size);
					 y_coord = generator.nextInt(2000-max_size);
				 }
				 
				 g.drawString(s, x_coord,y_coord+(fm.getHeight()/2));
				 g.drawRect(x_coord-5,y_coord-5,fm.stringWidth( s )+5,fm.getHeight());
				 subdomainRect.put(s,new Rectangle(x_coord-5,y_coord-5,fm.stringWidth( s )+5,fm.getHeight()));
				 
				 //System.out.println(s + "\t" + mapping.get(s));
			}
			
			for(String from : subdomainRect.keySet())
			{
				for(String to : subdomainRect.keySet())
				{
					if(!from.equals(to))
					{
						rs = statement.executeQuery("SELECT count(sd_to) FROM subdomain_connections WHERE sd_from='" + from + "' AND sd_to='" + to + "'");
						if(rs.next())
						{
							int count = rs.getInt("count(sd_to)");
							if(count == 0)
							{
								continue;
							}
							
							if(count < 10)
							{
								g.setColor(Color.RED);
							}
							else if(count > 10 && count <= 50)
							{
								g.setColor(Color.YELLOW);
							}
							else if(count > 50 && count <= 100)
							{
								g.setColor(Color.MAGENTA);
							}
							else if(count > 100 && count <= 500)
							{
								g.setColor(Color.GREEN);
							}
							else if(count > 500 && count <= 1000)
							{
								g.setColor(Color.BLUE);
							}
							else if(count > 1000 && count <= 2500)
							{
								g.setColor(Color.CYAN);
							}
							else if(count > 2500)
							{
								g.setColor(Color.WHITE);
							}
							
							Rectangle fromRect = subdomainRect.get(from);
							Rectangle toRect = subdomainRect.get(to);
							
							if(fromRect.y > toRect.y)
							{
								  drawArrow(g, fromRect.x+(fromRect.getSize().width/2), fromRect.y, toRect.x+(toRect.getSize().width/2), toRect.y+(toRect.getSize().height));
								//g.drawLine(fromRect.x+(fromRect.getSize().width/2), fromRect.y, toRect.x+(toRect.getSize().width/2), toRect.y+(toRect.getSize().height));
								//g.fillOval(fromRect.x+(fromRect.getSize().width/2), fromRect.y-10, 10,10);
							}
							else
							{
								drawArrow(g, fromRect.x+(fromRect.getSize().width/2), fromRect.y+(fromRect.getSize().height), toRect.x+(toRect.getSize().width/2), toRect.y);
								//g.drawLine(fromRect.x+(fromRect.getSize().width/2), fromRect.y+(fromRect.getSize().height), toRect.x+(toRect.getSize().width/2), toRect.y);
								//g.fillOval(fromRect.x+(fromRect.getSize().width/2), fromRect.y+(fromRect.getSize().height)+10, 10,10);
							}
						}
					}
				}
			}
			
			g.setColor(Color.RED);
			g.fillRect(1800,1800,30,20);
			g.drawString("< 10", 1800+40, 1800+12);
			
			g.setColor(Color.YELLOW);
			g.fillRect(1800,1820,30,20);
			g.drawString("Between 10 and 50", 1800+40, 1820+12);
			
			g.setColor(Color.MAGENTA);
			g.fillRect(1800,1840,30,20);
			g.drawString("Between 50 and 100", 1800+40, 1840+12);
			
			g.setColor(Color.GREEN);
			g.fillRect(1800,1860,30,20);
			g.drawString("Between 100 and 500", 1800+40, 1860+12);
			
			g.setColor(Color.BLUE);
			g.fillRect(1800,1880,30,20);
			g.drawString("Between 500 and 1000", 1800+40, 1880+12);
			
			g.setColor(Color.CYAN);
			g.fillRect(1800,1900,30,20);
			g.drawString("Between 1000 and 2500", 1800+40, 1900+12);
			
			g.setColor(Color.WHITE);
			g.fillRect(1800,1920,30,20);
			g.drawString("> 2500", 1800+40, 1920+12);
			
			g.drawRect(1780, 1780, 1940, 1940);
			g.dispose();
			response.setContentType("image/jpeg");  
	        ImageIO.write(bufferedImage, "jpg", os); 
			
		} catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[] {len, len-7, len-7, len},
                      new int[] {0, -7, 7, 0}, 4);
    }
	
	public boolean intersects(int x_coord, int y_coord, int width, int height)
	{
		for(Rectangle rect : subdomainRect.values())
		{
			Rectangle check = new Rectangle(x_coord,y_coord,width,height);
			if(check.intersects(rect))
			{
				return true;
			}
		}
		
		return false;
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
