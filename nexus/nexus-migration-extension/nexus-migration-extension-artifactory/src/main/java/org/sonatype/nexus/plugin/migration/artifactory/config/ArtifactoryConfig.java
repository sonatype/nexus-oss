package org.sonatype.nexus.plugin.migration.artifactory.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class ArtifactoryConfig
{

    private final Xpp3Dom dom;

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
        Xpp3Dom repositoriesDom = dom.getChild( "localRepositories" );
        if ( repositoriesDom == null )
        {
            return Collections.emptyList();
        }

        List<ArtifactoryRepository> repos = new ArrayList<ArtifactoryRepository>();
        for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "localRepository" ) )
        {
            repos.add( new ArtifactoryRepository( repoDom ) );
        }
        return Collections.unmodifiableList( repos );
    }

    public List<ArtifactoryRepository> getRemoteRepositories()
    {
        Xpp3Dom repositoriesDom = dom.getChild( "remoteRepositories" );
        if ( repositoriesDom == null )
        {
            return Collections.emptyList();
        }

        List<ArtifactoryRepository> repos = new ArrayList<ArtifactoryRepository>();
        for ( Xpp3Dom repoDom : repositoriesDom.getChildren( "remoteRepository" ) )
        {
            repos.add( new ArtifactoryRepository( repoDom ) );
        }
        return Collections.unmodifiableList( repos );
    }

}
