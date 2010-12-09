package org.sonatype.nexus.proxy.maven.metadata.operations;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MetadataException
    extends Exception
{

    private static final long serialVersionUID = -5336177865089762129L;

    public MetadataException( XmlPullParserException e )
    {
        super( e.getMessage(), e );
    }

    public MetadataException( String msg )
    {
        super( msg );
    }

    public MetadataException( Exception e )
    {
        super( e );
    }

    public MetadataException( String msg, Exception e )
    {
        super( msg, e );
    }

}
