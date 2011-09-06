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
package org.sonatype.nexus.bundle.launcher.util;

import org.slf4j.Logger;
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;

class AetherTransferListener
    extends AbstractTransferListener
{

    private final Logger log;

    private ThreadLocal<Long> last;

    public AetherTransferListener( Logger log )
    {
        this.log = log;
        last = new ThreadLocal<Long>();
    }

    @Override
    public void transferInitiated( TransferEvent event )
        throws TransferCancelledException
    {
        log.info( "Downloading {}{}...", event.getResource().getRepositoryUrl(), event.getResource().getResourceName() );
    }

    @Override
    public void transferSucceeded( TransferEvent event )
    {
        log.info( "Downloaded [{} bytes] {}{}", new Object[] { event.getTransferredBytes(),
            event.getResource().getRepositoryUrl(), event.getResource().getResourceName() } );
    }

    @Override
    public void transferFailed( TransferEvent event )
    {
        log.error( "Failed to download {}{}", new Object[] { event.getResource().getRepositoryUrl(),
            event.getResource().getResourceName(), event.getException() } );
    }

    @Override
    public void transferProgressed( TransferEvent event )
        throws TransferCancelledException
    {
        Long last = this.last.get();
        if ( last == null || last.longValue() < System.currentTimeMillis() - 5 * 1000 )
        {
            String progress;
            if ( event.getResource().getContentLength() > 0 )
            {
                progress = (int) ( event.getTransferredBytes() * 100.0 / event.getResource().getContentLength() ) + "%";
            }
            else
            {
                progress = event.getTransferredBytes() + " bytes";
            }
            log.info( "Downloading [{}] {}{}...", new Object[] { progress, event.getResource().getRepositoryUrl(),
                event.getResource().getResourceName() } );
            this.last.set( new Long( System.currentTimeMillis() ) );
        }
    }

}
