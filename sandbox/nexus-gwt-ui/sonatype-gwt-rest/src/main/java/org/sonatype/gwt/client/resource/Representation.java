/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
