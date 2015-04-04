public class Constants 
{
	public static final String jdbc_url = "jdbc:mysql://localhost:9999/CRAWLER";
	public static final String jdbc_username = "root";
	public static final String jdbc_password = "kpcofgs";
	public static final int crawler_maxurls = 100;
	
	public static final int MAX_CONCURRENT_THREADS = 10;
	public static final int DATABASE_RETRY_TIMEOUT = 1000;
	public static final int CONNECT_TIMEOUT = 10000;
	public static final int MAX_DESC_CHARS = 100;
	public static final int MAX_TITLE_CHARS = 100;
	public static final String INVALID_PUNCTUATION = "!@#$%^&*()-_+={}[];:,.<>?/|\\~`'\"";
}
