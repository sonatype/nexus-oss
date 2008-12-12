package org.sonatype.nexus.plugin.migration.artifactory.config;

import static org.sonatype.nexus.plugin.migration.artifactory.util.DomUtil.getValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ArtifactoryVirtualRepository
{

    private Xpp3Dom dom;

    public ArtifactoryVirtualRepository( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getKey()
    {
        return getValue( dom, "key" );
    }

    public List<String> getRepositories()
    {
        Xpp3Dom repositoriesDom = dom.getChild( "repositories" );
        if ( repositoriesDom == null )
        {
            return Collections.emptyList();
        }

        List<String> repos = new ArrayList<String>();
        for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "repositoryRef" ) )
        {
            repos.add( repoDom.getValue() );
        }
        return Collections.unmodifiableList( repos );
    }

}
