import java.io.*;
import java.sql.*;
import java.util.*;

public class Database
{
  Connection connection;
  PreparedStatement insert_word_statement, insert_url_statement;
  boolean isConnected = false;

  public void openConnection() throws SQLException, IOException, InterruptedException
  {
	  while(!isConnected)
		  {
			  try
			  {
			    connection = DriverManager.getConnection(Constants.jdbc_url, Constants.jdbc_username, Constants.jdbc_password);
			    String insert_word_query = "INSERT INTO WORDS (word, urlid) VALUES (?, ?)";
			    String insert_url_query = "INSERT INTO URLS (url, urlid)  VALUES (?, ?)";
				insert_word_statement = connection.prepareStatement(insert_word_query);
				insert_url_statement = connection.prepareStatement(insert_url_query);
				isConnected = true;
			  }
			  catch(Exception e)
			  {
				  Thread.sleep(Constants.DATABASE_RETRY_TIMEOUT);
			  }
		  }
  }
  
  public void closeConnection() throws SQLException, IOException
  {
    if(connection != null)
    {
    	insert_word_statement.close();
      connection.close();
    }
  }
  
  public void createIndexes() throws SQLException, IOException
  {
	  Statement stat = connection.createStatement();
	  stat.executeUpdate("CREATE INDEX sdIndex ON subdomain_connections (sd_from, sd_to) USING HASH");
	  stat.executeUpdate("CREATE INDEX wordIndex ON words (word) USING HASH");
	  stat.executeUpdate("CREATE FULLTEXT INDEX ftWordIndex ON words (word)");
	  stat.close();
  }
  
  public void createDB() throws SQLException, IOException
  {
    Statement stat = connection.createStatement();
    stat.executeUpdate("DROP TABLE IF EXISTS URLS");
    stat.executeUpdate("DROP TABLE IF EXISTS WORDS");
    stat.executeUpdate("DROP TABLE IF EXISTS subdomain_connections");
    stat.executeUpdate("CREATE TABLE URLS (ID int NOT NULL AUTO_INCREMENT, urlid INT NOT NULL, url VARCHAR(250), title VARCHAR(200), description VARCHAR(110), first_img VARCHAR(200), links_in INT DEFAULT 0, links_out INT DEFAULT 0, PRIMARY KEY(ID), UNIQUE(url))");
    stat.executeUpdate("CREATE TABLE WORDS (urlid INT, word VARCHAR(250))");
    stat.executeUpdate("CREATE TABLE subdomain_connections (cid INT NOT NULL AUTO_INCREMENT, sd_from VARCHAR(100), sd_to VARCHAR(100), count INT, PRIMARY KEY (cid)");
	stat.close();
  }
  
  public void removeURL(int urlIndex) throws SQLException, IOException
  {
	  Statement stat = connection.createStatement();
	  String query = "DELETE FROM urls WHERE urlid=" + urlIndex;
	  stat.executeUpdate(query);
	  stat.close();
  }
  
  public void updateSubdomainConnection(String from, String to) throws SQLException, IOException
  {
	  Statement stat = connection.createStatement();
	  String query = "INSERT INTO subdomain_connections (sd_from,sd_to) VALUES ('" + from + "', '" + to + "')";
	  stat.executeUpdate(query);
	  stat.close();
  }
  
  public void insertURLs(ArrayList<String> urls) throws SQLException, IOException
  {
	  StringBuilder sb = null;
	  try
	  {
		  sb = new StringBuilder("INSERT IGNORE INTO URLS (url, urlid)  VALUES ");
		  for(int i = 0; i < urls.size(); i++)
		  {
			  sb.append(urls.get(i));
			  if(i < urls.size()-1)
			  {
				  sb.append(", ");
			  }
		  }
		  Statement stat = connection.createStatement();
		  stat.executeUpdate(sb.toString());
		  stat.close();
	  }
	  catch(Exception e)
	  {
		  System.err.println(sb.toString());
		  e.printStackTrace();
	  }
  }
  
  public void insertURL(int urlid, String url) throws SQLException, IOException
  {
	  if(url != null)
	  {
		  insert_url_statement.setString(1, url);
		  insert_url_statement.setInt(2, urlid);
		  insert_url_statement.executeUpdate();
	  }
  }
  
  public void updateURL(int urlID, String description, String title, String img_path, int links_out) throws SQLException, IOException
  {
	  String query = null;
	  try
	  {
	    Statement stat = connection.createStatement();
	    query = "UPDATE URLS SET description='" + description + "', title='" + title + "', first_img='" + img_path + "', links_out=" + links_out + " WHERE URLID=" + urlID;
	    stat.executeUpdate(query);
		stat.close();
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  System.err.println(query);
	  }
  }
  
  public String getURL(int urlID) throws SQLException, IOException
  {
    Statement stat = connection.createStatement();
    String query = "SELECT * FROM URLS WHERE URLID=" + urlID;
    ResultSet result = stat.executeQuery(query);
    String data = null;
    if(result.next())
    {
    	data = result.getString("url");
    }
    
    result.close();
	stat.close();
    return data;
  }
  
  public void updateUrlIncomingLink(String url) throws SQLException, IOException
  {
	  Statement stat = connection.createStatement();
	  String query = "UPDATE urls SET links_in = links_in + 1 WHERE url='" + url + "'";
	  stat.executeUpdate(query);
	  stat.close();
  }
  
  public boolean urlExists(String url) throws SQLException, IOException
  {
    Statement stat = connection.createStatement();
    String query = "SELECT * FROM URLS WHERE url='" + url + "'";
    ResultSet result = stat.executeQuery(query);
    
    if(result.next())
    {
    	result.close();
    	stat.close();
      return true;
    }
    
    result.close();
	stat.close();
    return false;
  }
  
  public boolean wordExists(String word, int urlid) throws SQLException, IOException
  {
    Statement stat = connection.createStatement();
    String query = "SELECT * FROM WORDS WHERE word='" + word + "' AND urlid=" + urlid;
    //System.err.println(word);
    ResultSet result = stat.executeQuery(query);
    
    if(result.next())
    {
    	result.close();
    	stat.close();
      return true;
    }
    
    result.close();
	stat.close();
    return false;
  }

  public void insertWords(ArrayList<String> words) throws SQLException, IOException
  {
	  try
	  {
		  StringBuilder sb = new StringBuilder("INSERT INTO WORDS (word, urlid) VALUES ");
		  for(int i = 0; i < words.size(); i++)
		  {
			  sb.append(words.get(i));
			  if(i < words.size()-1)
			  {
				  sb.append(", ");
			  }
		  }
		  Statement stat = connection.createStatement();
		  stat.executeUpdate(sb.toString());
		  stat.close();
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
	  }
  }
  
  public void insertWord(int urlid, String word) throws SQLException, IOException
  {
	  try
	  {
	  insert_word_statement.setString(1, word);
	  insert_word_statement.setInt(2, urlid);
	  insert_word_statement.executeUpdate();
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  System.err.println("\t-->" + word);
	  }
  }
}