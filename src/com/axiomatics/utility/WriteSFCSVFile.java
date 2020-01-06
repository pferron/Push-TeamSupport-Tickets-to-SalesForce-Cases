package com.axiomatics.utility;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.Gmail;

import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WriteSFCSVFile {
    /** Application name. */
    private static final String APPLICATION_NAME =
        "Gmail API";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/ReadOnly");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this Gmail API.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials
     */
    private static final List<String> SCOPES =
        Arrays.asList(GmailScopes.GMAIL_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(String pathCredentialFile) throws IOException {
        // Load client secrets.
        //InputStream in = Quickstart.class.getResourceAsStream("/client_secret.json");
    	
    	InputStream in = new FileInputStream(pathCredentialFile);
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    public static Gmail getGmailService(String pathCredentialFile) throws IOException {
        Credential credential = authorize(pathCredentialFile);
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    public static String getBodyMessage(Gmail service, String userId, String messageId, String leftBondary, String rightBondary)
    	      throws IOException {
    	    Message message = service.users().messages().get(userId, messageId).execute();

    	    String messagePart = message.getPayload().getParts().get(1).getBody().getData();
    	    
    	    //String bodyMessage = StringUtils.newStringUtf8(Base64.decodeBase64(messagePart));	
    	    //String bodyMessage = new String(Base64.decodeBase64(messagePart), "UTF-8");	
    	    String bodyMessage = new String(Base64.decodeBase64(messagePart));
    	    
    	    bodyMessage = bodyMessage.replaceAll("<wbr>", ""); // remove <wbr> tag due to salesforce occurence being too long in the Salesforce report
    	    bodyMessage = bodyMessage.replaceAll("<([^<]*)>", "#");
    	    
    	    bodyMessage = bodyMessage.substring(bodyMessage.lastIndexOf(leftBondary));
    	    
  
    	    bodyMessage = bodyMessage.substring(0, bodyMessage.indexOf(rightBondary));
    	    
    	    //System.out.println(bodyMessage);
    	    
    	    return bodyMessage;
    	  }
    

    public static String listMessagesMatchingQuery(Gmail service, String userId,
    	      String query) throws IOException {
    	    ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

    	    List<Message> messages = new ArrayList<Message>();
    	    while (response.getMessages() != null) {
    	      messages.addAll(response.getMessages());
    	      if (response.getNextPageToken() != null) {
    	        String pageToken = response.getNextPageToken();
    	        response = service.users().messages().list(userId).setQ(query)
    	            .setPageToken(pageToken).execute();
    	      } else {
    	        break;
    	      }
    	    }

    	    String messageId = messages.get(0).getId();
    	    
    	    /*for (Message message : messages) {
    	      System.out.println(message.toPrettyString());
    	      getBodyMessage(service,  userId, message.getId());
    	    }*/
    	    
    	    return messageId;
    	  }

    public static void createCSVFile(String bodyMessage, String pathCSVFile)
    {
    	PrintWriter pw = null;
    	try {
    	    pw = new PrintWriter(new File(pathCSVFile));
    	} catch (FileNotFoundException e) {
    	    e.printStackTrace();
    	}
    	StringBuilder 	csvFileBody		= new StringBuilder();
    	StringBuilder 	columnNamesList = new StringBuilder();
    	String			columnName		= "";
    	String			bodyColumn		= "";
    	int				columnNumber	= 0;
    	
    	
    	/**************************************** Column Names ********************************/
    	while(bodyMessage.indexOf("##") != -1)
    	{
    		if (bodyMessage.substring(0,1).equalsIgnoreCase("\r")) break;
    		columnName = bodyMessage.substring(0, bodyMessage.indexOf("##"));
    		bodyMessage = bodyMessage.substring(columnName.length() + ("##").length(), bodyMessage.length());
    		columnNamesList.append(columnName + ",");
    		columnNumber++;
    	}
    	csvFileBody.append(columnNamesList +"\n");
    	bodyMessage = StringUtils.stripStart(bodyMessage, "\r\n");
		bodyMessage = StringUtils.stripStart(bodyMessage, "#");
    	/***************************************************************************************/
		
    	while(!bodyMessage.replaceAll("#", "").isEmpty())
    	{   		   		
    		for(int i=0; i<columnNumber; i++)
    		{
    			if (bodyMessage.replaceAll("#", "").isEmpty()) break; //check there is no more valid records
    			bodyColumn	= bodyMessage.substring(0, bodyMessage.indexOf("#"));
    			csvFileBody.append(bodyColumn+",");
    			bodyMessage = bodyMessage.substring(bodyMessage.indexOf("#"), bodyMessage.length());
    			bodyMessage = StringUtils.stripStart(bodyMessage, "#");
    		}
    		
    		csvFileBody.append('\n');
    		bodyMessage = StringUtils.stripStart(bodyMessage, "\r\n");
    		bodyMessage = StringUtils.stripStart(bodyMessage, "#");
    	}
    	
    	pw.write(csvFileBody.toString());
    	pw.close();
    	System.out.println("CSV file being created!");
    	
    }

    /*public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        Gmail service = getGmailService();

        // Print the labels in the user's account.
        String user = "me";
        
        String messageId = listMessagesMatchingQuery(service, user, "subject: Report: Rejected Contacts");
        
        String bodyMessage = getBodyMessage(service,  user, messageId);
        
        createCSVFile(bodyMessage);
        
    }*/
    

}

