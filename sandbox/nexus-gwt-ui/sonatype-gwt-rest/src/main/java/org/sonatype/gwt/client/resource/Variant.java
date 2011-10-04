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
