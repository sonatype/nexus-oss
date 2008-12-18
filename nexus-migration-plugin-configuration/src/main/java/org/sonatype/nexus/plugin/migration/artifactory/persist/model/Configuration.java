package org.sonatype.nexus.plugin.migration.artifactory.persist.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( "mappingConfiguration" )
public class Configuration
{

    @XStreamImplicit
    private List<CMapping> urlsMapping;

    public List<CMapping> getUrlsMapping()
    {
        if ( urlsMapping == null )
        {
            urlsMapping = new ArrayList<CMapping>();
        }
        return urlsMapping;
    }

    public void setUrlsMapping( List<CMapping> maps )
    {
        this.urlsMapping = maps;
    }

    public void addUrlMapping( CMapping map )
    {
        getUrlsMapping().add( map );
    }
}
