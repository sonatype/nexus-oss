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
package org.sonatype.nexus.error.reporting.bundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.sonatype.sisu.pr.bundle.AbstractBundle;

public class MapContentsBundle
    extends AbstractBundle
{

    private static final String LINE_SEPERATOR = System.getProperty( "line.separator" );

    private byte[] content;

    public MapContentsBundle( Map<String, Object> context )
        throws UnsupportedEncodingException
    {
        super( "contextListing.txt", "text/plain" );

        StringBuilder sb = new StringBuilder();

        for ( String key : context.keySet() )
        {
            sb.append( "key: " + key );
            sb.append( LINE_SEPERATOR );

            Object o = context.get( key );
            sb.append( "value: " + o == null ? "null" : o.toString() );
            sb.append( LINE_SEPERATOR );
            sb.append( LINE_SEPERATOR );
        }

        this.content = sb.toString().getBytes( "utf-8" );
    }

    @Override
    protected InputStream openStream()
        throws IOException
    {
        return new ByteArrayInputStream( content );
    }

    @Override
    public long getContentLength()
    {
        return content.length;
    }

}
