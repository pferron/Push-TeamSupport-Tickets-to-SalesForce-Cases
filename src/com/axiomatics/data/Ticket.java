package com.axiomatics.data;

public class Ticket implements Comparable<Ticket> {
	
	private String ticketID;
	private String ticketNumber;
	private String ticketName;
	private String customer;
	private String ticketTypeName;
	private String ticketStatus;
	private String isClosed;
	private String ticketOwner;
	private String modifiedDate;
	private String createdDate;
	private String closedDate;
	private String ticketOrigin;
	private String ticketPriority;
	private String contactEmail;
	
	@Override
	public int compareTo(Ticket o) {
		// TODO Auto-generated method stub
		return this.ticketNumber.compareTo(o.ticketNumber);
	}
	
	
	public String getCreatedDate()
	{
	    return this.createdDate;
	}
	public void setCreatedDate(String createdDate)
	{
	     this.createdDate = createdDate;
	}	
	
	public String getTicketClosedDate()
	{
	    return this.closedDate;
	}
	public void setTicketClosedDate(String closedDate)
	{
	     this.closedDate = closedDate;
	}	
	
	public String getContactEmail()
	{
	    return this.contactEmail;
	}
	public void setContactEmail(String contactEmail)
	{
	     this.contactEmail = contactEmail;
	}	

	public String getTicketPriority()
	{
	    return this.ticketPriority;
	}
	public void setTicketPriority(String ticketPriority)
	{
	     this.ticketPriority = ticketPriority;
	}	
	
	public String getTicketOrigin()
	{
	    return this.ticketOrigin;
	}
	public void setTicketOrigin(String ticketOrigin)
	{
	     this.ticketOrigin = ticketOrigin;
	}	
	
	public String getCustomer()
	{
	    return this.customer;
	}
	public void setCustomer(String customer)
	{
	     this.customer = customer;
	}	
	
	
	public String getTicketName()
	{
	    return this.ticketName;
	}
	public void setTicketName(String ticketName)
	{
	     this.ticketName = ticketName;
	}	
	
	public String getModifiedDate()
	{
	    return this.modifiedDate;
	}
	public void setModifiedDate(String modifiedDate)
	{
	     this.modifiedDate = modifiedDate;
	}	
	
	public String getTicketOwner()
	{
	    return this.ticketOwner;
	}
	public void setTicketOwner(String ticketOwner)
	{
	     this.ticketOwner = ticketOwner;
	}	
	
	public String getTicketTypeName()
	{
	    return this.ticketTypeName;
	}
	public void setTicketTypeName(String ticketTypeName)
	{
	     this.ticketTypeName = ticketTypeName;
	}	
	
	public String getTicketStatus()
	{
	    return this.ticketStatus;
	}
	public void setTicketStatus(String ticketStatus)
	{
	     this.ticketStatus = ticketStatus;
	}	
	
	public String getIsClosed()
	{
	    return this.isClosed;
	}
	public void setIsClosed(String isClosed)
	{
	     this.isClosed = isClosed;
	}	
	
	public String getTicketNumber()
	{
	    return this.ticketNumber;
	}
	public void setTicketNumber(String ticketNumber)
	{
	     this.ticketNumber = ticketNumber;
	}	
	
	public String getTicketID()
	{
	    return this.ticketID;
	}
	public void setTicketID(String ticketID)
	{
	     this.ticketID = ticketID;
	}

}

