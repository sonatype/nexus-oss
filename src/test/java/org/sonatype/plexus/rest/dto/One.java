package org.sonatype.plexus.rest.dto;

public class One
{
    private String oneValue = "";
    
    private Bbb bbb = new Bbb();

    public String getOneValue()
    {
        return oneValue;
    }

    public void setOneValue( String oneValue )
    {
        this.oneValue = oneValue;
    }

    public Bbb getBbb()
    {
        return bbb;
    }

    public void setBbb( Bbb bbb )
    {
        this.bbb = bbb;
    }
    
}
