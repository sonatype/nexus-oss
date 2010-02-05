package org.sonatype.nexus.plugins.rrb;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "rrbresponse" )
public class MavenRepositoryReaderResponse
    implements Serializable
{
    private static final long serialVersionUID = 3969716837308011475L;

    List<RepositoryDirectory> data;

    public MavenRepositoryReaderResponse()
    {
        super();
    }

    @XmlElementWrapper( name = "data" )
    @XmlElement( name = "node" )
    public List<RepositoryDirectory> getData()
    {
        return data;
    }

    public void setData( List<RepositoryDirectory> data )
    {
        this.data = data;
    }

    @Override
    public String toString()
    {
        return "MavenRepositoryReaderResponse [data=" + data + "]";
    }

}