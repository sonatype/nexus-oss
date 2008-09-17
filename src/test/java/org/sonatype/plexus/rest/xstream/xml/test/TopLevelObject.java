package org.sonatype.plexus.rest.xstream.xml.test;


public class TopLevelObject
{

    private BaseDataObject data;
    
    private String id;

    public BaseDataObject getData()
    {
        return data;
    }

    public void setData( BaseDataObject data )
    {
        this.data = data;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }
    
    
    
}
