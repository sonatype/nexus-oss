/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
