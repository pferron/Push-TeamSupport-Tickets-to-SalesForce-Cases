package com.axiomatics.data;

public class SalesForceCustomer {
	
	private String accountName				= "";
	private String type						= "";
	private String accountOwner 			= "";
	private String supportAgreement			= "";
	private String supportAgreementExpSF 	= "";
	private String supportAgreementExpTS 	= "";
	private String accountID			 	= "";
	
	public String getAccountID()
	{
	    return this.accountID;
	}
	public void setAccountID(String accountID)
	{
	     this.accountID = accountID;
	}
	
	public String getSupportAgreementExpTS()
	{
	    return this.supportAgreementExpTS;
	}
	public void setSupportAgreementExpTS(String supportAgreementExpTS)
	{
	     this.supportAgreementExpTS = supportAgreementExpTS;
	}
	
	public String getSupportAgreementExpSF()
	{
	    return this.supportAgreementExpSF;
	}
	public void setSupportAgreementExpSF(String supportAgreementExpSF)
	{
	     this.supportAgreementExpSF = supportAgreementExpSF;
	}
	
	public String getSupportAgreement()
	{
	    return this.supportAgreement;
	}
	public void setSupportAgreement(String supportAgreement)
	{
	     this.supportAgreement = supportAgreement;
	}
	
	public String getAccountOwner()
	{
	    return this.accountOwner;
	}
	public void setAccountOwner(String accountOwner)
	{
	     this.accountOwner = accountOwner;
	}
	
	public String getType()
	{
	    return this.type;
	}
	public void setType(String type)
	{
	     this.type = type;
	}
	
	public String getAccountName()
	{
	    return this.accountName;
	}
	public void setAccountName(String accountName)
	{
	     this.accountName = accountName;
	}

}
