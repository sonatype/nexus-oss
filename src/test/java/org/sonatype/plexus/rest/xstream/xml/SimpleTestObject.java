package org.sonatype.plexus.rest.xstream.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "simple" )
public class SimpleTestObject
{

    private String data;

    public String getData()
    {
        return data;
    }

    public void setData( String data )
    {
        this.data = data;
    }

}
