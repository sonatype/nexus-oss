package org.sonatype.gwt.client.resource;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 * A simple representation that takes into account being GWT "client side" code. Hence, the supported formats are
 * "hardwired" in to keep it simple and fast.
 * 
 * @author cstamas
 */
public class Representation
    extends Variant
{
    private String body;

    public Representation( Variant variant, String body )
    {
        super( variant.getMediaType() );

        this.body = body;
    }

    public Representation( String body )
    {
        super( PLAIN_TEXT );

        this.body = body;
    }

    public Representation( JSONValue json )
    {
        super( APPLICATION_JSON );

        this.body = json.toString();
    }

    public Representation( Document doc )
    {
        super( APPLICATION_XML );

        this.body = doc.toString();
    }

    public Representation( Response response )
    {
        super( response.getHeader( "Content-Type" ) );

        this.body = response.getText();
    }

    public String getText()
    {
        return body;
    }

    public Object getParsed()
    {
        if ( APPLICATION_JSON.getMediaType().equals( getMediaType() ) )
        {
            return JSONParser.parse( getText() );
        }
        else if ( APPLICATION_XML.getMediaType().equals( getMediaType() ) )
        {
            return XMLParser.parse( getText() );
        }
        else if ( APPLICATION_RSS.getMediaType().equals( getMediaType() ) )
        {
            return XMLParser.parse( getText() );
        }
        else if ( APPLICATION_ATOM.getMediaType().equals( getMediaType() ) )
        {
            return XMLParser.parse( getText() );
        }
        else
        {
            return getText();
        }
    }

    public long getSize()
    {
        return body.length();
    }
}
