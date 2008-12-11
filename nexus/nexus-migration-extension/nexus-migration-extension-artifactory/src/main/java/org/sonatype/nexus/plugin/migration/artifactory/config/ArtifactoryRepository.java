package org.sonatype.nexus.plugin.migration.artifactory.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class ArtifactoryRepository
{

    private final Xpp3Dom dom;

    public ArtifactoryRepository( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryRepository read( File file )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( file );
        try
        {
            return new ArtifactoryRepository( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    @SuppressWarnings( "deprecation" )
    public static ArtifactoryRepository read( InputStream input )
        throws IOException, XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( input );
        try
        {
            return new ArtifactoryRepository( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            reader.close();
        }
    }

    public String getKey()
    {
        return dom.getChild( "key" ).getValue();
    }

    public String getDescription()
    {
        return dom.getChild( "description" ).getValue();
    }

    public boolean getHandleReleases()
    {
        return Boolean.parseBoolean( dom.getChild( "handleReleases" ).getValue() );
    }

    public boolean getHandleSnapshots()
    {
        return Boolean.parseBoolean( dom.getChild( "handleSnapshots" ).getValue() );
    }

}
