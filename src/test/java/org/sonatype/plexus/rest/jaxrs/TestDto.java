package org.sonatype.plexus.rest.jaxrs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlRootElement
@XStreamAlias( "test" )
public class TestDto
{
    private String aString;

    private Date aDate;

    private List<String> aStringList;

    private List<TestDto> children;

    public String getAString()
    {
        return aString;
    }

    public void setAString( String string )
    {
        aString = string;
    }

    public Date getADate()
    {
        return aDate;
    }

    public void setADate( Date date )
    {
        aDate = date;
    }

    public List<String> getAStringList()
    {
        if ( aStringList == null )
        {
            aStringList = new ArrayList<String>();
        }

        return aStringList;
    }

    public void setAStringList( List<String> stringList )
    {
        aStringList = stringList;
    }

    public List<TestDto> getChildren()
    {
        if ( children == null )
        {
            children = new ArrayList<TestDto>();
        }

        return children;
    }

    public void setChildren( List<TestDto> children )
    {
        this.children = children;
    }
}
