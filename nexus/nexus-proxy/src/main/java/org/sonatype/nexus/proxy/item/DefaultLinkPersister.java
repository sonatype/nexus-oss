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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

@Component( role = LinkPersister.class )
public class DefaultLinkPersister
    implements LinkPersister
{
    private static final String UTF8_CHARSET = "UTF-8";

    private static final String LINK_PREFIX = "LINK to ";

    private static final byte[] LINK_PREFIX_BYTES = LINK_PREFIX.getBytes( Charset.forName( UTF8_CHARSET ) );

    @Requirement
    private RepositoryItemUidFactory repositoryItemUidFactory;

    public boolean isLinkContent( final ContentLocator locator )
        throws IOException
    {
        if ( locator != null )
        {
            final byte[] buf = ContentLocatorUtils.getFirstBytes( LINK_PREFIX_BYTES.length, locator );

            if ( buf != null )
            {
                return Arrays.equals( buf, LINK_PREFIX_BYTES );
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public RepositoryItemUid readLinkContent( final ContentLocator locator )
        throws NoSuchRepositoryException, IOException
    {
        if ( locator != null )
        {
            InputStream fis = null;

            try
            {
                fis = locator.getContent();

                final String linkBody = IOUtil.toString( fis, UTF8_CHARSET );

                final String uidStr = linkBody.substring( LINK_PREFIX.length(), linkBody.length() );

                return repositoryItemUidFactory.createUid( uidStr );
            }
            finally
            {
                IOUtil.close( fis );
            }
        }
        else
        {
            return null;
        }
    }

    public void writeLinkContent( final StorageLinkItem link, final OutputStream os )
        throws IOException
    {
        try
        {
            final String linkBody = LINK_PREFIX + link.getTarget().toString();

            IOUtil.copy( new ByteArrayInputStream( linkBody.getBytes( UTF8_CHARSET ) ), os );

            os.flush();
        }
        finally
        {
            IOUtil.close( os );
        }
    }
}
