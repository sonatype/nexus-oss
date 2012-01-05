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

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;

/**
 * AttributeStorage that actually delegates the work to other instance of AttributeStorage, and having an option of
 * "fallback" to some secondary instance. Usable for scenarios where "transitioning" (smooth upgrade for example) is to
 * be used, the "main" attribute storage would be "upgraded" from "legacy" attribute storage as the attributes are
 * requested over the time from this instance. This class is not a component, but is used by AttributesHandler when
 * "transitioning" is needed.
 * 
 * @author cstamas
 * @since 1.10.0
 */
public class TransitioningAttributeStorage
    implements AttributeStorage
{
    private final AttributeStorage mainAttributeStorage;

    private final AttributeStorage fallbackAttributeStorage;

    public TransitioningAttributeStorage( final AttributeStorage mainAttributeStorage,
                                          final AttributeStorage fallbackAttributeStorage )
    {
        super();
        this.mainAttributeStorage = mainAttributeStorage;
        this.fallbackAttributeStorage = fallbackAttributeStorage;
    }

    @Override
    public Attributes getAttributes( final RepositoryItemUid uid )
        throws IOException
    {
        final RepositoryItemUidLock uidLock = uid.getAttributeLock();

        uidLock.lock( Action.read );

        try
        {
            Attributes result = mainAttributeStorage.getAttributes( uid );

            if ( result == null && fallbackAttributeStorage != null )
            {
                uidLock.lock( Action.create );

                try
                {
                    result = fallbackAttributeStorage.getAttributes( uid );

                    if ( result != null )
                    {
                        mainAttributeStorage.putAttributes( uid, result );

                        try
                        {
                            fallbackAttributeStorage.deleteAttributes( uid );
                        }
                        catch ( IOException e )
                        {
                            // legacy swallows them, this is needed only to satisfy it's signature
                        }
                    }
                }
                finally
                {
                    uidLock.unlock();
                }
            }

            return result;
        }
        finally
        {
            uidLock.unlock();
        }
    }

    @Override
    public void putAttributes( final RepositoryItemUid uid, final Attributes item )
        throws IOException
    {
        final RepositoryItemUidLock uidLock = uid.getAttributeLock();

        uidLock.lock( Action.create );

        try
        {
            mainAttributeStorage.putAttributes( uid, item );

            if ( fallbackAttributeStorage != null )
            {
                try
                {
                    fallbackAttributeStorage.deleteAttributes( uid );
                }
                catch ( IOException e )
                {
                    // legacy swallows them, this is needed only to satisfy it's signature
                }
            }
        }
        finally
        {
            uidLock.unlock();
        }
    }

    @Override
    public boolean deleteAttributes( final RepositoryItemUid uid )
        throws IOException
    {
        final RepositoryItemUidLock uidLock = uid.getAttributeLock();

        uidLock.lock( Action.delete );

        try
        {
            final boolean mainResult = mainAttributeStorage.deleteAttributes( uid );

            try
            {
                final boolean legacyResult =
                    ( fallbackAttributeStorage != null && fallbackAttributeStorage.deleteAttributes( uid ) );

                return mainResult || legacyResult;
            }
            catch ( IOException e )
            {
                // legacy swallows them, this is needed only to satisfy it's signature
            }

            return mainResult;
        }
        finally
        {
            uidLock.unlock();
        }
    }
}
