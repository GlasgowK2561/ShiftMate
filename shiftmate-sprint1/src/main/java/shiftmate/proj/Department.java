package shiftmate.proj;

public class Department 
{

    private int depID;
    private String depName;
    private String depManager;

    
public Department(int depID, String depName, String depManager)
{
    this.depID = depID;
    this.depName = depName;
    this.depManager = depManager;
}



public int getDepID()      
{
    return depID;
}

public void setDepID(int depID)     
{
    this.depID = depID;
}







public String getDepName()     
{
    return depName;
}

public void setDepName(String depName)      
{
    this.depName = depName;
}









public String getDepManager()     
{
    return depManager;
}

public void setDepManager(String depManager)       
{
    this.depManager = depManager;
}




}