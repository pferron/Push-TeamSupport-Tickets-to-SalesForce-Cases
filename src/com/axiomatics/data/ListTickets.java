package com.axiomatics.data;

import java.util.List;

public class ListTickets {
	
	private List<Ticket> tickets;
	private int nbTicketsNotRecorded;
	
	public List<Ticket> getAllTicket()
	{
	    return this.tickets;
	}
	public void setAllTicket(List<Ticket> tickets)
	{
	     this.tickets = tickets;
	}	
	
	public int getNbTicketsNotRecorded()
	{
	    return this.nbTicketsNotRecorded;
	}
	public void setNbTicketsNotRecorded(int nbTicketsNotRecorded)
	{
	     this.nbTicketsNotRecorded = nbTicketsNotRecorded;
	}	

}
