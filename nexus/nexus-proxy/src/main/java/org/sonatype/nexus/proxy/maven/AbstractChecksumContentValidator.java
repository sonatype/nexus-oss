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
package org.sonatype.nexus.proxy.maven;

import java.util.List;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

public abstract class AbstractChecksumContentValidator
    extends AbstractLoggingComponent
{

    public AbstractChecksumContentValidator()
    {
        super();
    }

    public boolean isRemoteItemContentValid(final  ProxyRepository proxy, final ResourceStoreRequest req, final String baseUrl,
                                             final AbstractStorageItem item, final List<RepositoryItemValidationEvent> events )
        throws LocalStorageException
    {
        ChecksumPolicy checksumPolicy = getChecksumPolicy( proxy, item );
        if ( checksumPolicy == null || !checksumPolicy.shouldCheckChecksum() )
        {
            return true;
        }

        RemoteHashResponse remoteHash = retrieveRemoteHash( item, proxy, baseUrl );

        // let compiler make sure I did not forget to populate validation results
        String msg;
        boolean contentValid;

        if ( remoteHash == null && ChecksumPolicy.STRICT.equals( checksumPolicy ) )
        {
            msg =
                "The artifact " + item.getPath() + " has no remote checksum in repository " + item.getRepositoryId()
                    + "! The checksumPolicy of repository forbids downloading of it.";

            contentValid = false;
        }
        else if ( remoteHash == null )
        {
            msg =
                "Warning, the artifact " + item.getPath() + " has no remote checksum in repository "
                    + item.getRepositoryId() + "!";

            contentValid = true; // policy is STRICT_IF_EXIST or WARN
        }
        else if ( remoteHash.getRemoteHash().equals( retrieveLocalHash( item, remoteHash.getInspector() ) ) )
        {
            // remote hash exists and matches item content
            return true;
        }
        else if ( ChecksumPolicy.WARN.equals( checksumPolicy ) )
        {
            msg =
                "Warning, the artifact " + item.getPath() + " and it's remote checksums does not match in repository "
                    + item.getRepositoryId() + "!";

            contentValid = true;
        }
        else
        // STRICT or STRICT_IF_EXISTS
        {
            msg =
                "The artifact " + item.getPath() + " and it's remote checksums does not match in repository "
                    + item.getRepositoryId() + "! The checksumPolicy of repository forbids downloading of it.";

            contentValid = false;
        }

        if ( !contentValid )
        {
            getLogger().debug( "Validation failed due: " + msg );
        }

        events.add( newChechsumFailureEvent( proxy, item, msg ) );

        cleanup( proxy, remoteHash, contentValid );

        return contentValid;
    }

    protected String retrieveLocalHash( AbstractStorageItem item, String inspector )
    {
        return item.getRepositoryItemAttributes().get( inspector );
    }

    protected abstract void cleanup( ProxyRepository proxy, RemoteHashResponse remoteHash, boolean contentValid )
        throws LocalStorageException;

    protected abstract RemoteHashResponse retrieveRemoteHash( AbstractStorageItem item, ProxyRepository proxy,
                                                              String baseUrl )
        throws LocalStorageException;

    protected abstract ChecksumPolicy getChecksumPolicy( ProxyRepository proxy, AbstractStorageItem item )
        throws LocalStorageException;

    private RepositoryItemValidationEvent newChechsumFailureEvent( final ProxyRepository proxy, final AbstractStorageItem item, final String msg )
    {
        return new ChecksumContentValidationEventFailed( proxy, item, msg );
    }

}