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
package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * The Nexus default marshaller: uses XStream to marshall complete StorageItem instances as XML.
 * 
 * @author cstamas
 */
@Singleton
@Named( "xstream-xml" )
public class XStreamMarshaller
    implements Marshaller
{
    private final XStream xstream;

    public XStreamMarshaller()
    {
        this.xstream = new XStream();
        this.xstream.alias( "file", DefaultStorageFileItem.class );
        this.xstream.alias( "compositeFile", DefaultStorageCompositeFileItem.class );
        this.xstream.alias( "collection", DefaultStorageCollectionItem.class );
        this.xstream.alias( "link", DefaultStorageLinkItem.class );
    }

    @Override
    public void marshal( final StorageItem item, final OutputStream outputStream )
        throws IOException
    {
        xstream.toXML( item, outputStream );
        outputStream.flush();
    }

    @Override
    public StorageItem unmarshal( final InputStream inputStream )
        throws IOException
    {
        try
        {
            return (StorageItem) xstream.fromXML( inputStream );
        }
        catch ( NullPointerException e )
        {
            // see NEXUS-3911: XPP3 throws sometimes NPE on "corrupted XMLs in some specific way"
            throw new InvalidInputException(
                "XPP3 thrown a NPE, see NEXUS-3911 for details, and input is claimed as corrupt.", e );
        }
        catch ( XStreamException e )
        {
            // it is corrupt -- so says XStream, but see above and NEXUS-3911
            throw new InvalidInputException( "XStream claimed file as corrupt.", e );
        }
    }

}
