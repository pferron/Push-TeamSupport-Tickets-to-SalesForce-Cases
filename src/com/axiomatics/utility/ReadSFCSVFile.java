package com.axiomatics.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.axiomatics.data.SalesForceContact;
import com.axiomatics.data.SalesForceCustomer;

public class ReadSFCSVFile {
	
		public List<SalesForceContact> ReadSFContact(String csvfile)
		{		
	        String line 		= "";
	        String cvsSplitBy 	= ",";
	        int[] indexColumn 	= new int[7];
	
	        List<SalesForceContact> contactList = new ArrayList<>();
	
	        try (BufferedReader br = new BufferedReader(new FileReader(csvfile))) 
	        {
	
	        	line = br.readLine();
	        	
	        	String[] field = line.split(cvsSplitBy);
	        	indexColumn = columnName(field, field.length);
	        	
	
	        	
	            while ((line = br.readLine()) != null) 
	            {
	
	            	if (line.isEmpty()) break;
	            	
	                // use comma as separator
	            	SalesForceContact contact = new SalesForceContact();
	                field = line.split(cvsSplitBy);
	                
	                // replace HTML ampersand &amp; by &
//	                if (field[indexColumn[0]].contains("&amp;"))
//	                	field[indexColumn[0]] = field[indexColumn[0]].replaceAll("&amp;", "&");
	                
	                contact.setEmail(field[indexColumn[6]]);
	                contact.setContactID(field[indexColumn[7]]);
	                	                                
	                contactList.add(contact);
	
	            }            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			return contactList;
	
		}
		
		public List<SalesForceCustomer> ReadSFCustomer(String csvfile)
		{		
	        String line 		= "";
	        String cvsSplitBy 	= ",";
	        int[] indexColumn 	= new int[7];

	        List<SalesForceCustomer> customerList = new ArrayList<>();

	        try (BufferedReader br = new BufferedReader(new FileReader(csvfile))) 
	        {

	        	line = br.readLine();
	        	
	        	String[] field = line.split(cvsSplitBy);
	        	indexColumn = columnName(field, field.length);
	        	

	        	
	            while ((line = br.readLine()) != null) 
	            {

	            	if (line.isEmpty()) break;
	            	
	                // use comma as separator
	            	SalesForceCustomer customer = new SalesForceCustomer();
	                field = line.split(cvsSplitBy);
	                
	                // replace HTML ampersand &amp; by &
	                if (field[indexColumn[0]].contains("&amp;"))
	                	field[indexColumn[0]] = field[indexColumn[0]].replaceAll("&amp;", "&");
	                
	                customer.setAccountName(field[indexColumn[0]]);
	                customer.setType(field[indexColumn[1]]);
	                customer.setAccountOwner(field[indexColumn[2]]);
	                customer.setSupportAgreement(field[indexColumn[3]]);
	                customer.setSupportAgreementExpSF(field[indexColumn[4]]);
	                customer.setAccountID(field[indexColumn[6]]);
	                	                                
	                customerList.add(customer);

	            }            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			return customerList;

		}

		private static int[] columnName(String[] field, int columnMunber)
		{
			int[] indexColumn = new int[columnMunber];
			
			for (int i=0; i < columnMunber; i++)
			{
				String columnName = field[i].replaceAll("\"", "");
				
			    switch (columnName) {
	            // Active Customer List CSV File
	            case "Account Name":
	            	indexColumn[0] = i;
	                break;

		        // Active Customer List CSV File
	            case "Type":
	            	indexColumn[1] = i;
	                break;
	                
		        // Active Customer List CSV File         
	            case "Account Owner": 
	            	indexColumn[2] = i;
	                break;
	                
		        // Active Customer List CSV File
	            case "Support Agreement": 
	            	indexColumn[3] = i;
	                break;
	               
		        // Active Customer List CSV File
	            case "Support Agreement Expiration": 
	            	indexColumn[4] = i;
	                break;
	                
		        // Active Customer List CSV File
	            case "Account ID": 
	            	indexColumn[6] = i;
	                break;
	            
	            // Active Contact List CSV File
	            case "Email": 
	            	indexColumn[6] = i;
	                break;
	            
	            // Active Contact List CSV File
	            case "Contact ID": 
	            	indexColumn[7] = i;
	                break;
	                
	                
			    }
		
			
			}
			
			return indexColumn;
		}
		
}
