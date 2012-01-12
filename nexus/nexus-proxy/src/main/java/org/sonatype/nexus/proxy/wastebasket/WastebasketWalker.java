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
package org.sonatype.nexus.proxy.wastebasket;

import java.util.Collection;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.SilentWalker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerProcessor;

public class WastebasketWalker
    extends AbstractWalkerProcessor
    implements WalkerProcessor, SilentWalker
{

    private long age;

    public WastebasketWalker( long age )
    {
        this.age = age;
    }

    @Override
    public void processItem( WalkerContext ctx, StorageItem item )
    {
        long now = System.currentTimeMillis();
        long limitDate = now - age;

        if ( item instanceof StorageFileItem && //
            ( age == DefaultWastebasket.ALL || item.getModified() < limitDate ) )
        {
            try
            {
                ctx.getRepository().getLocalStorage().shredItem( ctx.getRepository(), item.getResourceStoreRequest() );
            }
            catch ( ItemNotFoundException e )
            {
                // silent
            }
            catch ( UnsupportedStorageOperationException e )
            {
                // silent?
            }
            catch ( LocalStorageException e )
            {
                // silent?
            }
        }
    }

    @Override
    public void onCollectionExit( WalkerContext ctx, StorageCollectionItem item )
        throws Exception
    {
        if ( DefaultWastebasket.TRASH.equals( item.getName() ) )
        {
            // NEXUS-4642 do not delete the trash
            return;
        }

        // item is now gone, let's check if this is empty and if so delete it as well
        Collection<StorageItem> items = item.list();
        if ( items.isEmpty() )
        {
            ctx.getRepository().getLocalStorage().shredItem( ctx.getRepository(), item.getResourceStoreRequest() );
        }
    }
}
