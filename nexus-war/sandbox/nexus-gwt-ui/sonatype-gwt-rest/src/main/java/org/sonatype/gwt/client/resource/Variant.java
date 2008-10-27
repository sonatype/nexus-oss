package org.sonatype.gwt.client.resource;

/**
 * Variant, that is actually marking the MIME type.
 * 
 * @author cstamas
 */
public class Variant
{
    private String mediaType;

    public static final Variant PLAIN_TEXT = new Variant( "plain/text" );

    public static final Variant APPLICATION_JSON = new Variant( "application/json" );

    public static final Variant APPLICATION_XML = new Variant( "application/xml" );

    public static final Variant APPLICATION_RSS = new Variant( "application/rss+xml" );

    public static final Variant APPLICATION_ATOM = new Variant( "application/atom+xml" );

    public Variant( String mediaType )
    {
        super();

        if ( mediaType.indexOf( ';' ) > -1 )
        {
            // this is content-type header in format "XXX/XXX ; charset..."
            mediaType = mediaType.substring( 0, mediaType.indexOf( ';' ) );
        }

        this.mediaType = mediaType.toLowerCase();
    }

    public Variant( Variant variant )
    {
        super();

        this.mediaType = variant.getMediaType();
    }

    public String getMediaType()
    {
        return mediaType;
    }

}
