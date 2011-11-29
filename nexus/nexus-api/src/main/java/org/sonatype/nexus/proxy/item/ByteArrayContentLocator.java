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
package org.sonatype.nexus.proxy.item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ByteArrayContentLocator
    implements ContentLocator
{
    private final byte[] content;

    private final String mimeType;

    public ByteArrayContentLocator( final byte[] content, final String mimeType )
    {
        this.content = Arrays.copyOf( content, content.length );

        this.mimeType = mimeType;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        return new ByteArrayInputStream( content );
    }

    @Override
    public String getMimeType()
    {
        return mimeType;
    }

    @Override
    public boolean isReusable()
    {
        return true;
    }

    public long getLength()
    {
        return content.length;
    }

    public byte[] getByteArray()
    {
        return content;
    }
}
