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

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.walker.AffirmativeStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;

@Component( role = Wastebasket.class )
public class DefaultWastebasket
    implements SmartWastebasket
{
    private static final String TRASH_PATH_PREFIX = "/.nexus/trash";

    private static final long ALL = -1L;

    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

    protected Logger getLogger()
    {
        return logger;
    }

    // ==

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    // ==

    @Requirement
    private Walker walker;

    protected Walker getWalker()
    {
        return walker;
    }

    // ==

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    protected RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    // ==

    private DeleteOperation deleteOperation = DeleteOperation.MOVE_TO_TRASH;

    // ==============================
    // Wastebasket iface

    public DeleteOperation getDeleteOperation()
    {
        return deleteOperation;
    }

    public void setDeleteOperation( final DeleteOperation deleteOperation )
    {
        this.deleteOperation = deleteOperation;
    }

    public Long getTotalSize()
    {
        Long totalSize = null;

        for ( Repository repository : getRepositoryRegistry().getRepositories() )
        {
            Long repoWBSize = getSize( repository );

            if ( repoWBSize != null )
            {
                totalSize += repoWBSize;
            }
        }
        
        return totalSize;
    }

    public void purgeAll()
        throws IOException
    {
        purgeAll( ALL );
    }

    public void purgeAll( final long age )
        throws IOException
    {
        for ( Repository repository : getRepositoryRegistry().getRepositories() )
        {
            purge( repository, age );
        }

        if ( age == ALL )
        {
            // NEXUS-4078: deleting "legacy" trash too for now
            File basketFile =
                getApplicationConfiguration().getWorkingDirectory( AbstractRepositoryFolderCleaner.GLOBAL_TRASH_KEY );

            // check for existence, is this needed at all?
            if ( basketFile.isDirectory() )
            {
                AbstractRepositoryFolderCleaner.deleteFilesRecursively( basketFile );
            }
        }
    }

    public Long getSize( final Repository repository )
    {
        return null;
    }

    public void purge( final Repository repository )
        throws IOException
    {
        purge( repository, ALL );
    }

    public void purge( final Repository repository, final long age )
        throws IOException
    {
        ResourceStoreRequest req = new ResourceStoreRequest( getTrashPath( repository, RepositoryItemUid.PATH_ROOT ) );

        if ( age == ALL )
        {
            // simple and fast way, no need for walker
            try
            {
                repository.getLocalStorage().shredItem( repository, req );
            }
            catch ( ItemNotFoundException e )
            {
                // silent
            }
            catch ( UnsupportedStorageOperationException e )
            {
                // silent?
            }
        }
        else
        {
            // walker and walk and changes for age
            if ( repository.getLocalStorage().containsItem( repository, req ) )
            {
                req.setRequestGroupLocalOnly( true );

                req.setRequestLocalOnly( true );

                DefaultWalkerContext ctx =
                    new DefaultWalkerContext( repository, req, new AffirmativeStoreWalkerFilter() );

                ctx.getProcessors().add( new WastebasketWalker( age ) );

                getWalker().walk( ctx );
            }
        }
    }

    public void delete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws LocalStorageException
    {
        try
        {
            if ( DeleteOperation.MOVE_TO_TRASH.equals( getDeleteOperation() ) )
            {
                ResourceStoreRequest trashed =
                    new ResourceStoreRequest( getTrashPath( repository, request.getRequestPath() ) );

                ls.moveItem( repository, request, trashed );
            }

            ls.shredItem( repository, request );
        }
        catch ( ItemNotFoundException e )
        {
            // silent
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // yell
            throw new LocalStorageException( "Delete operation is unsupported!", e );
        }
    }

    public boolean undelete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws LocalStorageException
    {
        try
        {
            ResourceStoreRequest trashed =
                new ResourceStoreRequest( getTrashPath( repository, request.getRequestPath() ) );

            ResourceStoreRequest untrashed =
                new ResourceStoreRequest( getUnTrashPath( repository, request.getRequestPath() ) );

            if ( !ls.containsItem( repository, untrashed ) )
            {
                ls.moveItem( repository, trashed, untrashed );

                return true;
            }
        }
        catch ( ItemNotFoundException e )
        {
            // silent
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // yell
            throw new LocalStorageException( "Undelete operation is unsupported!", e );
        }

        return false;
    }

    // ==============================
    // SmartWastebasket iface

    public void setMaximumSizeConstraint( MaximumSizeConstraint constraint )
    {
        // TODO Auto-generated method stub

    }

    // ==

    protected String getTrashPath( final Repository repository, final String path )
    {
        if ( path.startsWith( TRASH_PATH_PREFIX ) )
        {
            return path;
        }
        else if ( path.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            return TRASH_PATH_PREFIX + path;
        }
        else
        {
            return TRASH_PATH_PREFIX + RepositoryItemUid.PATH_SEPARATOR + path;
        }
    }

    protected String getUnTrashPath( final Repository repository, final String path )
    {
        String result = path;

        if ( result.startsWith( TRASH_PATH_PREFIX ) )
        {
            result = result.substring( TRASH_PATH_PREFIX.length(), result.length() );
        }

        if ( !result.startsWith( RepositoryItemUid.PATH_ROOT ) )
        {
            result = RepositoryItemUid.PATH_ROOT + result;
        }

        return result;
    }
}
