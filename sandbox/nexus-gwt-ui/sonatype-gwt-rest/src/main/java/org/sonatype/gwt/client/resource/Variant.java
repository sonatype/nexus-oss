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
