package com.yonyouup.ruanyj;

public class BillEntityBase
{
	  private int id ;
	  public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getcCode()
	{
		return cCode;
	}
	public void setcCode(String cCode)
	{
		this.cCode = cCode;
	}
	public String getcSubId()
	{
		return cSubId;
	}
	public void setcSubId(String cSubId)
	{
		this.cSubId = cSubId;
	}
	public String getcName()
	{
		return cName;
	}
	public void setcName(String cName)
	{
		this.cName = cName;
	}
	public int getiOrder()
	{
		return iOrder;
	}
	public void setiOrder(int iOrder)
	{
		this.iOrder = iOrder;
	}
	public int getIsDeleted()
	{
		return isDeleted;
	}
	public void setIsDeleted(int isDeleted)
	{
		this.isDeleted = isDeleted;
	}
	public String getcDataSourceName()
	{
		return cDataSourceName;
	}
	public void setcDataSourceName(String cDataSourceName)
	{
		this.cDataSourceName = cDataSourceName;
	}
	public String getcPrimaryKey()
	{
		return cPrimaryKey;
	}
	public void setcPrimaryKey(String cPrimaryKey)
	{
		this.cPrimaryKey = cPrimaryKey;
	}
	public int getiBillId()
	{
		return iBillId;
	}
	public void setiBillId(int iBillId)
	{
		this.iBillId = iBillId;
	}
	public int getiSystem()
	{
		return iSystem;
	}
	public void setiSystem(int iSystem)
	{
		this.iSystem = iSystem;
	}
	public int getbMain()
	{
		return bMain;
	}
	public void setbMain(int bMain)
	{
		this.bMain = bMain;
	}
	public String getcForeignKey()
	{
		return cForeignKey;
	}
	public void setcForeignKey(String cForeignKey)
	{
		this.cForeignKey = cForeignKey;
	}
	public String getcParentCode()
	{
		return cParentCode;
	}
	public void setcParentCode(String cParentCode)
	{
		this.cParentCode = cParentCode;
	}
	public String getChildrenField()
	{
		return childrenField;
	}
	public void setChildrenField(String childrenField)
	{
		this.childrenField = childrenField;
	}
	public String getcModelType()
	{
		return cModelType;
	}
	public void setcModelType(String cModelType)
	{
		this.cModelType = cModelType;
	}
	public int getbIsNull()
	{
		return bIsNull;
	}
	public void setbIsNull(int bIsNull)
	{
		this.bIsNull = bIsNull;
	}
	private String cCode;
	  private String cSubId;
	  private String cName;
	  private int iOrder;
	  private int isDeleted;
	  private String cDataSourceName;
	  private String cPrimaryKey;
	  private int iBillId;
	  private int iSystem;
	  private int bMain;
	  private String cForeignKey;
	  private String cParentCode;
	  private String childrenField;
	  private String cModelType;
	  private int bIsNull;
}
