/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.ApplicationEventMulticaster;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

public class DeletionNotifierWalker
    extends AbstractFileWalkerProcessor
{
    private final ApplicationEventMulticaster applicationEventMulticaster;

    private final ResourceStoreRequest request;

    public DeletionNotifierWalker( ApplicationEventMulticaster applicationEventMulticaster, ResourceStoreRequest request )
    {
        this.applicationEventMulticaster = applicationEventMulticaster;

        this.request = request;
    }

    @Override
    protected void processFileItem( WalkerContext ctx, StorageFileItem item )
    {
        item.getItemContext().putAll( request.getRequestContext() );

        // just fire it, and someone will eventually catch it
        applicationEventMulticaster.notifyProximityEventListeners( new RepositoryItemEventDelete(
            ctx.getRepository(),
            item ) );
    }

}
