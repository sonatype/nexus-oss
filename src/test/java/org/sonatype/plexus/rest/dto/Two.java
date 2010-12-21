package org.sonatype.plexus.rest.dto;

public class Two
    extends One
{
    private String twoValue = "two data";

    public String getTwoValue()
    {
        return twoValue;
    }

    public void setTwoValue( String twoValue )
    {
        this.twoValue = twoValue;
    }
    
    
}
