/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.item;

import java.io.UnsupportedEncodingException;

import org.codehaus.plexus.util.StringUtils;

/**
 * A simple content locator that emits a string actually.
 * 
 * @author cstamas
 */
public class StringContentLocator
    extends ByteArrayContentLocator
{
    private static final String ENCODING = "UTF-8";

    private final String content;

    public StringContentLocator( String content )
    {
        this( content, null );
    }

    public StringContentLocator( String content, String mimeType )
    {
        super( toByteArray( content ), StringUtils.isBlank( mimeType ) ? "text/plain" : mimeType );

        this.content = content;
    }

    public String getString()
    {
        return content;
    }

    public static byte[] toByteArray( String string )
    {
        try
        {
            return string.getBytes( ENCODING );
        }
        catch ( UnsupportedEncodingException e )
        {
            // heh? will not happen
            return new byte[0];
        }
    }
}
