
public class SEUtils 
{
	public static String processTitle(String title)
	{
		try
		{
			String tmp = title;
			
			if(title.isEmpty())
			{
				title = "(No Title)";
			}
			
			if(title.contains("::"))
			{
				title = title.substring(title.lastIndexOf("::")+2);
				
				if(title.isEmpty())
				{
					title = tmp;
				}
			}
			
			
		}
		catch(NullPointerException ne)
		{
			return "(No Title)";
		}
		catch(Exception e){}
		
		return title;
	}

}
