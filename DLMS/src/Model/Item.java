package Model;

import java.io.Serializable;

public class Item implements Serializable {
	
	//public String ManagerId;
	public String ItemId;
	public String ItemName;
	public int InStock;
	
	public Item()
	{
		super();
	}
	
public String getItemId()
{
	return ItemId;
}

public String getItemName()
{
	return ItemName;
}

public int getInStock()
{
	return InStock;
}

public void setItemId(String itemId)
{
	this.ItemId=itemId;
}

public void setItemName(String itemName)
{
	this.ItemName=itemName;
}
public void setInStock(int inStock)
{
	this.InStock=inStock;
}
}
