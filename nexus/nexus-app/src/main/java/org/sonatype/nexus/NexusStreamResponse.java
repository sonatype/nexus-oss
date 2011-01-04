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
package org.sonatype.nexus;

import java.io.InputStream;

public class NexusStreamResponse
{
    private String name;
    
    private InputStream inputStream;

    private long size;

    private String mimeType;

    private long fromByte;

    private long bytesCount;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream( InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize( long contentLength )
    {
        this.size = contentLength;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType( String contentType )
    {
        this.mimeType = contentType;
    }

    public long getFromByte()
    {
        return fromByte;
    }

    public void setFromByte( long fromByte )
    {
        this.fromByte = fromByte;
    }

    public long getBytesCount()
    {
        return bytesCount;
    }

    public void setBytesCount( long bytesCount )
    {
        this.bytesCount = bytesCount;
    }
}
