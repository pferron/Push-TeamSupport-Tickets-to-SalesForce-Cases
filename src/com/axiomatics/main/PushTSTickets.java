package com.axiomatics.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.axiomatics.data.Connection;
import com.axiomatics.data.ListTickets;
import com.axiomatics.data.SalesForceContact;
import com.axiomatics.data.SalesForceCustomer;
import com.axiomatics.data.Ticket;
import com.axiomatics.utility.ReadSFCSVFile;
import com.axiomatics.utility.WriteSFCSVFile;
import com.google.api.services.gmail.Gmail;

public class PushTSTickets {
	
	private static final String URL_TICKETS								= "https://app.teamsupport.com/api/json/";
	private static final String	SENT_CSV_FILE_FOR_SALESFORCE_CASE		= "./sentFiles/SalesForce cases";
	private static final String SF_CUSTOMERS_CSV_FILE 					= "./csvFiles/Active Customer List.csv";
	private static final String SF_CONTACTS_CSV_FILE 					= "./csvFiles/Active Contact List.csv";
	private static final String TICKETS_LIST_LOG_FILE 					= "./logs/TicketsList";
	
	public static void main(String[] args) throws Exception {
		
		ListTickets 				allElligibleTickets 	= new ListTickets();
		List<SalesForceCustomer> 	salesForceCustomers 	= new ArrayList<>();
		List<SalesForceContact> 	salesForceContacts 		= new ArrayList<>();
		
		PushTSTickets 		http 					= new PushTSTickets();
		Connection 			connParameters 			= new Connection();
		
		/************************************** delete current files ***********************************************************************/
		Date now = new Date();
        DateFormat dfLogFile = new SimpleDateFormat ("yyyyMMdd");
        String extLogFile = dfLogFile.format(now);
        
        String logFile 		= TICKETS_LIST_LOG_FILE + "_" + extLogFile + ".log";
        String sentSFFile	= SENT_CSV_FILE_FOR_SALESFORCE_CASE + "_" + extLogFile + ".csv";
        
        fileDelete(logFile);
        fileDelete(sentSFFile);
        
        /**************************************** create 2 SalesForce CSV files from received email *******************************************/
		String 	pathCSVFile_ActiveCustomers	= "csvFiles/Active Customer List.csv";
		String 	pathCSVFile_ActiveContacts	= "csvFiles/Active Contact List.csv";
		String 	pathCredentialFile 			= "client_secret.json";
		Gmail 	service						= null;
		String 	user 						= "me"; 
		String	leftBondary_ActiveCustomers	= "Account Name";
		String	leftBondary_ActiveContacts	= "Salutation";
        String	rightBondary				= "Grand Totals";
		
        
        // SalesForce Active Customer List report
        service 				= WriteSFCSVFile.getGmailService(pathCredentialFile);            
        String 	messageId 		= WriteSFCSVFile.listMessagesMatchingQuery(service, user, "subject: Report: Active Customer List");        
        String 	bodyMessage 	= WriteSFCSVFile.getBodyMessage(service, user, messageId, leftBondary_ActiveCustomers, rightBondary);
        
        WriteSFCSVFile.createCSVFile(bodyMessage, pathCSVFile_ActiveCustomers);
        
        // SalesForce All Contacts - Support Web User = true report
        messageId 		= WriteSFCSVFile.listMessagesMatchingQuery(service, user, "subject: Report: All Contacts - Support Web User = true -Rejected Contacts ");        
        bodyMessage 	= WriteSFCSVFile.getBodyMessage(service, user, messageId, leftBondary_ActiveContacts, rightBondary);
        
        WriteSFCSVFile.createCSVFile(bodyMessage, pathCSVFile_ActiveContacts);
        
        /******************************************** Read (Active Customer List) SalesForce CSV file ***************************************************************/
		ReadSFCSVFile customerSFcsvFile 	= new ReadSFCSVFile();
		salesForceCustomers 				= customerSFcsvFile.ReadSFCustomer(SF_CUSTOMERS_CSV_FILE);
		
		/******************************************** Read (All Contacts - Support Web User = true) SalesForce CSV file ***************************************************************/
		ReadSFCSVFile contactSFcsvFile 	= new ReadSFCSVFile();
		salesForceContacts 				= contactSFcsvFile.ReadSFContact(SF_CONTACTS_CSV_FILE);
		
		 /*******************************************************************************/
		/****************************  Test Instance Connection ************************/
		/*******************************************************************************/
	    // Connection parameters
		//String jsonURL = URL_TICKETS;
		//String username = "1205149";
		//String password = "041cf131-7d7a-4285-a674-fb906a513b6e";
		
		
		/*******************************************************************************/
		/*************************  Production Instance Connection *********************/
		/*******************************************************************************/
		// Connection parameters
		String jsonURL = URL_TICKETS;
		String userName = "1102306";
		String password = "34eb26b7-d195-4d70-94b6-0fdeb9634b51";
		
		connParameters.setJsonURL(jsonURL);	
		connParameters.setUserName(userName);
		connParameters.setPassword(password);
		
		System.out.println("Get Open Ticket List - Send Http GET request");
		allElligibleTickets = http.getTicketsList(connParameters);
		http.createLogAndSFFile(allElligibleTickets, salesForceCustomers, salesForceContacts);
		
		System.out.println("Number of tickets not recorded in the csv file = " + allElligibleTickets.getNbTicketsNotRecorded());
		System.out.println("Done!!!");
		
	}
	
	public void createLogAndSFFile(ListTickets allTickets, List<SalesForceCustomer> salesForceCustomers, 
			List<SalesForceContact> salesForceContacts) throws IOException
	{
		
		List<Ticket> tickets = new ArrayList<>();
		tickets = allTickets.getAllTicket();
		
		for (int i=0; i < tickets.size(); i++) 
		{
		    String salesForceAccountID = findSFAccountID(tickets.get(i).getCustomer(), salesForceCustomers);
		    String salesForceContactID = findSFContactID(tickets.get(i).getContactEmail(), salesForceContacts);
		    
		    if (!salesForceAccountID.isEmpty())
		    {		
				CSVFileForSFTickets(tickets.get(i), salesForceAccountID, salesForceContactID, SENT_CSV_FILE_FOR_SALESFORCE_CASE);
				
				/*************** uncomment first paragraph to include SalesForce tickets in the email and comment the second paragraph *******************/
				/************************************************ First Paragraph *************************************************************************/
				/*emailSF.append(write(DISCREPANCIES_CONTACT_LISTS_LOG_FILE, "SalesForce contact being flagged \"Support Web User\" but has no portal access in TS")+ "\n");
				emailSF.append(write(DISCREPANCIES_CONTACT_LISTS_LOG_FILE, "First Name      	: " + contact.getFirstName() + "\n"));
				emailSF.append(write(DISCREPANCIES_CONTACT_LISTS_LOG_FILE, "Last Name        	: " + contact.getLastName() + "\n"));
				emailSF.append(write(DISCREPANCIES_CONTACT_LISTS_LOG_FILE, "Email				: " + contact.getEmail() + "\n"));
				emailSF.append(write(DISCREPANCIES_CONTACT_LISTS_LOG_FILE, "Account Name		: " + contact.getAccountName() + "\n"));
				emailSF.append(write(DISCREPANCIES_CONTACT_LISTS_LOG_FILE, "CR Manager		: " + contact.getCRManager() + "\n"));
				emailSF.append(write(DISCREPANCIES_CONTACT_LISTS_LOG_FILE,"\n") + "\n");*/
				
				/************************************************ Second Paragraph *************************************************************************/
				write(TICKETS_LIST_LOG_FILE, "SalesForce Account ID\t\t: " 	+ salesForceAccountID + "\n");
				write(TICKETS_LIST_LOG_FILE, "Ticket Origin\t\t\t: " 		+ tickets.get(i).getTicketOrigin() + "\n");
				write(TICKETS_LIST_LOG_FILE, "Ticket Priority\t\t\t: " 		+ tickets.get(i).getTicketPriority() + "\n");
				write(TICKETS_LIST_LOG_FILE, "Ticket Status\t\t\t: " 		+ tickets.get(i).getTicketStatus() + "\n");
				write(TICKETS_LIST_LOG_FILE, "Ticket Name\t\t\t: " 			+ tickets.get(i).getTicketName() + "\n");
				write(TICKETS_LIST_LOG_FILE, "Email contact\t\t\t: " 		+ tickets.get(i).getContactEmail() + "\n");
				write(TICKETS_LIST_LOG_FILE,"\n");
				
				/*********************************************** Console Log ******************************************************************************/
				System.out.println("SalesForce account ID\t\t: " 	+ salesForceAccountID);
				System.out.println("Ticket Origin\t\t\t: " 			+ tickets.get(i).getTicketOrigin());
				System.out.println("Ticket Priority\t\t\t: " 		+ tickets.get(i).getTicketPriority());
				System.out.println("Ticket Status\t\t\t: " 			+ tickets.get(i).getTicketStatus());
				System.out.println("Ticket Name\t\t\t: " 			+ tickets.get(i).getTicketName());
				System.out.println("Email contact\t\t\t: " 			+ tickets.get(i).getContactEmail());
				System.out.println("\n");
				
				System.out.println();
		    }
		}
         
     }
	
	public static void CSVFileForSFTickets(Ticket openticket, String salesForceAccountID, 
			String salesForceContactID, String pathCSVFile) throws IOException{
		
		FileWriter  fw 			= null;
		String		f			= null;

		Date now 					= new Date();
        DateFormat dfLogFile 		= new SimpleDateFormat ("yyyyMMdd");
        String extLogFile 			= dfLogFile.format(now);
        f 							= pathCSVFile + "_" + extLogFile + ".csv";
        File file 					= new File(f);
		
		// if file doesnt exists, then create it
		if (!file.exists()) {
	        file = new File(f);
			fw = new FileWriter(file);
			fw.append("Case ID,Account ID,Contact ID,Case Origin,Priority,Status,Subject,Case Type,Creation Date,Support Engineer,Teamsupport Ticket\n");
			fw.flush();
	        fw.close();
		}
		
         fw = new FileWriter(f,true);
        
        fw.append("new"); 							// Case ID
        fw.append(",");
        fw.append(salesForceAccountID); 			// Account ID
        fw.append(",");
        fw.append(salesForceContactID);  			// Contact ID
        fw.append(",");
//        fw.append("");  							// Case Currency
//        fw.append(",");
//        fw.append("");  							// Description
//        fw.append(",");
        fw.append(openticket.getTicketOrigin()); 	// Case Origin
        fw.append(",");
//        fw.append("");  							// Owner ID
//        fw.append(",");
        fw.append(openticket.getTicketPriority()); 	// Priority
        fw.append(",");
//        fw.append("");  							// Case Reason
//        fw.append(",");
        fw.append(openticket.getTicketStatus());   	// Status
        fw.append(",");
        fw.append(openticket.getTicketName().replaceAll(",", ""));		// Subject
        fw.append(",");
        fw.append(openticket.getTicketTypeName());	// Case Type
        fw.append(",");
        fw.append(openticket.getCreatedDate());  							// Creation Date
        fw.append(",");
        fw.append(openticket.getTicketOwner());  							// Support Engineer
        fw.append(",");
        fw.append(openticket.getTicketNumber());  							// TeamSupport Ticket
        fw.append(",");
//        fw.append(openticket.getTicketClosedDate());	// Closed Date
//        fw.append(",");
        
        fw.append("\n");

        fw.flush();
        fw.close();
    }
	
	private static String write(String f, String s) throws IOException {
		
        TimeZone tz = TimeZone.getTimeZone("CST"); // or PST, MID, etc ...
        Date now = new Date();
        DateFormat df = new SimpleDateFormat ("yyyy.MM.dd hh:mm:ss ");
        DateFormat dfLogFile = new SimpleDateFormat ("yyyyMMdd");
        df.setTimeZone(tz);
        String currentTime = df.format(now);
        String extLogFile = dfLogFile.format(now);
        f = f + "_" + extLogFile + ".log";
       
        FileWriter aWriter = new FileWriter(f, true);
        
        aWriter.write(currentTime + " " + s + "\r\n");
        aWriter.flush();
        aWriter.close();
        
        return s;
    }
	
	private static void fileDelete(String filename)
	{
		File file = new File(filename);
	    
	    if (file.exists())
	    	file.delete();
	    else
	    	System.out.println("Cannot find file " + filename);
	}
	
	
	private static String findSFAccountID(String tsContactEmail, List<SalesForceCustomer> salesForceCustomers)
	{
		String sfAccountID = "";
		
		for (int i=0; i < salesForceCustomers.size(); i++) 
		{
			if (tsContactEmail.equalsIgnoreCase(salesForceCustomers.get(i).getAccountName()))
			{
				sfAccountID = salesForceCustomers.get(i).getAccountID();
				break;
			}
		}
		
		return sfAccountID;
	}
	
	
	private static String findSFContactID(String tsAccountName, List<SalesForceContact> salesForceContacts)
	{
		String sfContactID = "";
		
		for (int i=0; i < salesForceContacts.size(); i++) 
		{
			if (tsAccountName.equalsIgnoreCase(salesForceContacts.get(i).getEmail()))
			{
				sfContactID = salesForceContacts.get(i).getContactID();
				break;
			}
		}
		
		return sfContactID;
	}
	
	
	public String getContactEmail(JSONObject object, Connection connParameters) throws Exception
	{		
		String contactEmail	= "";
		String creatorName 	= "";
		String contactName 	= "";
		String contactID 	= "";
		
		try  // if there is a unique Contact (Contact Object)
		{ 	
			JSONObject 	customers		= object.getJSONObject("Contacts");
			JSONObject 	customer 		= customers.getJSONObject("Contact");
			contactID					= customer.getString("ContactID");
		}
		catch (Exception e)  // If there is more than one Contact (Contact Array)
		{
			creatorName						= object.getString("CreatorName");
			JSONObject 	contactListObject 	= object.getJSONObject("Contacts");
			JSONArray 	contactArray 		= contactListObject.getJSONArray("Contact");
			
			for (int i=0; i<contactArray.length(); i++) 
			{
				JSONObject contactObject	= contactArray.getJSONObject(i);
				contactID					= contactObject.getString("ContactID");
				contactName					= contactObject.getString("ContactName");
				if (creatorName.equalsIgnoreCase(contactName)) break;
			}
		}   
		
		HttpGet request = buildRequest(connParameters, "contacts/"  + contactID);
		StringBuffer result = getResponse(request);
		
		JSONObject responseObject = new JSONObject(result.toString());
		JSONObject 	contactObject 	= responseObject.getJSONObject("Contact");
		
		contactEmail				= contactObject.getString("Email");
		
		return contactEmail;
	}
	
	public ListTickets getTicketsList(Connection connParameters) throws Exception {
		
		int				nbTicketsNotRecorded	= 0;
		ListTickets 	allElligibleTickets 	= new ListTickets();
		List<Ticket> 	tickets 				= new ArrayList<>();
		Date 			now 					= new Date();
		DateFormat 		df 						= new SimpleDateFormat("MM/dd/yyyy"); 
		String			ticketOwner				= null;
		String			contactEmail			= null;
	
		HttpGet request = buildRequest(connParameters, "tickets");
		StringBuffer result = getResponse(request);
		
		JSONObject responseObject = new JSONObject(result.toString());
		JSONArray resultsArray = responseObject.getJSONArray("Tickets");
		
		for (int i=0; i<resultsArray.length(); i++) 
		{
			JSONObject object = resultsArray.getJSONObject(i);		

			String closedDate				= "";
			String isClosed					= object.getString("IsClosed");
			
//			if (isClosed.equalsIgnoreCase("True"))
//			{
//				try 
//				{
//					closedDate				= object.getString("DateClosed");
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}   
//			}
			
			String ticketTypeName			= object.getString("TicketTypeName");
			String strTicketModifiedDate	= object.getString("DateModified");
			String strTicketCreatedDate		= object.getString("DateCreated");
		    String ticketStatus 			= object.getString("Status");
		
			strTicketModifiedDate			= strTicketModifiedDate.substring(0, strTicketModifiedDate.indexOf(' '));
			strTicketCreatedDate			= strTicketCreatedDate.substring(0, strTicketCreatedDate.indexOf(' '));

		    // do not load closed tickets, KB tickets 
			//if (isClosed.equalsIgnoreCase("True") || ticketTypeName.equalsIgnoreCase("KB")) continue;
		    
		    // do not load KB tickets
			if (ticketTypeName.equalsIgnoreCase("KB")) continue;
							
			try
			{
				ticketOwner = object.getString("UserName");
			}
			catch (Exception e)
			{
				ticketOwner = "";
			} 
				
			try
			{
				contactEmail = getContactEmail(object, connParameters);
			}
			catch (Exception e)
			{
				contactEmail = "";
			} 
			
			try {
				String 		ticketID 		= object.getString("ID");
				String 		ticketNumber 	= object.getString("TicketNumber");
				System.out.println("Ticket Number = " + ticketNumber);
				String 		ticketName		= object.getString("Name");
				String 		ticketOrigin	= object.getString("TicketSource");
				String 		ticketPriority	= object.getString("Severity");
				JSONObject 	customers		= object.getJSONObject("Customers");
				JSONObject 	customer 		= customers.getJSONObject("Customer");
				String 		strCustomer		= customer.getString("CustomerName");
				if (strCustomer.equalsIgnoreCase("Acme") || strCustomer.equalsIgnoreCase("_Unknown Company")) continue;
				
				Ticket ticket = new Ticket();
				ticket.setTicketID(ticketID);
				ticket.setTicketNumber(ticketNumber);
				ticket.setCustomer(strCustomer);
				ticket.setTicketOwner(ticketOwner);
				ticket.setTicketName(ticketName);
				ticket.setTicketOrigin(ticketOrigin);
				ticket.setTicketPriority(ticketPriority);
				ticket.setTicketStatus(ticketStatus);
				ticket.setIsClosed(isClosed);
				ticket.setTicketTypeName(ticketTypeName);
				ticket.setModifiedDate(strTicketModifiedDate);
				ticket.setCreatedDate(strTicketCreatedDate);
				ticket.setTicketClosedDate(closedDate);
				ticket.setContactEmail(contactEmail);
				tickets.add(ticket);	
				}
				catch (Exception e)
				{
					e.printStackTrace();
					nbTicketsNotRecorded++;
					System.out.println("Number of tickets not recorded in the csv file = " + nbTicketsNotRecorded);
				}   
		}
		
		Collections.sort(tickets);
		allElligibleTickets.setAllTicket(tickets);
		allElligibleTickets.setNbTicketsNotRecorded(nbTicketsNotRecorded);

		return allElligibleTickets;
	}
	
	public HttpGet buildRequest(Connection connParameters, String strRequest){
		
		String getUrlCustomersList = connParameters.getJsonURL() + strRequest;
		String auth = connParameters.getUserName() + ":" + connParameters.getPassword();
		String encodedAuth = Base64.encodeBase64String(auth.getBytes());
		
		HttpGet request = new HttpGet(getUrlCustomersList);
				
		request.addHeader("Authorization", "Basic " + encodedAuth);
		
		System.out.println("\nSending 'GET' request to URL : " + getUrlCustomersList);
		
		return request;
	
	}
	
	public StringBuffer getResponse(HttpGet request) throws ClientProtocolException, IOException{
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : " +
	                   response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(
	                   new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		return result;
		
	}

}
