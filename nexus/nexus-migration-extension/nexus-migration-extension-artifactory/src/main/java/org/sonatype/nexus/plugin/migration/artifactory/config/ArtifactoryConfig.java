package org.sonatype.nexus.plugin.migration.artifactory.config;

import static org.sonatype.nexus.plugin.migration.artifactory.util.DomUtil.getValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class ArtifactoryConfig
{

    private final Xpp3Dom dom;

    private List<ArtifactoryVirtualRepository> virtualRepositories;

    private List<ArtifactoryRepository> remoteRepositories;

    private List<ArtifactoryRepository> localRepositories;

    private Map<String, ArtifactoryProxy> proxies;

    public ArtifactoryConfig( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryConfig read( File file )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( file );
        try
        {
            return new ArtifactoryConfig( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryConfig read( InputStream input )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( input );
        try
        {
            return new ArtifactoryConfig( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    public List<ArtifactoryRepository> getLocalRepositories()
    {
        if ( localRepositories == null )
        {
            Xpp3Dom repositoriesDom = dom.getChild( "localRepositories" );
            if ( repositoriesDom == null )
            {
                return Collections.emptyList();
            }

            localRepositories = new ArrayList<ArtifactoryRepository>();
            for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "localRepository" ) )
            {
                localRepositories.add( new ArtifactoryRepository( repoDom ) );
            }
            localRepositories = Collections.unmodifiableList( localRepositories );
        }
        return localRepositories;
    }

    public List<ArtifactoryRepository> getRemoteRepositories()
    {
        if ( remoteRepositories == null )
        {
            Xpp3Dom repositoriesDom = dom.getChild( "remoteRepositories" );
            if ( repositoriesDom == null )
            {
                return Collections.emptyList();
            }

            remoteRepositories = new ArrayList<ArtifactoryRepository>();
            for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "remoteRepository" ) )
            {
                remoteRepositories.add( new ArtifactoryRepository( repoDom ) );
            }
            remoteRepositories = Collections.unmodifiableList( remoteRepositories );
        }
        return remoteRepositories;
    }

    public List<ArtifactoryVirtualRepository> getVirtualRepositories()
    {
        if ( virtualRepositories == null )
        {
            Xpp3Dom repositoriesDom = dom.getChild( "virtualRepositories" );
            if ( repositoriesDom == null )
            {
                return Collections.emptyList();
            }

            virtualRepositories = new ArrayList<ArtifactoryVirtualRepository>();
            for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "virtualRepository" ) )
            {
                virtualRepositories.add( new ArtifactoryVirtualRepository( repoDom ) );
            }
            virtualRepositories = Collections.unmodifiableList( virtualRepositories );
        }
        return virtualRepositories;
    }

    public Map<String, ArtifactoryProxy> getProxies()
    {
        if ( proxies == null )
        {
            Xpp3Dom proxiesDom = dom.getChild( "proxies" );
            if ( proxiesDom == null )
            {
                return Collections.emptyMap();
            }

            proxies = new LinkedHashMap<String, ArtifactoryProxy>();
            for ( Xpp3Dom proxyDom : proxiesDom.getChildren( "proxy" ) )
            {
                proxies.put( getValue( proxyDom, "key" ), new ArtifactoryProxy( proxyDom ) );
            }
        }
        return proxies;
    }

}
