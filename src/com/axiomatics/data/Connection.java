package com.axiomatics.data;

public class Connection {
	
	private String jsonURL	= "";
	private String userName	= "";
	private String password	= "";
	
	public String getJsonURL()
	{
	    return this.jsonURL;
	}
	public void setJsonURL(String jsonURL)
	{
	     this.jsonURL = jsonURL;
	}
	
	public String getUserName()
	{
	    return this.userName;
	}
	public void setUserName(String userName)
	{
	     this.userName = userName;
	}
	
	public String getPassword()
	{
	    return this.password;
	}
	public void setPassword(String password)
	{
	     this.password = password;
	}
	

}
