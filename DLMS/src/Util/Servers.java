package Util;

public enum Servers {
CON ("Concordia"),
MCG ("McGill"),
MON ("Montreal")
;
	
	private final String serverName;
	Servers(String serverName)
	{
		this.serverName=serverName;
	}
	
	  public String getserverName() {
	        return this.serverName;
	    }
}
