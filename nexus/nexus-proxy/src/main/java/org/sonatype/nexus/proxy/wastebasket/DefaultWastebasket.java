package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.statistics.DeferredLong;
import org.sonatype.nexus.proxy.statistics.DeferredLongSum;
import org.sonatype.nexus.proxy.statistics.impl.DefaultDeferredLong;
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

    @Requirement
    private Logger logger;

    @Requirement
    private Walker walker;

    protected Logger getLogger()
    {
        return logger;
    }

    // ==

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    protected RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

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

    public DeferredLong getTotalSize()
    {
        ArrayList<DeferredLong> sizes = new ArrayList<DeferredLong>();

        for ( Repository repository : getRepositoryRegistry().getRepositories() )
        {
            sizes.add( getSize( repository ) );
        }

        return new DeferredLongSum( sizes );
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
    }

    public DeferredLong getSize( final Repository repository )
    {
        return new DefaultDeferredLong( -1L );
    }

    public void purge( final Repository repository )
        throws IOException
    {
        purge( repository, ALL );
    }

    public void purge( final Repository repository, final long age )
        throws IOException
    {
        ResourceStoreRequest trashRoot =
            new ResourceStoreRequest( getTrashPath( repository, RepositoryItemUid.PATH_ROOT ) );

        if ( age == ALL )
        {
            // simple and fast way, no need for walker
            try
            {
                repository.getLocalStorage().shredItem( repository, trashRoot );
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
            if ( repository.getLocalStorage().containsItem( repository, trashRoot ) )
            {
                DefaultWalkerContext ctx =
                    new DefaultWalkerContext( repository, trashRoot, new AffirmativeStoreWalkerFilter() );
                
                ctx.getProcessors().add( new WastebasketWalker( age ) );

                walker.walk( ctx );
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
