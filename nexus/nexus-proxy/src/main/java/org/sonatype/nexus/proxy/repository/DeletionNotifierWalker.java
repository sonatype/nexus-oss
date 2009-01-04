/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.Map;

import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

public class DeletionNotifierWalker
    extends AbstractFileWalkerProcessor
{
    private final Repository repository;

    private final Map<String, Object> context;

    public DeletionNotifierWalker( Repository repository, Map<String, Object> context )
    {
        this.repository = repository;

        this.context = context;
    }

    @Override
    protected void processFileItem( WalkerContext ctx, StorageFileItem item )
    {
        if ( context != null )
        {
            item.getItemContext().putAll( context );
        }

        // just fire it, and someone will eventually catch it
        repository.notifyProximityEventListeners( new RepositoryItemEventDelete( ctx.getRepository(), item ) );
    }

}
